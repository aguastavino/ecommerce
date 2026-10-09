package it.ecommerce.service.impl;

import it.ecommerce.model.Product;
import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.repository.ProductRepository;
import it.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product add(Product product) {
        if (productRepository.existsByCode(product.getCode())) {
            throw new DuplicateResourceException("Product with code " + product.getCode() + " already exists");
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
