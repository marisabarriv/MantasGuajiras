package com.mantasguajiras.backend.movementtype.repository;

import com.mantasguajiras.backend.movementtype.entity.MovementType;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementTypeRepository extends JpaRepository<MovementType, UUID> {
    Optional<MovementType> findByName(String name);
}