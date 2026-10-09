package it.ecommerce.strategy;

import it.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SufficientStockStrategyTest {

    private final SufficientStockStrategy strategy = new SufficientStockStrategy();
    private final Product product = Product.builder().code("P1").stock(10).build();

    @Test
    void availableWhenStockCoversQuantity() {
        assertTrue(strategy.isAvailable(product, 10));
    }

    @Test
    void unavailableWhenQuantityExceedsStock() {
        assertFalse(strategy.isAvailable(product, 11));
    }

    @Test
    void unavailableForNonPositiveQuantity() {
        assertFalse(strategy.isAvailable(product, 0));
    }
}
