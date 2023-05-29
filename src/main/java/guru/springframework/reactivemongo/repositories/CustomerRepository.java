package guru.springframework.reactivemongo.repositories;

import guru.springframework.reactivemongo.domain.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    Flux<Customer> findAllByCustomerName(String name);

}
