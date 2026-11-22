package com.mantasguajiras.backend.order.controller;

import com.mantasguajiras.backend.order.dto.requests.OrderPaymentRequest;
import com.mantasguajiras.backend.order.dto.requests.OrderRequest;
import com.mantasguajiras.backend.order.dto.response.OrderResponse;
import com.mantasguajiras.backend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Mantas personalizadas. Acceso para TODOS los roles, valores
 * incluidos (decisión del negocio; ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** ?status=PENDING (con saldo) | PAID (pagadas). Sin status: todas. */
    @GetMapping
    public List<OrderResponse> findAll(
            @RequestParam(required = false) String status) {
        return orderService.findAll(status);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    /** Registrar un pago (abono o saldo) de una manta con saldo pendiente. */
    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse addPayment(
            @PathVariable UUID id,
            @Valid @RequestBody OrderPaymentRequest request) {
        return orderService.addPayment(id, request);
    }

    @PutMapping("/{id}")
    public OrderResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody OrderRequest request) {
        return orderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        orderService.delete(id);
    }
}
