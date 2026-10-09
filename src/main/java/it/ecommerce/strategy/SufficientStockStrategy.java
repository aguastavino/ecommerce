package it.ecommerce.strategy;

import it.ecommerce.model.Product;
import org.springframework.stereotype.Component;

/**
 * An order line is fulfillable when the current stock covers the requested quantity.
 */
@Component
public class SufficientStockStrategy implements StockAvailabilityStrategy {

    @Override
    public boolean isAvailable(Product product, int requestedQuantity) {
        return requestedQuantity > 0 && product.getStock() >= requestedQuantity;
    }
}
