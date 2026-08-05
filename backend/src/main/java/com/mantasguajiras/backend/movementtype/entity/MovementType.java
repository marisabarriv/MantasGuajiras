package com.mantasguajiras.backend.movementtype.entity;

import java.util.UUID;

import com.mantasguajiras.backend.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movement_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}