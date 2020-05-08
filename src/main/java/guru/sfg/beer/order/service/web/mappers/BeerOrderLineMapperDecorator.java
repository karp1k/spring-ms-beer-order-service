package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.ms.beer.BeerService;
import guru.sfg.beer.order.service.web.model.BeerOrderLineDto;
import guru.springframework.springmsbeercommon.web.model.BeerDto ;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author kas
 */
public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {

    private BeerOrderLineMapper mapper;
    private BeerService beerService;

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine entity) {
        Optional<BeerDto> beerDtoOp = beerService.getBeerDtoByUpc(entity.getUpc());
        BeerOrderLineDto beerOrderLineDto = mapper.beerOrderLineToDto(entity);
        beerDtoOp.ifPresent(beerDto -> {
            beerOrderLineDto.setBeerId(beerDto.getId());
            beerOrderLineDto.setBeerName(beerDto.getBeerName());
            beerOrderLineDto.setBeerStyle(String.valueOf(beerDto.getBeerStyle()));
            beerOrderLineDto.setPrice(beerDto.getPrice());
        });
        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return mapper.dtoToBeerOrderLine(dto);
    }

    @Autowired
    public void setMapper(BeerOrderLineMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }
}
