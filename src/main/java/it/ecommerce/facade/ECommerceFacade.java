package it.ecommerce.facade;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderEventType;
import it.ecommerce.model.Product;
import it.ecommerce.service.CustomerService;
import it.ecommerce.service.OrderService;
import it.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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

    public Page<Customer> getCustomers(Pageable pageable) {
        return customerService.findAll(pageable);
    }

    public Product addProduct(Product product) {
        return productService.add(product);
    }

    public Page<Product> getProducts(Pageable pageable) {
        return productService.findAll(pageable);
    }

    public Order createOrder(CreateOrderCommand command) {
        return orderService.create(command);
    }

    public Page<Order> getOrders(Pageable pageable) {
        return orderService.findAll(pageable);
    }

    public Order handleOrderEvent(Long orderId, OrderEventType event) {
        return orderService.handleEvent(orderId, event);
    }

    public void cancelOrder(Long orderId) {
        orderService.cancel(orderId);
    }
}
