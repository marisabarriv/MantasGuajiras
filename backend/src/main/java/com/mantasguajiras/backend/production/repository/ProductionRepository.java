package com.mantasguajiras.backend.production.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mantasguajiras.backend.production.entity.Production;

@Repository
public interface ProductionRepository extends JpaRepository<Production, UUID> {

    /** Historial de producciones, de la más reciente a la más antigua. */
    @EntityGraph(attributePaths = {"createdBy"})
    List<Production> findAllByOrderByCreatedAtDesc();
}
