package com.mantasguajiras.backend.inventory.controller;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryAdjustmentRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryResponse> findAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/{id}")
    public InventoryResponse findById(@PathVariable UUID id) {
        return inventoryService.findById(id);
    }

    /**
     * Ajuste por conteo (solo ADMIN, ver SecurityConfig).
     */
    @PostMapping("/adjustments")
    public InventoryResponse adjust(
            @Valid @RequestBody InventoryAdjustmentRequest request) {
        return inventoryService.adjust(request);
    }
}
