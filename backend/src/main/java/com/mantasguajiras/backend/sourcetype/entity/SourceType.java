package com.mantasguajiras.backend.sourcetype.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "source_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}