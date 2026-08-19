package com.mantasguajiras.backend.production.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mantasguajiras.backend.production.entity.Production;

@Repository
public interface ProductionRepository extends JpaRepository<Production, UUID> {
}