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
import it.ecommerce.strategy.StockAvailabilityStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderFactory orderFactory;
    @Mock
    private StockAvailabilityStrategy strategy;

    private OrderServiceImpl service;
    private final Customer customer = Customer.builder().id(1L).build();
    private final Product product = Product.builder().id(2L).code("P1").stock(10).build();

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, customerRepository, productRepository, orderFactory, strategy);
    }

    @Test
    void createsOrderAndDecreasesStockWithMergedLines() {
        Order order = new Order();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(strategy.isAvailable(product, 5)).thenReturn(true);
        when(orderFactory.create(any(), any())).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = service.create(new CreateOrderCommand(1L, List.of(
                new CreateOrderCommand.Line(2L, 2), new CreateOrderCommand.Line(2L, 3))));

        assertSame(order, result);
        assertEquals(5, product.getStock());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Product, Integer>> captor = ArgumentCaptor.forClass(Map.class);
        verify(orderFactory).create(any(), captor.capture());
        assertEquals(5, captor.getValue().get(product));
    }

    @Test
    void rejectsOrderWhenStockIsInsufficient() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(strategy.isAvailable(product, 11)).thenReturn(false);

        assertThrows(InsufficientStockException.class,
                () -> service.create(new CreateOrderCommand(1L, List.of(new CreateOrderCommand.Line(2L, 11)))));

        assertEquals(10, product.getStock());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void failsWhenCustomerIsUnknown() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(new CreateOrderCommand(1L, List.of(new CreateOrderCommand.Line(2L, 1)))));
    }

    @Test
    void failsWhenProductIsUnknown() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(new CreateOrderCommand(1L, List.of(new CreateOrderCommand.Line(2L, 1)))));
    }

    @Test
    void listsOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(new Order()));

        assertEquals(1, service.findAll().size());
    }
}
