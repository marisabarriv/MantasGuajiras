package com.mantasguajiras.backend.unit.controller;

import com.mantasguajiras.backend.unit.dto.requests.UnitRequest;
import com.mantasguajiras.backend.unit.dto.response.UnitResponse;
import com.mantasguajiras.backend.unit.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unit")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnitResponse create(@Valid @RequestBody UnitRequest request) {
        return unitService.create(request);
    }

    @GetMapping
    public List<UnitResponse> findAll() {
        return unitService.findAll();
    }

    @GetMapping("/{id}")
    public UnitResponse findById(@PathVariable Short id) {
        return unitService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UnitResponse update(
            @PathVariable Short id,
            @Valid @RequestBody UnitRequest request) {

        return unitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Short id) {
        unitService.delete(id);
    }
}