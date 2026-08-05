package com.mantasguajiras.backend.inventorymovement.entity;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_movement")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movement_type_id", nullable = false)
    private MovementType movementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_type_id", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(length = 255)
    private String observations;
}