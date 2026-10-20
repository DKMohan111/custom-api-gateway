package com.backend.controller;

import com.backend.entity.Product;
import com.backend.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {

        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        productRepository.deleteById(id);

        return ResponseEntity.ok("Product deleted successfully");
    }
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Product not found",
                                "status", 404
                        )));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product updatedProduct) {

        return productRepository.findById(id)
                .map(product -> {
                    product.setName(updatedProduct.getName());
                    product.setPrice(updatedProduct.getPrice());
                    return ResponseEntity.ok(productRepository.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "At least one field must be provided", "status", 400)
            );
        }

        for (String field : updates.keySet()) {
            if (!field.equals("name") && !field.equals("price")) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Unsupported field: " + field, "status", 400)
                );
            }
        }

        if (updates.containsKey("name")) {
            Object name = updates.get("name");

            if (!(name instanceof String) || ((String) name).isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Product name cannot be blank", "status", 400)
                );
            }
        }

        if (updates.containsKey("price")) {
            Object price = updates.get("price");

            if (!(price instanceof Number)
                    || !Double.isFinite(((Number) price).doubleValue())
                    || ((Number) price).doubleValue() < 0) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Price must be a non-negative number", "status", 400)
                );
            }
        }

        return productRepository.findById(id)
                .<ResponseEntity<Object>>map(product -> {
                    if (updates.containsKey("name")) {
                        product.setName((String) updates.get("name"));
                    }

                    if (updates.containsKey("price")) {
                        product.setPrice(((Number) updates.get("price")).doubleValue());
                    }

                    return ResponseEntity.ok(
                            (Object) productRepository.save(product)
                    );
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found", "status", 404)));
    }

}
