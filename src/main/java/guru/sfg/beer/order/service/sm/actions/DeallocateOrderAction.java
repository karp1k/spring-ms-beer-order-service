package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.springframework.springmsbeercommon.beerorder.domain.BeerOrderStatusEnum;
import guru.springframework.springmsbeercommon.web.config.Constants;
import guru.springframework.springmsbeercommon.web.events.DeallocateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author kas
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper mapper;
    private final BeerOrderRepository beerOrderRepository;


    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String orderId = (String) stateContext.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);
        log.debug("Deallocate process for {}", orderId);
        BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(orderId));
        jmsTemplate.convertAndSend(Constants.DEALLOCATE_ORDER_QUEUE, DeallocateOrderRequest
                .builder()
                .beerOrderDto(mapper.beerOrderToDto(beerOrder))
                .build()
        );
    }
}
