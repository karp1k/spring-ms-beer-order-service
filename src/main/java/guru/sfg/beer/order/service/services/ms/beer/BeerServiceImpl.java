package guru.sfg.beer.order.service.services.ms.beer;

import guru.springframework.springmsbeercommon.web.model.BeerDto ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author kas
 */
@Slf4j
@Service
public class BeerServiceImpl implements BeerService {

    private static final String BEER_PATH = "/api/v1/beer/";
    private static final String BEER_UPC_PATH = BEER_PATH + "upc/";
    private final RestTemplate restTemplate;
    private final String beerServiceHost;


    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder,
                           @Value("${ms.brewery.beer-service.host}") String beerServiceHost) {
        this.restTemplate = restTemplateBuilder.build();
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerDtoById(UUID id) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_PATH + id.toString(), BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerDtoByUpc(String upc) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + BEER_UPC_PATH + upc, BeerDto.class));
    }

    @Override
    public String getNameById(UUID beerId) {
        //todo impl and call method which would return only name instead of whole DTO
        ResponseEntity<BeerDto> beerDtoResponse = restTemplate.getForEntity(beerServiceHost + BEER_PATH,
                BeerDto.class, beerId);
        return beerDtoResponse.getBody().getBeerName();
    }
}
