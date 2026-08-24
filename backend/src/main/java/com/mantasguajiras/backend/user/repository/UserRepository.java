package com.mantasguajiras.backend.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);

    long countByRole(Role role);
}
