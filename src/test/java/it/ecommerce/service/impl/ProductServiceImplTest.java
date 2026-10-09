package it.ecommerce.service.impl;

import it.ecommerce.exception.DuplicateResourceException;
import it.ecommerce.model.Product;
import it.ecommerce.repository.ProductRepository;
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
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;
    @InjectMocks
    private ProductServiceImpl service;

    private final Product product = Product.builder().code("P1").stock(5).build();

    @Test
    void savesNewProduct() {
        when(repository.save(product)).thenReturn(product);

        assertSame(product, service.add(product));
    }

    @Test
    void rejectsDuplicateCode() {
        when(repository.existsByCode("P1")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.add(product));
        verify(repository, never()).save(product);
    }

    @Test
    void listsProducts() {
        when(repository.findAll()).thenReturn(List.of(product));

        assertEquals(1, service.findAll().size());
    }
}
