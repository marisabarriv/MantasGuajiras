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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String abbreviation;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
