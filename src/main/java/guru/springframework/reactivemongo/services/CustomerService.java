package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.model.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Flux<CustomerDTO> findAllByCustomerName(String name);

    Flux<CustomerDTO> listCustomers();

    Mono<CustomerDTO> saveCustomer(CustomerDTO customerDTO);
    Mono<CustomerDTO> getCustomerById(String customerId);

    Mono<CustomerDTO> updateCustomer(String customerId, CustomerDTO customerDTO);

    Mono<CustomerDTO> patchCustomer(String customerId, CustomerDTO customerDTO);

    Mono<Void> deleteCustomerById(String customerId);

}
