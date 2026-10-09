package it.ecommerce.service;

import it.ecommerce.model.Customer;

import java.util.List;

public interface CustomerService {

    Customer add(Customer customer);

    List<Customer> findAll();
}
