package it.ecommerce.adapter;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.dto.CustomerRequest;
import it.ecommerce.dto.CustomerResponse;
import it.ecommerce.dto.OrderLineRequest;
import it.ecommerce.dto.OrderRequest;
import it.ecommerce.dto.OrderResponse;
import it.ecommerce.dto.ProductRequest;
import it.ecommerce.dto.ProductResponse;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderItem;
import it.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdaptersTest {

    @Test
    void customerAdapterMapsBothWaysAndNormalizesTaxCode() {
        CustomerAdapter adapter = new CustomerAdapter();

        Customer customer = adapter.toEntity(new CustomerRequest("Mario", "Rossi",
                LocalDate.of(1990, 1, 1), "rssmra90a01h501a", "m@test.it"));
        customer.setId(7L);
        CustomerResponse response = adapter.toResponse(customer);

        assertEquals("RSSMRA90A01H501A", response.taxCode());
        assertEquals(7L, response.id());
        assertEquals("m@test.it", response.email());
    }

    @Test
    void productAdapterMapsBothWays() {
        ProductAdapter adapter = new ProductAdapter();

        Product product = adapter.toEntity(new ProductRequest("P1", "Mouse", 10));
        product.setId(3L);
        ProductResponse response = adapter.toResponse(product);

        assertEquals(new ProductResponse(3L, "P1", "Mouse", 10), response);
    }

    @Test
    void orderAdapterMapsRequestToCommandAndOrderToResponse() {
        OrderAdapter adapter = new OrderAdapter();

        CreateOrderCommand command = adapter.toCommand(new OrderRequest(1L, List.of(new OrderLineRequest(2L, 3))));
        assertEquals(1L, command.customerId());
        assertEquals(new CreateOrderCommand.Line(2L, 3), command.lines().get(0));

        Order order = Order.builder().id(9L).customer(Customer.builder().id(1L).build())
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0)).build();
        order.addItem(OrderItem.builder().product(Product.builder().id(2L).code("P2").build()).quantity(3).build());
        OrderResponse response = adapter.toResponse(order);

        assertEquals(9L, response.id());
        assertEquals(1L, response.customerId());
        assertEquals("P2", response.items().get(0).productCode());
        assertEquals(3, response.items().get(0).quantity());
    }
}
