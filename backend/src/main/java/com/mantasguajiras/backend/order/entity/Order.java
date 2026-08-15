package com.mantasguajiras.backend.order.entity;

import java.math.BigDecimal;

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
@Table(name = "orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AuditableEntity {

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(length = 255)
    private String observations;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
