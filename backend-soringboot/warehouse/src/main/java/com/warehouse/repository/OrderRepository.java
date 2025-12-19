package com.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.warehouse.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
