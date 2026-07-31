package com.mantasguajiras.backend.unit.service;

import java.util.List;

import com.mantasguajiras.backend.unit.dto.requests.UnitRequest;
import com.mantasguajiras.backend.unit.dto.response.UnitResponse;

public interface UnitService {

    UnitResponse create(UnitRequest request);

    List<UnitResponse> findAll();

    UnitResponse findById(Short id);

    UnitResponse update(Short id, UnitRequest request);

    void delete(Short id);
}

