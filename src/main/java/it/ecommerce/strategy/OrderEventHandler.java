package it.ecommerce.strategy;

import it.ecommerce.model.Order;
import it.ecommerce.model.OrderEventType;

/**
 * Strategy applying a single type of event to an order.
 */
public interface OrderEventHandler {

    OrderEventType supportedEvent();

    void handle(Order order);
}
