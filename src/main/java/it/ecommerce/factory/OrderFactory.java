package it.ecommerce.factory;

import it.ecommerce.model.Customer;
import it.ecommerce.model.Order;
import it.ecommerce.model.Product;

import java.util.Map;

public interface OrderFactory {

    /**
     * @param productQuantities requested quantity for each product
     */
    Order create(Customer customer, Map<Product, Integer> productQuantities);
}
