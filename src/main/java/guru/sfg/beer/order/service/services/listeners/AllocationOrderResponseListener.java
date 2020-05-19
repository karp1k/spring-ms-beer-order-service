package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.springframework.springmsbeercommon.web.config.Constants;
import guru.springframework.springmsbeercommon.web.events.AllocationOrderResponse;
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
public class AllocationOrderResponseListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = Constants.ALLOCATE_ORDER_RESULT_QUEUE)
    public void listener(AllocationOrderResponse response) {
        beerOrderManager.processAllocation(response.getBeerOrderDto(),
                response.getAllocationError(), response.getInventoryPending());
    }
}
