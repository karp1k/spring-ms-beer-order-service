package guru.sfg.beer.order.service.sm;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * @author kas
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void preStateChange(
            State<BeerOrderStatusEnum,
            BeerOrderEventEnum> state,
            Message<BeerOrderEventEnum> message,
            Transition<BeerOrderStatusEnum,
            BeerOrderEventEnum> transition,
            StateMachine<BeerOrderStatusEnum,
            BeerOrderEventEnum> stateMachine,
            StateMachine<BeerOrderStatusEnum,
            BeerOrderEventEnum> rootStateMachine) {

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault(BeerOrderManager.BEER_ORDER_ID_HEADER," ")))
                .ifPresent(beerOrderId -> {
                    BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(beerOrderId));
                    beerOrder.setOrderStatus(state.getId());
                    // saveAndFlush to escape hibernate lazy write
                    beerOrderRepository.saveAndFlush(beerOrder);
                });


    }
}
