package com.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.warehouse.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
