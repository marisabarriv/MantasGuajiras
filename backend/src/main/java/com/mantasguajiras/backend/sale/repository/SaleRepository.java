package com.mantasguajiras.backend.sale.repository;

import com.mantasguajiras.backend.sale.entity.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    /**
     * Ventas de un periodo, de la más reciente a la más antigua, con
     * vendedor, productos y unidades en la misma consulta (sin una
     * consulta adicional por cada venta).
     */
    @EntityGraph(attributePaths = {"createdBy", "items", "items.product", "items.product.unit"})
    List<Sale> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime endExclusive);

    /** Venta ya registrada con ese identificador del dispositivo. */
    Optional<Sale> findByClientRequestId(UUID clientRequestId);
}
