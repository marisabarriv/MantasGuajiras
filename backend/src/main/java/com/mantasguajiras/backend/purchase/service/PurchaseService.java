package com.mantasguajiras.backend.purchase.service;

import com.mantasguajiras.backend.purchase.dto.requests.PurchaseRequest;
import com.mantasguajiras.backend.purchase.dto.response.PurchaseResponse;

import java.util.List;
import java.util.UUID;

public interface PurchaseService {

    List<PurchaseResponse> findAll();

    PurchaseResponse findById(UUID id);

    PurchaseResponse create(PurchaseRequest request);

    PurchaseResponse update(UUID id, PurchaseRequest request);

    void delete(UUID id);
}