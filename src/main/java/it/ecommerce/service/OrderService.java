package it.ecommerce.service;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Order create(CreateOrderCommand command);

    Page<Order> findAll(Pageable pageable);

    Order handleEvent(Long orderId, OrderEventType event);

    /**
     * Deletes an order that has not been delivered yet, giving the reserved stock back.
     */
    void cancel(Long orderId);
}
