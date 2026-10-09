package it.ecommerce.service;

import it.ecommerce.model.Product;

import java.util.List;

public interface ProductService {

    Product add(Product product);

    List<Product> findAll();
}
