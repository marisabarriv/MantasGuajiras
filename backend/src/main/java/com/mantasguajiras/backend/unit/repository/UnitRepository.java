package com.mantasguajiras.backend.unit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mantasguajiras.backend.unit.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, Short> {

    List<Unit> findByActiveTrueOrderByNameAsc();

}