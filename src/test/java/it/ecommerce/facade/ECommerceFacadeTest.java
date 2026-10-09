package it.ecommerce.facade;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;
import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ECommerceFacadeTest {

    @Autowired
    private ECommerceFacade facade;

    private Customer customer(String email, String taxCode) {
        return Customer.builder().firstName("Mario").lastName("Rossi")
                .birthDate(LocalDate.of(1990, 1, 1)).taxCode(taxCode).email(email).build();
    }

    private Product product(String code) {
        return Product.builder().code(code).name("Product " + code).stock(10).build();
    }

    @Test
    void addsAndListsCustomers() {
        Customer saved = facade.addCustomer(customer("a@test.it", "RSSMRA90A01H501A"));

        assertNotNull(saved.getId());
        assertEquals(1, facade.getCustomers().size());
    }

    @Test
    void rejectsDuplicateCustomerEmail() {
        facade.addCustomer(customer("dup@test.it", "RSSMRA90A01H501B"));

        assertThrows(DuplicateResourceException.class,
                () -> facade.addCustomer(customer("dup@test.it", "RSSMRA90A01H501C")));
    }

    @Test
    void addsAndListsProducts() {
        facade.addProduct(product("P1"));

        assertEquals(1, facade.getProducts().size());
        assertThrows(DuplicateResourceException.class, () -> facade.addProduct(product("P1")));
    }

    @Test
    void createsOrderForCustomer() {
        Customer c = facade.addCustomer(customer("o@test.it", "RSSMRA90A01H501D"));
        Product p1 = facade.addProduct(product("P1"));
        Product p2 = facade.addProduct(product("P2"));

        Order order = facade.createOrder(new CreateOrderCommand(c.getId(), List.of(
                new CreateOrderCommand.Line(p1.getId(), 2),
                new CreateOrderCommand.Line(p2.getId(), 3))));

        assertNotNull(order.getId());
        assertEquals(2, order.getItems().size());
        assertEquals(1, facade.getOrders().size());
    }

    @Test
    void failsOrderForUnknownCustomer() {
        Product p = facade.addProduct(product("P1"));

        assertThrows(ResourceNotFoundException.class, () -> facade.createOrder(
                new CreateOrderCommand(999L, List.of(new CreateOrderCommand.Line(p.getId(), 1)))));
    }
}
