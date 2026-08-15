package com.mantasguajiras.backend.unit.service.impl;

import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.unit.dto.requests.UnitRequest;
import com.mantasguajiras.backend.unit.dto.response.UnitResponse;
import com.mantasguajiras.backend.unit.entity.Unit;
import com.mantasguajiras.backend.unit.mapper.UnitMapper;
import com.mantasguajiras.backend.unit.repository.UnitRepository;
import com.mantasguajiras.backend.unit.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;

    @Override
    public UnitResponse create(UnitRequest request) {

        if (unitRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Ya existe una unidad con ese nombre.");
        }

        Unit unit = unitMapper.toEntity(request);

        return unitMapper.toResponse(
                unitRepository.save(unit)
        );
    }

    @Override
    public List<UnitResponse> findAll() {

        return unitRepository.findAll()
                .stream()
                .map(unitMapper::toResponse)
                .toList();
    }

    @Override
    public UnitResponse findById(UUID id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Unidad no encontrada."));

        return unitMapper.toResponse(unit);
    }

    @Override
    public UnitResponse update(UUID id, UnitRequest request) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Unidad no encontrada."));

        if (unitRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Ya existe una unidad con ese nombre.");
        }

        unitMapper.updateEntity(request, unit);

        return unitMapper.toResponse(
                unitRepository.save(unit)
        );
    }

    @Override
    public void delete(UUID id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Unidad no encontrada."));

        unitRepository.delete(unit);
    }
}