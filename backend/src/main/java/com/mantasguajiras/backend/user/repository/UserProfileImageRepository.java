package com.mantasguajiras.backend.user.repository;

import com.mantasguajiras.backend.user.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, UUID> {
}
