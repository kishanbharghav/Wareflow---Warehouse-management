package com.warehouse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.warehouse.repository.ProductRepository;
import com.warehouse.entity.Product;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @GetMapping
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @PostMapping
    public Product addProduct(@RequestBody Product p) {
        return repo.save(p);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        repo.deleteById(id);
    }
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product p) {
        Product existing = repo.findById(id).orElseThrow();
        existing.setName(p.getName());
        existing.setPrice(p.getPrice());
        existing.setQuantity(p.getQuantity());
        return repo.save(existing);
    }

}
