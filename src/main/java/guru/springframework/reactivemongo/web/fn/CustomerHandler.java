package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.services.CustomerService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static guru.springframework.reactivemongo.web.fn.BeerRouterConfig.URL;
import static guru.springframework.reactivemongo.web.fn.CustomerRouterConfig.CUSTOMER_PATH;
import static org.springframework.http.HttpHeaders.LOCATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerHandler {

    public static final String CUSTOMER_ID = "customerId";
    private final CustomerService customerService;
    private final Validator validator;

    private void validate(CustomerDTO customerDTO) {
        Errors errors = new BeanPropertyBindingResult(customerDTO, "customerDto");
        validator.validate(customerDTO, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    public Mono<ServerResponse> listCustomers(ServerRequest serverRequest) {

        Flux<CustomerDTO> customerDTOFlux;

        if (serverRequest.queryParam("customerName").isPresent()) {
            customerDTOFlux = customerService.findAllByCustomerName(serverRequest.queryParam("customerName").get());
        } else {
            customerDTOFlux = customerService.listCustomers();
        }

        return ServerResponse.ok()
            .body(customerDTOFlux, CustomerDTO.class);
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest serverRequest) {
        return ServerResponse.ok()
            .body(customerService.getCustomerById(serverRequest.pathVariable(CUSTOMER_ID)), CustomerDTO.class);
    }

    public Mono<ServerResponse> deleteCustomerById(ServerRequest serverRequest) {
        return customerService.deleteCustomerById(serverRequest.pathVariable(CUSTOMER_ID))
            .then(Mono.defer(() -> ServerResponse.noContent().build()));

    }

    public Mono<ServerResponse> createCustomer(ServerRequest serverRequest) {
        return serverRequest
            .bodyToMono(CustomerDTO.class)
            .doOnNext(this::validate)
            .flatMap(customerDTO ->
                customerService.saveCustomer(customerDTO)
                    .flatMap(savedCustomerDTO ->
                        ServerResponse
                            .ok()
                            .headers(httpHeaders -> httpHeaders.add(LOCATION, URL + CUSTOMER_PATH + "/" + savedCustomerDTO.getId()))
                            .body(Mono.just(savedCustomerDTO), CustomerDTO.class)));
    }

    public Mono<ServerResponse> updateOrPatchCustomer(ServerRequest serverRequest) {
        return serverRequest
            .bodyToMono(CustomerDTO.class)
            .doOnNext(this::validate)
            .flatMap(customerDTO -> {
                Mono<CustomerDTO> customerDTOMono = Mono.empty();
                if (serverRequest.method() == HttpMethod.PATCH) {
                    customerDTOMono = customerService.patchCustomer(serverRequest.pathVariable(CUSTOMER_ID), customerDTO);
                } else if (serverRequest.method() == HttpMethod.PUT) {
                    customerDTOMono = customerService.updateCustomer(serverRequest.pathVariable(CUSTOMER_ID), customerDTO);
                }
                return customerDTOMono
                    .flatMap(savedCustomerDTO ->
                        ServerResponse
                            .ok()
                            .headers(httpHeaders -> httpHeaders.add(LOCATION, URL + CUSTOMER_PATH + "/" + savedCustomerDTO.getId()))
                            .body(Mono.just(savedCustomerDTO), CustomerDTO.class));
            });
    }

}
