package it.ecommerce.service;

import it.ecommerce.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    Customer add(Customer customer);

    Page<Customer> findAll(Pageable pageable);
}
