package it.ecommerce.service.impl;

import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.model.Customer;
import it.ecommerce.repository.CustomerRepository;
import it.ecommerce.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer add(Customer customer) {
        log.info("Adding customer with email {}", customer.getEmail());
        if (customerRepository.existsByEmail(customer.getEmail())) {
            log.warn("Customer creation rejected: email {} already exists", customer.getEmail());
            throw new DuplicateResourceException("Customer with email " + customer.getEmail() + " already exists");
        }
        if (customerRepository.existsByTaxCode(customer.getTaxCode())) {
            log.warn("Customer creation rejected: tax code {} already exists", customer.getTaxCode());
            throw new DuplicateResourceException("Customer with tax code " + customer.getTaxCode() + " already exists");
        }
        Customer saved = customerRepository.save(customer);
        log.info("Customer added with id {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        log.info("Retrieving all customers");
        List<Customer> customers = customerRepository.findAll();
        log.info("Retrieved {} customers", customers.size());
        return customers;
    }
}
