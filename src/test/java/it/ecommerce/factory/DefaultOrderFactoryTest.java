package it.ecommerce.factory;

import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultOrderFactoryTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    private final DefaultOrderFactory factory = new DefaultOrderFactory(clock);

    @Test
    void buildsOrderWithItemsAndCreationDate() {
        Customer customer = Customer.builder().id(1L).build();
        Product p1 = Product.builder().id(1L).code("P1").build();
        Product p2 = Product.builder().id(2L).code("P2").build();
        Map<Product, Integer> quantities = new LinkedHashMap<>();
        quantities.put(p1, 2);
        quantities.put(p2, 5);

        Order order = factory.create(customer, quantities);

        assertSame(customer, order.getCustomer());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), order.getCreatedAt());
        assertEquals(2, order.getItems().size());
        assertEquals(5, order.getItems().get(1).getQuantity());
        assertSame(order, order.getItems().get(0).getOrder());
    }
}
