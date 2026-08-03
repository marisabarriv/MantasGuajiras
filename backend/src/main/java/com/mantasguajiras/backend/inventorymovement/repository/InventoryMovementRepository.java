package com.mantasguajiras.backend.inventorymovement.repository;

import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {
}