package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.Customer;
import guru.sfg.beer.order.service.web.model.CustomerDto;
import org.mapstruct.Mapper;

/**
 * @author kas
 */
@Mapper(uses = {BeerOrderMapper.class, DateMapper.class})
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);
    Customer toEntity(CustomerDto customerDto);
}
