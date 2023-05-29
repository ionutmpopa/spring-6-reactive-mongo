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
public class CustomerRouterConfig {

    private final CustomerHandler customerHandler;

    public static final String CUSTOMER_PATH = "/api/v3/customer";
    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";

    @Bean
    public RouterFunction<ServerResponse> customerRoutes() {
        return route()
            .GET(CUSTOMER_PATH, accept(APPLICATION_JSON), customerHandler::listCustomers)
            .GET(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::getCustomerById)
            .POST(CUSTOMER_PATH, accept(APPLICATION_JSON), customerHandler::createCustomer)
            .PUT(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::updateOrPatchCustomer)
            .PATCH(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::updateOrPatchCustomer)
            .DELETE(CUSTOMER_PATH_ID, accept(APPLICATION_JSON), customerHandler::deleteCustomerById)
            .build();
    }

}
