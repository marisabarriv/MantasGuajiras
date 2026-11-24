package com.mantasguajiras.backend.inventorymovement.controller;

import com.mantasguajiras.backend.inventorymovement.dto.requests.MovementFilter;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-movements")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    /**
     * Historial de movimientos, del más reciente al más antiguo.
     * Filtros opcionales y combinables:
     *   productId, type (IN/OUT), source (SALE, PURCHASE, PRODUCTION, ORDER,
     *   ADJUSTMENT), username, from y to (fechas AAAA-MM-DD, incluidas).
     */
    @GetMapping
    public List<InventoryMovementResponse> findAll(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return inventoryMovementService.search(
                new MovementFilter(productId, type, source, username, from, to));
    }

    @GetMapping("/{id}")
    public InventoryMovementResponse findById(@PathVariable UUID id) {
        return inventoryMovementService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryMovementResponse create(
            @Valid @RequestBody InventoryMovementRequest request) {
        return inventoryMovementService.create(request);
    }
}
