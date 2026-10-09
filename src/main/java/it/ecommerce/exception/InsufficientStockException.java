package it.ecommerce.exception;

import it.ecommerce.model.Product;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Product product, int requested) {
        super("Product %s has insufficient stock: requested %d, available %d"
                .formatted(product.getCode(), requested, product.getStock()));
    }
}
