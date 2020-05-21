package guru.sfg.beer.order.service.bootstrap;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jt on 2019-06-06.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderBootStrap implements CommandLineRunner {
    public static final String TASTING_ROOM = "Tasting Room";
    public static final String BEER_1_UPC = "018200533082";
    public static final String BEER_2_UPC = "18200007712";
    public static final String BEER_3_UPC = "018200113529";

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        loadCustomerData();
    }

    private void loadCustomerData() {
        if (customerRepository.findAllByCustomerNameLike(BeerOrderBootStrap.TASTING_ROOM).size() == 0) {
            Customer saved = customerRepository.saveAndFlush(Customer.builder()
                    .customerName(TASTING_ROOM)
                    .apiKey(UUID.randomUUID())
                    .build());
            log.debug("Testing Room customer Id: {}", saved.getId().toString());
        }
    }
}
