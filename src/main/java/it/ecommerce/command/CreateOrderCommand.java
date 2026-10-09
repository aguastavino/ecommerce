package it.ecommerce.command;

import java.util.List;

/**
 * Command object describing the intent of creating an order, decoupled from the entities and from any transport layer.
 */
public record CreateOrderCommand(Long customerId, List<Line> lines) {

    public record Line(Long productId, int quantity) {
    }
}
