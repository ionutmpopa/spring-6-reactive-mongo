package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.exception.NotFoundException;
import guru.springframework.reactivemongo.mappers.CustomerMapper;
import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    public static final String NOT_FOUND = "Element not found";

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;


    @Override
    public Flux<CustomerDTO> findAllByCustomerName(String name) {
        return customerRepository.findAllByCustomerName(name)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Flux<CustomerDTO> listCustomers() {
        return customerRepository.findAll()
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> saveCustomer(CustomerDTO customerDTO) {
        return customerRepository.save(customerMapper.customerDtoToCustomer(customerDTO))
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> getCustomerById(String customerId) {
        return customerRepository.findById(customerId)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Mono<CustomerDTO> updateCustomer(String customerId, CustomerDTO customerDTO) {
        return customerRepository.findById(customerId)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .map(customerMapper::customerToCustomerDto)
            .flatMap(customerDTO1 -> {
                customerDTO1.setCustomerName(customerDTO.getCustomerName());
                return customerRepository.save(customerMapper.customerDtoToCustomer(customerDTO1))
                    .map(customerMapper::customerToCustomerDto);
            });
    }

    @Override
    public Mono<CustomerDTO> patchCustomer(String customerId, CustomerDTO customerDTO) {
        return customerRepository.findById(customerId)
            .map(customerMapper::customerToCustomerDto)
            .switchIfEmpty(Mono.error(new NotFoundException(NOT_FOUND)))
            .flatMap(customerDTO1 -> {
                if (StringUtils.hasText(customerDTO.getCustomerName())) {
                    customerDTO1.setCustomerName(customerDTO.getCustomerName());
                }
                return customerRepository.save(customerMapper.customerDtoToCustomer(customerDTO1))
                    .map(customerMapper::customerToCustomerDto);
            });
    }

    @Override
    public Mono<Void> deleteCustomerById(String customerId) {
        return customerRepository.existsById(customerId)
            .onErrorResume(throwable -> Mono.error(new RuntimeException(throwable.getMessage())))
            .flatMap(exists -> {
                if (Boolean.TRUE.equals(exists)) {
                    return customerRepository.deleteById(customerId);
                } else {
                    return Mono.error(new NotFoundException(NOT_FOUND));
                }
            });
    }
}
