package it.ecommerce.facade;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.exception.InsufficientStockException;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Product;
import it.ecommerce.repository.CustomerRepository;
import it.ecommerce.repository.OrderRepository;
import it.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Not transactional on purpose: every thread must run its own transaction.
 */
@SpringBootTest
class ConcurrentOrdersTest {

    private static final int STOCK = 10;
    private static final int REQUESTS = 30;

    @Autowired
    private ECommerceFacade facade;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void neverSellsMoreThanTheAvailableStock() throws Exception {
        Customer customer = facade.addCustomer(Customer.builder().firstName("Mario").lastName("Rossi")
                .birthDate(LocalDate.of(1990, 1, 1)).taxCode("RSSMRA90A01H501A").email("m@test.it").build());
        Product product = facade.addProduct(Product.builder().code("P1").name("Mouse").stock(STOCK).build());
        CreateOrderCommand command = new CreateOrderCommand(customer.getId(),
                List.of(new CreateOrderCommand.Line(product.getId(), 1)));

        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < REQUESTS; i++) {
            Callable<Boolean> task = () -> {
                try {
                    facade.createOrder(command);
                    return true;
                } catch (InsufficientStockException ex) {
                    return false;
                }
            };
            results.add(executor.submit(task));
        }
        long accepted = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                accepted++;
            }
        }
        executor.shutdown();

        assertEquals(STOCK, accepted);
        assertEquals(0, productRepository.findById(product.getId()).orElseThrow().getStock());
        assertEquals(STOCK, orderRepository.count());
    }
}
