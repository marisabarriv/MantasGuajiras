package com.mantasguajiras.backend.inventory.repository;

import com.mantasguajiras.backend.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    boolean existsByProductId(UUID productId);

    /**
     * Lee el inventario de un producto bloqueando la fila hasta que
     * termine la transacción. Evita que dos operaciones simultáneas
     * (p. ej. dos ventas) lean la misma cantidad y descuenten de más.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.id = :productId")
    Optional<Inventory> findByIdForUpdate(@Param("productId") UUID productId);
}
