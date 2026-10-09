package it.ecommerce.controller;

import it.ecommerce.facade.ECommerceFacade;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ECommerceFacade facade;

    private Customer customer() {
        return facade.addCustomer(Customer.builder().firstName("Mario").lastName("Rossi")
                .birthDate(LocalDate.of(1990, 1, 1)).taxCode("RSSMRA90A01H501A").email("m@test.it").build());
    }

    private Product product(int stock) {
        return facade.addProduct(Product.builder().code("P1").name("Mouse").stock(stock).build());
    }

    private String orderJson(Long customerId, Long productId, int quantity) {
        return """
                {"customerId": %d, "items": [{"productId": %d, "quantity": %d}]}
                """.formatted(customerId, productId, quantity);
    }

    @Test
    void createsOrderAndDecreasesStock() throws Exception {
        Customer c = customer();
        Product p = product(10);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(c.getId(), p.getId(), 4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].quantity").value(4));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stock").value(6));
    }

    @Test
    void rejectsOrderExceedingStock() throws Exception {
        Customer c = customer();
        Product p = product(10);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(c.getId(), p.getId(), 11)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/orders"))
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/products"))
                .andExpect(jsonPath("$[0].stock").value(10));
    }

    @Test
    void rejectsInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(1L, 1L, 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownCustomer() throws Exception {
        Product p = product(10);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(999L, p.getId(), 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addsCustomerThroughApi() throws Exception {
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"Anna","lastName":"Bianchi","birthDate":"1985-05-05",
                         "taxCode":"BNCNNA85E45H501B","email":"anna@test.it"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }
}
