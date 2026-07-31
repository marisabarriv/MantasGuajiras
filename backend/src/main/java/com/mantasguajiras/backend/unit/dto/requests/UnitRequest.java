package com.mantasguajiras.backend.unit.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class UnitRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "La abreviatura es obligatoria.")
    @Size(max = 10)
    private String abbreviation;

    @NotNull(message = "Debe indicar si la unidad está activa.")
    private Boolean active;
}
