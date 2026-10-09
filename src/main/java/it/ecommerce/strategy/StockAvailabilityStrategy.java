package it.ecommerce.strategy;

import it.ecommerce.model.Product;

/**
 * Strategy deciding whether a requested quantity of a product can be fulfilled.
 */
public interface StockAvailabilityStrategy {

    boolean isAvailable(Product product, int requestedQuantity);
}
