package com.mantasguajiras.backend.sale.entity;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * Identificador que genera el dispositivo para esta venta. Evita
     * registrarla dos veces si se reenvía (ventas sin conexión).
     */
    @Column(name = "client_request_id", unique = true)
    private UUID clientRequestId;

    /** Hora real de la venta si se hizo sin conexión; NULL si se registró en línea. */
    @Column(name = "offline_created_at")
    private LocalDateTime offlineCreatedAt;
}
