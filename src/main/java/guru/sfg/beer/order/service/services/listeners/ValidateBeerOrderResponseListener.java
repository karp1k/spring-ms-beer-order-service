package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.springframework.springmsbeercommon.web.config.Constants;
import guru.springframework.springmsbeercommon.web.events.ValidateBeerOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author kas
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateBeerOrderResponseListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = Constants.VALIDATE_ORDER_RESULT_QUEUE)
    public void listen(ValidateBeerOrderResponse response) {
        beerOrderManager.handleValidation(response.getOrderId(), response.getIsValid());
    }
}
