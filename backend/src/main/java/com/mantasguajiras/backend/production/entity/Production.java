package com.mantasguajiras.backend.production.entity;

import java.util.ArrayList;
import java.util.List;

import com.mantasguajiras.backend.common.entity.AuditableEntity;
import com.mantasguajiras.backend.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "production")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Production extends AuditableEntity {

    @Column(length = 255)
    private String observations;

    /** Persona que fabricó las mantas (puede no ser usuario del sistema). */
    @Column(name = "maker_name", length = 100)
    private String makerName;

    @OneToMany(
        mappedBy = "production",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<ProductionItem> items = new ArrayList<>();

    /** Usuario que registró la producción. NULL en producciones anteriores a V16. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
