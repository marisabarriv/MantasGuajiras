package com.mantasguajiras.backend.sale.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.dto.response.SaleResponse;

public interface SaleService {

    SaleResponse create(SaleRequest saleRequest);

    SaleResponse update(UUID id, SaleRequest saleRequest);

    SaleResponse findById(UUID id);

    /**
     * Historial de un periodo (ver {@link com.mantasguajiras.backend.common.query.DateRange}):
     * sin fechas, los últimos 30 días; máximo 93 días.
     */
    List<SaleResponse> findAll(LocalDate from, LocalDate to);

    void delete(UUID id);

}

