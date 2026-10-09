package it.ecommerce.service.impl;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;
import it.ecommerce.exception.ResourceNotFoundException;
import it.ecommerce.factory.OrderFactory;
import it.ecommerce.repository.CustomerRepository;
import it.ecommerce.repository.OrderRepository;
import it.ecommerce.repository.ProductRepository;
import it.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderFactory orderFactory;

    @Override
    @Transactional
    public Order create(CreateOrderCommand command) {
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", command.customerId()));

        Map<Product, Integer> quantities = new LinkedHashMap<>();
        for (CreateOrderCommand.Line line : command.lines()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", line.productId()));
            quantities.merge(product, line.quantity(), Integer::sum);
        }

        return orderRepository.save(orderFactory.create(customer, quantities));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
