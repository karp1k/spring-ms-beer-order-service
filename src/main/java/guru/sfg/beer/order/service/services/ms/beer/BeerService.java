package guru.sfg.beer.order.service.services.ms.beer;

import guru.sfg.beer.order.service.web.model.ms.beer.BeerDto;

import java.util.Optional;
import java.util.UUID;

/**
 * @author kas
 */
public interface BeerService {

    Optional<BeerDto> getBeerDtoById(UUID id);
    Optional<BeerDto> getBeerDtoByUpc(String upc);

    String getNameById(UUID beerId);

}
