package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.springframework.springmsbeercommon.web.model.BeerOrderDto;

import java.util.UUID;

/**
 * @author kas
 */
public interface BeerOrderManager {

    String BEER_ORDER_ID_HEADER = "beer_order_id";

    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void handleValidation(UUID orderId, boolean isValid);

    void processAllocation(BeerOrderDto beerOrderDto, Boolean allocationError, Boolean inventoryPending);

    void beerOrderPickedUp(UUID id);
}
