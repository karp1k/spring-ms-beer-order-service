package guru.sfg.beer.order.service.testcomponents;

import guru.springframework.springmsbeercommon.web.config.Constants;
import guru.springframework.springmsbeercommon.web.events.ValidateBeerOrderRequest;
import guru.springframework.springmsbeercommon.web.events.ValidateBeerOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author kas
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestBeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = Constants.VALIDATE_ORDER_QUEUE)
    public void listen(Message message) {
        boolean isValid = true;

        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) message.getPayload();
        if (request.getBeerOrderDto().getCustomerRef().equals("fail-validation")) {
            isValid = false;
        }

        jmsTemplate.convertAndSend(Constants.VALIDATE_ORDER_RESULT_QUEUE, ValidateBeerOrderResponse.builder()
                .isValid(isValid).orderId(request.getBeerOrderDto().getId()).build());

    }
}
