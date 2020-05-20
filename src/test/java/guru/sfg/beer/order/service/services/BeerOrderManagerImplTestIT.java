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
import guru.springframework.springmsbeercommon.beerorder.domain.BeerOrderStatusEnum;
import guru.springframework.springmsbeercommon.web.config.Constants;
import guru.springframework.springmsbeercommon.web.events.AllocationFailureMessage;
import guru.springframework.springmsbeercommon.web.events.DeallocateOrderRequest;
import guru.springframework.springmsbeercommon.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

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

    @Autowired
    JmsTemplate jmsTemplate;

    Customer customer;

    BeerOrder beerOrder;

    BeerDto beerDto;

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
                .customerRef("")
                .customer(customer)
                .build();

        BeerOrderLine beerOrderLine = BeerOrderLine.builder()
                .beerId(beerId)
                .upc(upc)
                .orderQuantity(10)
                .beerOrder(beerOrder)
                .build();

        beerOrder.setBeerOrderLines(new HashSet<>(Arrays.asList(beerOrderLine)));

        beerDto = BeerDto.builder()
                .id(beerId)
                .upc(upc)
                .build();

        try {
            wireMockServer.stubFor(get(BeerServiceImpl.BEER_UPC_PATH + upc).willReturn(okJson(objectMapper.writeValueAsString(beerDto))));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Test
    void newBeerOrder() {

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        //Thread.sleep(5000); // to fast processing stoping thread
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertNotNull(foundOrder);
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        BeerOrder allocatedBeerOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());

        allocatedBeerOrder.getBeerOrderLines().forEach(beerOrderLine ->
                assertEquals(beerOrderLine.getOrderQuantity(), beerOrderLine.getQuantityAllocated()));

    }

    @Test
    void testFailedValidation() {
        // attribute to set up transition to VALIDATION_EXCEPTION state
        // todo switch to mock bean
        beerOrder.setCustomerRef("fail-validation");


        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });


    }

    @Test
    void testFailedAllocation() {
        // todo switch to mock bean
        beerOrder.setCustomerRef("error-allocation");


        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });

    }

    @Test
    void testPartialAllocation() {
        // todo switch to mock bean
        beerOrder.setCustomerRef("partial-allocation");


        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus());
            foundOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                assertEquals(beerOrderLine.getOrderQuantity() - 1, beerOrderLine.getQuantityAllocated());
            });
        });
    }

    @Test
    void testFailedAllocationCompensatingTransaction() {
        // todo switch to mock bean
        beerOrder.setCustomerRef("error-allocation");


        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });

        AllocationFailureMessage allocationFailureMessage = (AllocationFailureMessage) jmsTemplate
                .receiveAndConvert(Constants.ALLOCATE_ORDER_FAILURE_QUEUE);
        assertNotNull(allocationFailureMessage);
    }

    @Test
    void newToPickedUp() {

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testCancelingFromValidationPending() {
        // todo switch to mock bean
        beerOrder.setCustomerRef("validation-cancel-order");
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.VALIDATION_PENDING, foundOrder.getOrderStatus());
        });
        beerOrderManager.cancelOrder(savedBeerOrder.getId());
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testCancelingFromAllocationPending() {
        // todo switch to mock bean
        beerOrder.setCustomerRef("allocation-cancel-order");
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, foundOrder.getOrderStatus());
        });
        beerOrderManager.cancelOrder(savedBeerOrder.getId());
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testCancelAllocatedOrder() {
        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        beerOrderManager.cancelOrder(savedBeerOrder.getId());
        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findOneById(savedBeerOrder.getId());
            assertEquals(BeerOrderStatusEnum.CANCELED, foundOrder.getOrderStatus());
        });

        DeallocateOrderRequest deallocateOrderRequest = (DeallocateOrderRequest) jmsTemplate
                .receiveAndConvert(Constants.DEALLOCATE_ORDER_QUEUE);
        assertNotNull(deallocateOrderRequest);

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