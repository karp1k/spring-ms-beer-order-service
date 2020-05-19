package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.ms.beer.BeerServiceImpl;
import guru.sfg.beer.order.service.web.model.BeerOrderPagedList;
import guru.springframework.springmsbeercommon.beerorder.domain.BeerOrderStatusEnum;
import guru.springframework.springmsbeercommon.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.*;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WireMockExtension.class) // junit 5
@SpringBootTest
class BeerOrderManagerImplTestIT {

    @Autowired
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    Customer customer;

    BeerOrder beerOrder;

    UUID beerId;
    String upc;

    @BeforeEach
    void setUp() {
        beerId = UUID.randomUUID();
        upc = "1234";

        customer = customerRepository.save(Customer.builder()
                .customerName("Test customer")
                .build());

        beerOrder = BeerOrder.builder()
                .customer(customer)
                .build();

        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .beerId(beerId)
                .upc(upc)
                .orderQuantity(10)
                .beerOrder(beerOrder)
                .build();

        beerOrder.setBeerOrderLines(new HashSet<>(Arrays.asList(beerOrderLine)));
    }

    @Test
    void newBeerOrder() throws InterruptedException {
        BeerDto beerDto = BeerDto.builder()
                .id(beerId)
                .upc(upc)
                .build();


        try {
            wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH + upc).willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        BeerOrder finalSavedBeerOrder = savedBeerOrder;
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(finalSavedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });
        //Thread.sleep(5000); // to fast processing stoping thread
        savedBeerOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
        assertNotNull(savedBeerOrder);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder.getOrderStatus());
        savedBeerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            assertEquals(beerOrderLine.getOrderQuantity(), beerOrderLine.getQuantityAllocated());
        });

    }

    @Test
    void handleValidation() {
    }

    @Test
    void processAllocation() {
    }


    @TestConfiguration
    static class RestTemplateBuilderProvider {


        private static final int WIRE_MOCK_SERVER_PORT = 8083;

        @Bean(destroyMethod = "stop") // set destroy method recommended by wiremock documentation
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(WIRE_MOCK_SERVER_PORT));
            server.start();
            return server;
        }

    }

}