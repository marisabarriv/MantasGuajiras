package com.mantasguajiras.backend.inventory.repository;

import com.mantasguajiras.backend.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    boolean existsByProductId(UUID productId);
}
