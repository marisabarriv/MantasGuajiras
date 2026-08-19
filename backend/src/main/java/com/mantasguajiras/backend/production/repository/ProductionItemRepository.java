package com.mantasguajiras.backend.production.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mantasguajiras.backend.production.entity.ProductionItem;

@Repository
public interface ProductionItemRepository extends JpaRepository<ProductionItem, UUID> {
}