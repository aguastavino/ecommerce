package it.ecommerce.adapter;

import it.ecommerce.command.CreateOrderCommand;
import it.ecommerce.dto.OrderItemResponse;
import it.ecommerce.dto.OrderRequest;
import it.ecommerce.dto.OrderResponse;
import it.ecommerce.model.Order;
import org.springframework.stereotype.Component;

/**
 * Adapts the REST representation of an order to the domain command / model and back.
 */
@Component
public class OrderAdapter {

    public CreateOrderCommand toCommand(OrderRequest request) {
        return new CreateOrderCommand(request.customerId(), request.items().stream()
                .map(i -> new CreateOrderCommand.Line(i.productId(), i.quantity()))
                .toList());
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getCreatedAt(), order.getStatus(),
                order.getItems().stream()
                        .map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProduct().getCode(), i.getQuantity()))
                        .toList());
    }
}
