package com.mantasguajiras.backend.sale.entity;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

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
}