package com.mantasguajiras.backend.order.service;

import com.mantasguajiras.backend.order.dto.requests.OrderRequest;
import com.mantasguajiras.backend.order.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<OrderResponse> findAll();

    OrderResponse findById(UUID id);

    OrderResponse create(OrderRequest request);

    OrderResponse update(UUID id, OrderRequest request);

    void delete(UUID id);
}