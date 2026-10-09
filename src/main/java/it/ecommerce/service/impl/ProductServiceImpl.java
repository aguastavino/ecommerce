package it.ecommerce.service.impl;

import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.model.Product;
import it.ecommerce.repository.ProductRepository;
import it.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product add(Product product) {
        log.info("Adding product with code {}", product.getCode());
        if (productRepository.existsByCode(product.getCode())) {
            log.warn("Product creation rejected: code {} already exists", product.getCode());
            throw new DuplicateResourceException("Product with code " + product.getCode() + " already exists");
        }
        Product saved = productRepository.save(product);
        log.info("Product added with id {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        log.info("Retrieving all products");
        List<Product> products = productRepository.findAll();
        log.info("Retrieved {} products", products.size());
        return products;
    }
}
