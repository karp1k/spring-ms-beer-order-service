package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;

import java.util.UUID;

/**
 * @author kas
 */
public interface BeerOrderManager {

    public static final String BEER_ORDER_ID_HEADER = "beer_order_id";

    BeerOrder newBeerOrder(BeerOrder beerOrder);
    void handleValidation(UUID orderId, boolean isValid);
}
