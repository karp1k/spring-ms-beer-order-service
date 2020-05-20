package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.springframework.springmsbeercommon.beerorder.domain.BeerOrderStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * in real world can be a callback for the another service about a failed event,
 * restore entity's some values in the db and etc.
 * for compensating transaction pattern
 * @author kas
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String orderId = (String) stateContext.getMessageHeader(BeerOrderManager.BEER_ORDER_ID_HEADER);
        log.error("Validation failed for {}", orderId);
    }
}
