package com.mantasguajiras.backend.productcategory.entity;

import java.util.UUID;

import com.mantasguajiras.backend.common.entity.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "product_category")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private UUID displayOrder = UUID.randomUUID();

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

}