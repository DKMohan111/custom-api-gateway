
package com.backend.controller;

import com.backend.entity.Product;
import com.backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @Test
    void patchProduct_updatesValidName() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Old Name");
        product.setPrice(100.0);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ResponseEntity<Object> response =
                productController.patchProduct(
                        1L, Map.of("name", "New Name"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Name", product.getName());
        verify(productRepository).save(product);
    }

    @Test
    void patchProduct_rejectsNonStringName() {
        ResponseEntity<Object> response =
                productController.patchProduct(
                        1L, Map.of("name", 123));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchProduct_rejectsBlankName() {
        ResponseEntity<Object> response =
                productController.patchProduct(
                        1L, Map.of("name", "   "));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchProduct_rejectsNonNumericPrice() {
        ResponseEntity<Object> response =
                productController.patchProduct(
                        1L, Map.of("price", "not-a-number"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchProduct_rejectsNegativePrice() {
        ResponseEntity<Object> response =
                productController.patchProduct(
                        1L, Map.of("price", -10));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchProduct_rejectsUnsupportedField() {
        ResponseEntity<Object> response =
                productController.patchProduct(
                        1L, Map.of("description", "Test"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchProduct_rejectsEmptyUpdates() {
        ResponseEntity<Object> response =
                productController.patchProduct(1L, Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void patchProduct_returnsNotFoundForMissingProduct() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseEntity<Object> response =
                productController.patchProduct(
                        99L, Map.of("name", "New Name"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productRepository, never()).save(any());
    }
}
