package it.ecommerce.controller;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.facade.ECommerceFacade;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderLifecycleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ECommerceFacade facade;

    private Order order;

    @BeforeEach
    void setUp() {
        Customer customer = facade.addCustomer(Customer.builder().firstName("Mario").lastName("Rossi")
                .birthDate(LocalDate.of(1990, 1, 1)).taxCode("RSSMRA90A01H501A").email("m@test.it").build());
        Product product = facade.addProduct(Product.builder().code("P1").name("Mouse").stock(10).build());
        order = facade.createOrder(new CreateOrderCommand(customer.getId(),
                List.of(new CreateOrderCommand.Line(product.getId(), 4))));
    }

    private static final String DELIVERED = "{\"type\": \"DELIVERED\"}";

    @Test
    void newOrderIsOrdinato() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(jsonPath("$.content[0].status").value("ORDINATO"));
    }

    @Test
    void deliveredEventUpdatesStatus() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/events", order.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(DELIVERED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONSEGNATO"));
    }

    @Test
    void secondDeliveredEventIsConflict() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/events", order.getId())
                .contentType(MediaType.APPLICATION_JSON).content(DELIVERED));

        mockMvc.perform(post("/api/orders/{id}/events", order.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(DELIVERED))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelRemovesOrderAndRestoresStock() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", order.getId())).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders")).andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/products")).andExpect(jsonPath("$.content[0].stock").value(10));
    }

    @Test
    void deliveredOrderCannotBeCancelled() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/events", order.getId())
                .contentType(MediaType.APPLICATION_JSON).content(DELIVERED));

        mockMvc.perform(delete("/api/orders/{id}", order.getId())).andExpect(status().isConflict());
    }

    @Test
    void cancelUnknownOrderIsNotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 999L)).andExpect(status().isNotFound());
    }

    @Test
    void listsArePaginated() throws Exception {
        for (int i = 0; i < 3; i++) {
            facade.addProduct(Product.builder().code("X" + i).name("Item").stock(1).build());
        }

        mockMvc.perform(get("/api/products").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}
