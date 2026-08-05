package com.mantasguajiras.backend.unit.entity;

import java.util.UUID;
import lombok.experimental.SuperBuilder;
import com.mantasguajiras.backend.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unit")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Unit extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String abbreviation;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
