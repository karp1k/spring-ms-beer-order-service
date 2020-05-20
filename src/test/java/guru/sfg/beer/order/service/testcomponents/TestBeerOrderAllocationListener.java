package guru.sfg.beer.order.service.testcomponents;

import guru.springframework.springmsbeercommon.web.config.Constants;
import guru.springframework.springmsbeercommon.web.events.AllocationOrderRequest;
import guru.springframework.springmsbeercommon.web.events.AllocationOrderResponse;
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
public class TestBeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = Constants.ALLOCATE_ORDER_QUEUE)
    public void listen(Message message) {

        boolean isAllocationError = false;
        boolean isInventoryPending = false;

        AllocationOrderRequest request = (AllocationOrderRequest) message.getPayload();
        switch (request.getBeerOrderDto().getCustomerRef()) {
            case "error-allocation":
                isAllocationError = true;
                break;
            case "partial-allocation":
                isInventoryPending = true;
                break;
            case "allocation-cancel-order":
                return;
            default:
                break;
        }

        boolean isPartialAllocation = isInventoryPending;
        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            int allocatedQuantity = beerOrderLineDto.getOrderQuantity();
            if (isPartialAllocation) {
                --allocatedQuantity;
            }
            beerOrderLineDto.setQuantityAllocated(allocatedQuantity);
        });


        jmsTemplate.convertAndSend(Constants.ALLOCATE_ORDER_RESULT_QUEUE, AllocationOrderResponse.builder()
                        .beerOrderDto(request.getBeerOrderDto())
                        .allocationError(isAllocationError)
                        .inventoryPending(isPartialAllocation)
                        .build()
                );

    }
}
