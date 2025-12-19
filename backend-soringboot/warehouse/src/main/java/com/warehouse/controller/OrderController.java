package com.warehouse.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.warehouse.entity.Order;
import com.warehouse.entity.OrderItem;
import com.warehouse.entity.Product;
import com.warehouse.repository.OrderItemRepository;
import com.warehouse.repository.OrderRepository;
import com.warehouse.repository.ProductRepository;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private OrderItemRepository orderItemRepo;

    // =========================
    // CREATE ORDER + ITEMS
    // =========================
    @PostMapping
    public Order createOrder(@RequestBody(required = false) CreateOrderRequest request) {

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        Order savedOrder = orderRepo.save(order);

        // Save order items (NO stock reduction here)
        if (request != null && request.getItems() != null) {
            for (CreateOrderItem item : request.getItems()) {
                Product product = productRepo.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                if (product.getQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Not enough stock for " + product.getName());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(product);
                orderItem.setQuantity(item.getQuantity());
                orderItemRepo.save(orderItem);
            }
        }

        return savedOrder;
    }

    // =========================
    // GET ALL ORDERS
    // =========================
    @GetMapping
    public List<Order> getOrders() {
        return orderRepo.findAll();
    }

    // =========================
    // GET ORDER ITEMS
    // =========================
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderItemRepo.findByOrderId(orderId);
    }

    // =========================
    // UPDATE ORDER STATUS
    // (REDUCE STOCK ON SHIPPED)
    // =========================
    @PutMapping("/{id}/status")
    public Order updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Order order = orderRepo.findById(id).orElseThrow();
        String oldStatus = order.getStatus();

        // Reduce inventory ONLY when moving to SHIPPED
        if ("SHIPPED".equals(status) && "PENDING".equals(oldStatus)) {
            List<OrderItem> items = orderItemRepo.findByOrderId(id);

            for (OrderItem item : items) {
                Product product = item.getProduct();

                if (product.getQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Not enough stock for " + product.getName());
                }

                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepo.save(product);
            }
        }

        order.setStatus(status);
        return orderRepo.save(order);
    }

    // =========================
    // UPDATE ORDER (SAFE)
    // =========================
    @PutMapping("/{id}")
    public Order updateOrder(
            @PathVariable Long id,
            @RequestBody Order updatedOrder) {

        Order order = orderRepo.findById(id).orElseThrow();

        if (updatedOrder.getStatus() != null) {
            order.setStatus(updatedOrder.getStatus());
        }

        return orderRepo.save(order);
    }

    // =========================
    // DELETE ORDER
    // (RESTORE STOCK IF SHIPPED)
    // =========================
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {

        Order order = orderRepo.findById(id).orElseThrow();
        List<OrderItem> items = orderItemRepo.findByOrderId(id);

        if ("SHIPPED".equals(order.getStatus()) || "DELIVERED".equals(order.getStatus())) {
            for (OrderItem item : items) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepo.save(product);
            }
        }

        orderItemRepo.deleteAll(items);
        orderRepo.deleteById(id);
    }
}

/* =========================
   DTO CLASSES
   ========================= */

class CreateOrderRequest {
    private List<CreateOrderItem> items;

    public List<CreateOrderItem> getItems() {
        return items;
    }

    public void setItems(List<CreateOrderItem> items) {
        this.items = items;
    }
}

class CreateOrderItem {
    private Long productId;
    private int quantity;

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
