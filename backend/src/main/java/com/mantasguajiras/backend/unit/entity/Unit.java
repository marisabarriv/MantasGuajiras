package com.mantasguajiras.backend.unit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    private Short id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false, length = 10)
    private String abbreviation;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
