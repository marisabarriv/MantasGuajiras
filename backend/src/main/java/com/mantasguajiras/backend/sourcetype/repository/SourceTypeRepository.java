package com.mantasguajiras.backend.sourcetype.repository;

import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceTypeRepository extends JpaRepository<SourceType, Short> {
}