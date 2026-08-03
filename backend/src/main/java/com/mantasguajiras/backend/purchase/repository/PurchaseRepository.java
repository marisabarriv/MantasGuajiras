package com.mantasguajiras.backend.purchase.repository;

import com.mantasguajiras.backend.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
}