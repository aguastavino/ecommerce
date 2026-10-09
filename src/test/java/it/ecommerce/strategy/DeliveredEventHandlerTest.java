package it.ecommerce.strategy;

import it.ecommerce.exception.InvalidOrderStateException;
import it.ecommerce.model.Order;
import it.ecommerce.model.OrderEventType;
import it.ecommerce.model.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliveredEventHandlerTest {

    private final DeliveredEventHandler handler = new DeliveredEventHandler();

    @Test
    void supportsDeliveredEvent() {
        assertEquals(OrderEventType.DELIVERED, handler.supportedEvent());
    }

    @Test
    void deliversOrderedOrder() {
        Order order = Order.builder().id(1L).build();

        handler.handle(order);

        assertEquals(OrderStatus.CONSEGNATO, order.getStatus());
    }

    @Test
    void rejectsAlreadyDeliveredOrder() {
        Order order = Order.builder().id(1L).status(OrderStatus.CONSEGNATO).build();

        assertThrows(InvalidOrderStateException.class, () -> handler.handle(order));
    }
}
