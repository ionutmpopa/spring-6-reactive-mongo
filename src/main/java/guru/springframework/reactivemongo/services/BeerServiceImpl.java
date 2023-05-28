package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.exception.NotFoundException;
import guru.springframework.reactivemongo.mappers.BeerMapper;
import guru.springframework.reactivemongo.model.BeerDTO;
import guru.springframework.reactivemongo.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerServiceImpl implements BeerService {
    public static final String NOT_FOUND = "Element not found";

    private final BeerRepository beerRepository;

    private final BeerMapper beerMapper;

    @Override
    public Mono<BeerDTO> saveBeer(BeerDTO beerDTO) {
        return beerRepository.save(beerMapper.beerDtoToBeer(beerDTO))
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Flux<BeerDTO> listBeers() {
        return beerRepository.findAll()
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDTO> findFirstByBeerName(String name) {
        return beerRepository.findFirstByBeerName(name)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(beerMapper::beerToBeerDto);
    }

    @Override
    public Flux<BeerDTO> findAllByBeerStyle(String style) {
        return beerRepository.findAllByBeerStyle(style)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(beerMapper::beerToBeerDto);

    }

    @Override
    public Mono<BeerDTO> updateBeer(String beerId, BeerDTO beerDTO) {
        return beerRepository.findById(beerId)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(beerMapper::beerToBeerDto)
            .flatMap(beerDTO1 -> {
                beerDTO1.setBeerName(beerDTO.getBeerName());
                beerDTO1.setBeerStyle(beerDTO.getBeerStyle());
                beerDTO1.setUpc(beerDTO.getUpc());
                beerDTO1.setQuantityOnHand(beerDTO.getQuantityOnHand());
                beerDTO1.setPrice(beerDTO.getPrice());
                return beerRepository.save(beerMapper.beerDtoToBeer(beerDTO1)).map(beerMapper::beerToBeerDto);
            });
    }

    @Override
    public Mono<BeerDTO> patchBeer(String beerId, BeerDTO beerDTO) {
        return beerRepository.findById(beerId)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(beerMapper::beerToBeerDto)
            .flatMap(beerDTO1 -> {
                if (StringUtils.hasText(beerDTO.getBeerName())) {
                    beerDTO1.setBeerName(beerDTO.getBeerName());
                }
                if (StringUtils.hasText(beerDTO.getBeerStyle())) {
                    beerDTO1.setBeerStyle(beerDTO.getBeerStyle());
                }
                if (StringUtils.hasText(beerDTO.getUpc())) {
                    beerDTO1.setUpc(beerDTO.getUpc());
                }
                if (beerDTO.getQuantityOnHand() != null) {
                    beerDTO1.setQuantityOnHand(beerDTO.getQuantityOnHand());
                }
                if (beerDTO.getPrice() != null) {
                    beerDTO1.setPrice(beerDTO.getPrice());
                }
                return beerRepository.save(beerMapper.beerDtoToBeer(beerDTO1)).map(beerMapper::beerToBeerDto);
            });
    }

    @Override
    public Mono<Void> deleteBeerById(String beerId) {
        return beerRepository.existsById(beerId)
            .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable.getMessage())))
            .flatMap(exists -> {
                if (Boolean.TRUE.equals(exists)) {
                    return beerRepository.deleteById(beerId);
                } else {
                    return Mono.error(new NotFoundException(NOT_FOUND));
                }
            });
    }

    @Override
    public Mono<BeerDTO> getBeerById(String beerId) {
        return beerRepository.findById(beerId)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(beerMapper::beerToBeerDto);
    }
}
