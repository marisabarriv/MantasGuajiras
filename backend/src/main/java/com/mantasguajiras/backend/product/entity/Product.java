package com.mantasguajiras.backend.product.entity;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.unit.entity.Unit;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "wholesale_price", precision = 12, scale = 2)
    private BigDecimal wholesalePrice;

    @Builder.Default
    @Column(name = "minimum_wholesale_quantity")
    private Short minimumWholesaleQuantity = 0;

    @Column(name = "minimum_stock", precision = 12, scale = 2)
    private BigDecimal minimumStock;

    @Column(nullable = false)
    private Boolean purchasable;

    @Column(nullable = false)
    private Boolean manufacturable;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "internal_code", nullable = false, unique = true, length = 30)
    private String internalCode;

    @Column(length = 50, unique = true)
    private String barcode;
}