package com.mantasguajiras.backend.inventorymovement.repository;

import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, UUID>, JpaSpecificationExecutor<InventoryMovement> {

    /** Historial filtrado (ver MovementFilter), cargando en una sola consulta lo que se muestra. */
    @Override
    @EntityGraph(attributePaths = {
            "product", "product.unit", "movementType", "sourceType", "createdBy"
    })
    List<InventoryMovement> findAll(Specification<InventoryMovement> specification, Sort sort);

    List<InventoryMovement> findBySourceId(UUID sourceId);

    boolean existsByProductId(UUID productId);

}
