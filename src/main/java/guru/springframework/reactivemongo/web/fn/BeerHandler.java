package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.model.BeerDTO;
import guru.springframework.reactivemongo.services.BeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import static guru.springframework.reactivemongo.web.fn.BeerRouterConfig.BEER_PATH;
import static guru.springframework.reactivemongo.web.fn.BeerRouterConfig.URL;
import static org.springframework.http.HttpHeaders.LOCATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandler {

    public static final String BEER_ID = "beerId";
    private final BeerService beerService;
    private final Validator validator;

    private void validate(BeerDTO beerDTO){
        Errors errors = new BeanPropertyBindingResult(beerDTO, "beerDto");
        validator.validate(beerDTO, errors);

        if (errors.hasErrors()){
            throw new ServerWebInputException(errors.toString());
        }
    }

    public Mono<ServerResponse> listBeers(ServerRequest serverRequest) {
        return ServerResponse.ok()
            .body(beerService.listBeers(), BeerDTO.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest serverRequest) {
        return ServerResponse.ok()
            .body(beerService.getBeerById(serverRequest.pathVariable(BEER_ID)), BeerDTO.class);
    }

    public Mono<ServerResponse> deleteBeerById(ServerRequest serverRequest) {
        return beerService.deleteBeerById(serverRequest.pathVariable(BEER_ID))
            .then(Mono.defer(() -> ServerResponse.noContent().build()));

    }

    public Mono<ServerResponse> createBeer(ServerRequest serverRequest) {
        return serverRequest
            .bodyToMono(BeerDTO.class)
            .doOnNext(this::validate)
            .flatMap(beerDTO ->
                beerService.saveBeer(beerDTO)
                    .flatMap(savedBeerDTO ->
                        ServerResponse
                            .ok()
                            .headers(httpHeaders -> httpHeaders.add(LOCATION, URL + BEER_PATH + "/" + savedBeerDTO.getId()))
                            .body(Mono.just(savedBeerDTO), BeerDTO.class)));
    }

    public Mono<ServerResponse> updateOrPatchBeer(ServerRequest serverRequest) {
        return serverRequest
            .bodyToMono(BeerDTO.class)
            .doOnNext(this::validate)
            .flatMap(beerDTO -> {
                Mono<BeerDTO> beerDTOMono = Mono.empty();
                if (serverRequest.method() == HttpMethod.PATCH) {
                    beerDTOMono = beerService.patchBeer(serverRequest.pathVariable(BEER_ID), beerDTO);
                } else if (serverRequest.method() == HttpMethod.PUT) {
                    beerDTOMono = beerService.updateBeer(serverRequest.pathVariable(BEER_ID), beerDTO);
                }
                return beerDTOMono
                    .flatMap(savedBeerDTO ->
                        ServerResponse
                            .ok()
                            .headers(httpHeaders -> httpHeaders.add(LOCATION, URL + BEER_PATH + "/" + savedBeerDTO.getId()))
                            .body(Mono.just(savedBeerDTO), BeerDTO.class));
            });
    }

}
