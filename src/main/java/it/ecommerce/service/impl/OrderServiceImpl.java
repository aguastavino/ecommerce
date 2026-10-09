package it.ecommerce.service.impl;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.exception.InsufficientStockException;
import it.ecommerce.exception.ResourceNotFoundException;
import it.ecommerce.factory.OrderFactory;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;
import it.ecommerce.repository.CustomerRepository;
import it.ecommerce.repository.OrderRepository;
import it.ecommerce.repository.ProductRepository;
import it.ecommerce.service.OrderService;
import it.ecommerce.strategy.StockAvailabilityStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderFactory orderFactory;
    private final StockAvailabilityStrategy stockAvailabilityStrategy;

    @Override
    @Transactional
    public Order create(CreateOrderCommand command) {
        log.info("Creating order for customer {} with {} lines", command.customerId(), command.lines().size());
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> {
                    log.warn("Order rejected: customer {} not found", command.customerId());
                    return new ResourceNotFoundException("Customer", command.customerId());
                });

        Map<Product, Integer> quantities = new LinkedHashMap<>();
        for (CreateOrderCommand.Line line : command.lines()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> {
                        log.warn("Order rejected: product {} not found", line.productId());
                        return new ResourceNotFoundException("Product", line.productId());
                    });
            quantities.merge(product, line.quantity(), Integer::sum);
        }

        reserveStock(quantities);

        Order saved = orderRepository.save(orderFactory.create(customer, quantities));
        log.info("Order {} created for customer {}", saved.getId(), customer.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        log.info("Retrieving all orders");
        List<Order> orders = orderRepository.findAll();
        log.info("Retrieved {} orders", orders.size());
        return orders;
    }

    private void reserveStock(Map<Product, Integer> quantities) {
        quantities.forEach((product, quantity) -> {
            if (!stockAvailabilityStrategy.isAvailable(product, quantity)) {
                log.warn("Order rejected: product {} has stock {}, requested {}",
                        product.getCode(), product.getStock(), quantity);
                throw new InsufficientStockException(product, quantity);
            }
        });
        quantities.forEach((product, quantity) -> {
            product.decreaseStock(quantity);
            log.info("Stock of product {} decreased by {}, remaining {}",
                    product.getCode(), quantity, product.getStock());
        });
    }
}
