package it.ecommerce.service.impl;

import it.ecommerce.model.Customer;
import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.repository.CustomerRepository;
import it.ecommerce.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer add(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("Customer with email " + customer.getEmail() + " already exists");
        }
        if (customerRepository.existsByTaxCode(customer.getTaxCode())) {
            throw new DuplicateResourceException("Customer with tax code " + customer.getTaxCode() + " already exists");
        }
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
