package it.ecommerce.facade;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;
import it.ecommerce.service.CustomerService;
import it.ecommerce.service.OrderService;
import it.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Single entry point of the E-Commerce system (Facade over the customer, product and order services).
 */
@Component
@RequiredArgsConstructor
public class ECommerceFacade {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;

    public Customer addCustomer(Customer customer) {
        return customerService.add(customer);
    }

    public List<Customer> getCustomers() {
        return customerService.findAll();
    }

    public Product addProduct(Product product) {
        return productService.add(product);
    }

    public List<Product> getProducts() {
        return productService.findAll();
    }

    public Order createOrder(CreateOrderCommand command) {
        return orderService.create(command);
    }

    public List<Order> getOrders() {
        return orderService.findAll();
    }
}
