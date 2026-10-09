package it.ecommerce.service.impl;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.exception.InsufficientStockException;
import it.ecommerce.exception.InvalidOrderStateException;
import it.ecommerce.exception.ResourceNotFoundException;
import it.ecommerce.factory.OrderFactory;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderEventType;
import it.ecommerce.model.OrderItem;
import it.ecommerce.model.OrderStatus;
import it.ecommerce.model.Product;
import it.ecommerce.repository.CustomerRepository;
import it.ecommerce.repository.OrderRepository;
import it.ecommerce.repository.ProductRepository;
import it.ecommerce.service.OrderService;
import it.ecommerce.strategy.OrderEventHandler;
import it.ecommerce.strategy.StockAvailabilityStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderFactory orderFactory;
    private final StockAvailabilityStrategy stockAvailabilityStrategy;
    private final Map<OrderEventType, OrderEventHandler> eventHandlers = new EnumMap<>(OrderEventType.class);

    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository,
                            ProductRepository productRepository, OrderFactory orderFactory,
                            StockAvailabilityStrategy stockAvailabilityStrategy,
                            List<OrderEventHandler> handlers) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderFactory = orderFactory;
        this.stockAvailabilityStrategy = stockAvailabilityStrategy;
        handlers.forEach(h -> eventHandlers.put(h.supportedEvent(), h));
    }

    @Override
    @Transactional
    public Order create(CreateOrderCommand command) {
        log.info("Creating order for customer {} with {} lines", command.customerId(), command.lines().size());
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> {
                    log.warn("Order rejected: customer {} not found", command.customerId());
                    return new ResourceNotFoundException("Customer", command.customerId());
                });

        Map<Product, Integer> quantities = lockProducts(command);
        reserveStock(quantities);

        Order saved = orderRepository.save(orderFactory.create(customer, quantities));
        log.info("Order {} created for customer {}", saved.getId(), customer.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAll(Pageable pageable) {
        log.info("Retrieving orders, page {}", pageable);
        Page<Order> orders = orderRepository.findAll(pageable);
        log.info("Retrieved {} orders", orders.getNumberOfElements());
        return orders;
    }

    @Override
    @Transactional
    public Order handleEvent(Long orderId, OrderEventType event) {
        log.info("Handling event {} on order {}", event, orderId);
        Order order = findOrder(orderId);
        try {
            eventHandlers.get(event).handle(order);
        } catch (InvalidOrderStateException ex) {
            log.warn("Event {} rejected for order {}: {}", event, orderId, ex.getMessage());
            throw ex;
        }
        log.info("Order {} is now {}", orderId, order.getStatus());
        return order;
    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        log.info("Cancelling order {}", orderId);
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CONSEGNATO) {
            log.warn("Cancellation rejected: order {} is already delivered", orderId);
            throw new InvalidOrderStateException("Order %d is already delivered and cannot be cancelled".formatted(orderId));
        }
        order.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .forEach(this::restoreStock);
        orderRepository.delete(order);
        log.info("Order {} cancelled", orderId);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order {} not found", orderId);
            return new ResourceNotFoundException("Order", orderId);
        });
    }

    private void restoreStock(OrderItem item) {
        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProduct().getId()));
        product.increaseStock(item.getQuantity());
        log.info("Stock of product {} restored by {}, now {}", product.getCode(), item.getQuantity(), product.getStock());
    }

    /**
     * Locks the products always in ascending id order, which avoids deadlocks between concurrent orders.
     */
    private Map<Product, Integer> lockProducts(CreateOrderCommand command) {
        Map<Long, Integer> requested = new TreeMap<>();
        command.lines().forEach(line -> requested.merge(line.productId(), line.quantity(), Integer::sum));

        Map<Product, Integer> quantities = new LinkedHashMap<>();
        requested.forEach((productId, quantity) -> {
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> {
                        log.warn("Order rejected: product {} not found", productId);
                        return new ResourceNotFoundException("Product", productId);
                    });
            quantities.put(product, quantity);
        });
        return quantities;
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
