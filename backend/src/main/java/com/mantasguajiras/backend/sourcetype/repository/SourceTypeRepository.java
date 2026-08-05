package com.mantasguajiras.backend.sourcetype.repository;

import com.mantasguajiras.backend.sourcetype.entity.SourceType;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceTypeRepository extends JpaRepository<SourceType, UUID> {

    Optional<SourceType> findByName(String name);

}