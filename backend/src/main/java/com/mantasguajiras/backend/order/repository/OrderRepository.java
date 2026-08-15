package com.mantasguajiras.backend.order.repository;

import com.mantasguajiras.backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByActiveTrue();
}