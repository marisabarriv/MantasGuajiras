package com.mantasguajiras.backend.order.entity;

import java.util.UUID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Manta personalizada: encargo de un cliente con diseño y abono.
 * Gasta tela del inventario; la manta va directo al cliente
 * (no pasa por el inventario de mantas).
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends AuditableEntity {

    /** Nombre para identificar la manta (ej.: "Manta wayuu azul de Ana"). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Opcional. */
    @Column(name = "client_name", length = 100)
    private String clientName;

    /** Valor acordado de la manta. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** Suma de los pagos recibidos (abonos). */
    @Builder.Default
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabric_product_id")
    private Product fabricProduct;

    @Column(name = "fabric_quantity", precision = 12, scale = 2)
    private BigDecimal fabricQuantity;

    @Column(length = 255)
    private String observations;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<OrderPayment> payments = new ArrayList<>();

    /** Lo que falta por pagar. */
    public BigDecimal getBalance() {
        return total.subtract(paidAmount);
    }

    public boolean isPaid() {
        return paidAmount.compareTo(total) >= 0;
    }

    /**
     * Identificador que genera el dispositivo para esta operación. Si llega
     * dos veces (reintento tras un corte de conexión), no se repite.
     */
    @Column(name = "client_request_id", unique = true)
    private UUID clientRequestId;
}
