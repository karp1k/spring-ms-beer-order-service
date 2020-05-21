package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.web.model.CustomerDto;

import java.util.List;

/**
 * @author kas
 */
public interface CustomerService {

    List<CustomerDto> getAllCustomers();
}
