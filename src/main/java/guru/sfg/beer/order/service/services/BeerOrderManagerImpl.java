package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.springframework.springmsbeercommon.beerorder.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import guru.springframework.springmsbeercommon.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor interceptor;

    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        // forcing to be a new
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
        // =====
        BeerOrder savedOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedOrder, BeerOrderEventEnum.VALIDATE_ORDER);
        return savedOrder;
    }

    @Override
    public void handleValidation(UUID orderId, boolean isValid) {
        BeerOrder order = beerOrderRepository.findOneById(orderId);
        if (isValid) {
            /*
                John decided to send another event - ALLOCATE_ORDER to allocation process
                instead of associating a VALIDATION_PASSED event with the AllocateOrderAction

            */
            sendBeerOrderEvent(order, BeerOrderEventEnum.VALIDATION_PASSED);
            // entity state changed, we need a new representation
            BeerOrder validatedOrder = beerOrderRepository.findOneById(orderId);
            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        } else {
            sendBeerOrderEvent(order, BeerOrderEventEnum.VALIDATION_FAILED);
        }

    }

    @Override
    public void processAllocation(BeerOrderDto beerOrderDto, Boolean allocationError, Boolean inventoryPending) {
        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderDto.getId());

        if (allocationError) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        } else if (!inventoryPending) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
            updateAllocatedQuantity(beerOrderDto);
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
            updateAllocatedQuantity(beerOrderDto);
        }
    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        log.info("picking up order");
        Optional<BeerOrder> beerOrderOp = Optional.ofNullable(beerOrderRepository.findOneById(id));
        beerOrderOp.ifPresent(beerOrder -> {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.BEER_ORDER_PICKED_UP);
            log.debug("successfully fired PICKED_UP event for {} order", beerOrder.getId());
        });

    }

    @Override
    public void cancelOrder(UUID orderId) {
        log.debug("Canceling order {}", orderId);
        Optional<BeerOrder> beerOrderOp = beerOrderRepository.findById(orderId);
        beerOrderOp.ifPresent(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.CANCLE_ORDER));
    }

    private void updateAllocatedQuantity(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderDto.getId());
        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                    beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                }
            });
        });
        beerOrderRepository.saveAndFlush(beerOrder);

    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);
        Message msg = MessageBuilder
                .withPayload(eventEnum)
                .setHeader(BeerOrderManager.BEER_ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();
        sm.sendEvent(msg);

    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        // return state machine for specific beer order
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());
        sm.stop();
        // for restoring state from database
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(interceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
    // if happens some issues with status change should be impl
    private void awaitForStatus(UUID orderId, BeerOrderStatusEnum status) {
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(false);
                log.debug("loop retries exceeded");
                return;
            }

            beerOrderRepository.findById(orderId).ifPresent(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(status)) {
                    log.debug("Order in correct status found");
                    found.set(true);
                } else {
                    log.debug("Order status not equal, expected {} found {}", status, beerOrder.getOrderStatus());
                }
            });

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            }
        }
    }
}
