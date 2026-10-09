package it.ecommerce.service;

import it.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Product add(Product product);

    Page<Product> findAll(Pageable pageable);
}
