package com.mantasguajiras.backend.unit.service;

import java.util.List;
import java.util.UUID;
import com.mantasguajiras.backend.unit.dto.requests.UnitRequest;
import com.mantasguajiras.backend.unit.dto.response.UnitResponse;

public interface UnitService {

    UnitResponse create(UnitRequest request);

    List<UnitResponse> findAll();

    UnitResponse findById(UUID id);

    UnitResponse update(UUID id, UnitRequest request);

    void delete(UUID id);
}

