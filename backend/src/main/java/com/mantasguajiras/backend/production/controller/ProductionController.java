package com.mantasguajiras.backend.production.controller;

import com.mantasguajiras.backend.production.dto.requests.ProductionRequest;
import com.mantasguajiras.backend.production.dto.response.ProductionResponse;
import com.mantasguajiras.backend.production.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productions")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @PostMapping
    public ResponseEntity<ProductionResponse> create(
            @Valid @RequestBody ProductionRequest productionRequest) {

        ProductionResponse response =
                productionService.create(productionRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductionResponse>> findAll() {

        return ResponseEntity.ok(
                productionService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductionResponse> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                productionService.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionRequest productionRequest) {

        ProductionResponse response =
                productionService.update(id, productionRequest);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {

        productionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}