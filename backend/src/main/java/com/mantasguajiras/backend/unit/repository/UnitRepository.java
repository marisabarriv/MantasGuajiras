package com.mantasguajiras.backend.unit.repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mantasguajiras.backend.unit.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, UUID> {

    Optional<Unit> findByNameIgnoreCase(String name);

    Optional<Unit> findByAbbreviationIgnoreCase(String abbreviation);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    boolean existsByAbbreviationIgnoreCase(String abbreviation);
}