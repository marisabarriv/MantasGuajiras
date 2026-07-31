package com.mantasguajiras.backend.unit.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mantasguajiras.backend.unit.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, Short> {

    Optional<Unit> findByNameIgnoreCase(String name);

    Optional<Unit> findByAbbreviationIgnoreCase(String abbreviation);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Short id);

    boolean existsByAbbreviationIgnoreCase(String abbreviation);
}