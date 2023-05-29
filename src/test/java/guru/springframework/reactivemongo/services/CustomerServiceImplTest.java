package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.domain.Customer;
import guru.springframework.reactivemongo.exception.NotFoundException;
import guru.springframework.reactivemongo.mappers.CustomerMapper;
import guru.springframework.reactivemongo.mappers.CustomerMapperImpl;
import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class CustomerServiceImplTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    @Autowired
    CustomerRepository customerRepository;

    CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = customerMapper.customerToCustomerDto(getTestCustomer());
    }

    @Test
    @DisplayName("Test Save Customer Using Subscriber")
    void saveCustomerUseSubscriber() {

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();

        Mono<CustomerDTO> savedMono = customerService.saveCustomer(customerDTO);

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
            atomicBoolean.set(true);
            atomicDto.set(savedDto);
        });

        await().untilTrue(atomicBoolean);

        CustomerDTO persistedDto = atomicDto.get();
        assertThat(persistedDto).isNotNull();
        assertThat(persistedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Save Customer Using Block")
    void testSaveCustomerUseBlock() {
        CustomerDTO savedDto = customerService.saveCustomer(getTestCustomerDto()).block();
        assertThat(savedDto).isNotNull();
        assertThat(savedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Update Customer Using Block")
    void testUpdateBlocking() {
        final String newName = "New Customer Name";  // use final so cannot mutate
        CustomerDTO savedCustomerDto = getSavedCustomerDto();
        savedCustomerDto.setCustomerName(newName);

        CustomerDTO updatedDto = customerService.saveCustomer(savedCustomerDto).block();

        //verify exists in db
        CustomerDTO fetchedDto = customerService.getCustomerById(updatedDto.getId()).block();
        assertThat(fetchedDto.getCustomerName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Test Update Using Reactive Streams")
    void testUpdateStreaming() {
        final String newName = "New Customer Name";  // use final so cannot mutate

        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();

        customerService.saveCustomer(getTestCustomerDto())
                .map(savedCustomerDto -> {
                    savedCustomerDto.setCustomerName(newName);
                    return savedCustomerDto;
                })
                .flatMap(customerService::saveCustomer) // save updated customer
                .flatMap(savedUpdatedDto -> customerService.getCustomerById(savedUpdatedDto.getId())) // get from db
                .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getCustomerName()).isEqualTo(newName);
    }

    @Test
    void findAllByCustomerName() {
        CustomerDTO customerDTO1 = getSavedCustomerDto();

        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();

        customerService.findAllByCustomerName(customerDTO1.getCustomerName())
            .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getCustomerName()).isEqualTo("Sherlock Holmes");
    }


    @Test
    void testDeleteCustomer() {
        CustomerDTO customerToDelete = getSavedCustomerDto();

        customerService.deleteCustomerById(customerToDelete.getId()).block();

        assertThrows(NotFoundException.class, () -> customerService.getCustomerById(customerToDelete.getId()).block());

    }

    public CustomerDTO getSavedCustomerDto(){
        return customerService.saveCustomer(getTestCustomerDto()).block();
    }

    public static CustomerDTO getTestCustomerDto(){
        return new CustomerMapperImpl().customerToCustomerDto(getTestCustomer());
    }

    public static Customer getTestCustomer() {
        return Customer.builder()
                .customerName("Sherlock Holmes")
                .build();
    }
}
