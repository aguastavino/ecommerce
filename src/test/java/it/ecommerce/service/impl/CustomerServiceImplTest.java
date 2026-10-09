package it.ecommerce.service.impl;

import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.model.Customer;
import it.ecommerce.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;
    @InjectMocks
    private CustomerServiceImpl service;

    private final Customer customer = Customer.builder().email("a@test.it").taxCode("TAX").build();

    @Test
    void savesNewCustomer() {
        when(repository.save(customer)).thenReturn(customer);

        assertSame(customer, service.add(customer));
    }

    @Test
    void rejectsDuplicateEmail() {
        when(repository.existsByEmail("a@test.it")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.add(customer));
        verify(repository, never()).save(customer);
    }

    @Test
    void rejectsDuplicateTaxCode() {
        when(repository.existsByTaxCode("TAX")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.add(customer));
        verify(repository, never()).save(customer);
    }

    @Test
    void listsCustomers() {
        when(repository.findAll()).thenReturn(List.of(customer));

        assertEquals(1, service.findAll().size());
    }
}
