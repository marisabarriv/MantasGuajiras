package com.mantasguajiras.backend.unit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {

    private Short id;

    private String name;

    private String abbreviation;

    private Boolean active;
}
