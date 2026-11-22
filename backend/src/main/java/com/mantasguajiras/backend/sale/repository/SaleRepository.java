package com.mantasguajiras.backend.sale.repository;

import com.mantasguajiras.backend.sale.entity.Sale;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    /** Historial de ventas, de la más reciente a la más antigua. */
    @EntityGraph(attributePaths = {"createdBy"})
    List<Sale> findAllByOrderByCreatedAtDesc();
}
