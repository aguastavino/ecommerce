package it.ecommerce.strategy;

import it.ecommerce.exception.InvalidOrderStateException;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderEventType;
import it.ecommerce.model.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class DeliveredEventHandler implements OrderEventHandler {

    @Override
    public OrderEventType supportedEvent() {
        return OrderEventType.DELIVERED;
    }

    @Override
    public void handle(Order order) {
        if (order.getStatus() != OrderStatus.ORDINATO) {
            throw new InvalidOrderStateException(
                    "Order %d cannot be delivered from status %s".formatted(order.getId(), order.getStatus()));
        }
        order.setStatus(OrderStatus.CONSEGNATO);
    }
}
