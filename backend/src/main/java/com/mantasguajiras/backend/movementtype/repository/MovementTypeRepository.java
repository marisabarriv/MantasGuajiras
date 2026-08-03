package com.mantasguajiras.backend.movementtype.repository;

import com.mantasguajiras.backend.movementtype.entity.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementTypeRepository extends JpaRepository<MovementType, Short> {
}