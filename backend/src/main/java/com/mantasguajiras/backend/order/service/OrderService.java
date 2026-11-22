package com.mantasguajiras.backend.order.service;

import com.mantasguajiras.backend.order.dto.requests.OrderPaymentRequest;
import com.mantasguajiras.backend.order.dto.requests.OrderRequest;
import com.mantasguajiras.backend.order.dto.response.OrderResponse;

import java.util.List;
import java.util.UUID;

/** Mantas personalizadas (encargos con abono). */
public interface OrderService {

    /** status: null = todas, "PENDING" = con saldo, "PAID" = pagadas. */
    List<OrderResponse> findAll(String status);

    OrderResponse findById(UUID id);

    OrderResponse create(OrderRequest request);

    OrderResponse addPayment(UUID id, OrderPaymentRequest request);

    OrderResponse update(UUID id, OrderRequest request);

    void delete(UUID id);
}
