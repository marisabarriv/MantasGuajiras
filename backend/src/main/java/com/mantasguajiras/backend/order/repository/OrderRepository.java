package com.mantasguajiras.backend.order.repository;

import com.mantasguajiras.backend.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /** Todas, de la más reciente a la más antigua. */
    @EntityGraph(attributePaths = {"createdBy", "fabricProduct"})
    List<Order> findAllByActiveTrueOrderByCreatedAtDesc();

    /**
     * Bloquea la manta mientras se registra un pago, para que dos
     * pagos simultáneos no superen el valor de la manta.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);

    /** Operación ya registrada con ese identificador del dispositivo. */
    Optional<Order> findByClientRequestId(UUID clientRequestId);

    /** La manta personalizada a la que pertenece un pago ya registrado. */
    @Query("select p.order from OrderPayment p where p.clientRequestId = :clientRequestId")
    Optional<Order> findByPaymentClientRequestId(@Param("clientRequestId") UUID clientRequestId);
}
