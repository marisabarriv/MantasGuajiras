package com.mantasguajiras.backend.inventorymovement.repository;

import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findBySourceId(UUID sourceId);

    // Historial: del más reciente al más antiguo, cargando en una
    // sola consulta lo que muestra la pantalla (producto, unidad,
    // tipo, origen y usuario).

    @EntityGraph(attributePaths = {
            "product", "product.unit", "movementType", "sourceType", "createdBy"
    })
    List<InventoryMovement> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {
            "product", "product.unit", "movementType", "sourceType", "createdBy"
    })
    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
