package com.mantasguajiras.backend.sale.service;

import java.util.List;
import java.util.UUID;

import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.dto.response.SaleResponse;

public interface SaleService {

    SaleResponse create(SaleRequest saleRequest);

    SaleResponse update(UUID id, SaleRequest saleRequest);

    SaleResponse findById(UUID id);

    List<SaleResponse> findAll();

    void delete(UUID id);

}

