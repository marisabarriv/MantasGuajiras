package com.mantasguajiras.backend.inventory.controller;

import com.mantasguajiras.backend.inventory.dto.requests.InventoryRequest;
import com.mantasguajiras.backend.inventory.dto.response.InventoryResponse;
import com.mantasguajiras.backend.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse create(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.create(request);
    }

    @GetMapping
    public List<InventoryResponse> findAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/{id}")
    public InventoryResponse findById(@PathVariable UUID id) {
        return inventoryService.findById(id);
    }

    @PutMapping("/{id}")
    public InventoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryRequest request) {

        return inventoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        inventoryService.delete(id);
    }
}