package guru.springframework.reactivemongo.web.fn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class BeerRouterConfig {

    private final BeerHandler beerHandler;

    public static final String BEER_PATH = "/api/v3/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    public static final String URL = "http://localhost:8080";

    @Bean
    public RouterFunction<ServerResponse> beerRoutes() {
        return route()
            .GET(BEER_PATH, accept(APPLICATION_JSON), beerHandler::listBeers)
            .GET(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::getBeerById)
            .POST(BEER_PATH, accept(APPLICATION_JSON), beerHandler::createBeer)
            .PUT(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::updateOrPatchBeer)
            .PATCH(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::updateOrPatchBeer)
            .DELETE(BEER_PATH_ID, accept(APPLICATION_JSON), beerHandler::deleteBeerById)
            .build();
    }

}
