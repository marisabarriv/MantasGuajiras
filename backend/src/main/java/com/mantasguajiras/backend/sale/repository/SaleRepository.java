package com.mantasguajiras.backend.sale.repository;
import com.mantasguajiras.backend.sale.entity.Sale;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    
}
