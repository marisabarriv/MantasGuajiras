package com.mantasguajiras.backend.inventorymovement.controller;

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

    @GetMapping
    public List<InventoryMovementResponse> findAll() {
        return inventoryMovementService.findAll();
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

    @PutMapping("/{id}")
    public InventoryMovementResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryMovementRequest request) {

        return inventoryMovementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        inventoryMovementService.delete(id);
    }
}