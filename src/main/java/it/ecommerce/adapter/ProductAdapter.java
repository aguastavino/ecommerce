package it.ecommerce.adapter;

import it.ecommerce.dto.ProductRequest;
import it.ecommerce.dto.ProductResponse;
import it.ecommerce.model.Product;
import org.springframework.stereotype.Component;

/**
 * Adapts the REST representation of a product to the domain model and back.
 */
@Component
public class ProductAdapter {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .code(request.code())
                .name(request.name())
                .stock(request.stock())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getCode(), product.getName(), product.getStock());
    }
}
