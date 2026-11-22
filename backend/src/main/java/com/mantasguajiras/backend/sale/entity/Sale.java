package com.mantasguajiras.backend.sale.entity;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sale")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Sale extends AuditableEntity {

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(length = 255)
    private String observations;

    @OneToMany(
    mappedBy = "sale",
    cascade = CascadeType.ALL,
    orphanRemoval = true
    )
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    /** Usuario que registró la venta. NULL en ventas anteriores a V14. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
