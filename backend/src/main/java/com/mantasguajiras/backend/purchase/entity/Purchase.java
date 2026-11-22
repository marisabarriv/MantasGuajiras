package com.mantasguajiras.backend.purchase.entity;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase extends AuditableEntity {

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(length = 255)
    private String observations;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<PurchaseItem> items = new java.util.ArrayList<>();

    /** Usuario que registró la compra. NULL en compras anteriores a V15. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
