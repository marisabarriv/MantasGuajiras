package com.mantasguajiras.backend.user.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.security.dto.requests.RegisterRequest;
import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.dto.requests.UserRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.mapper.UserMapper;
import com.mantasguajiras.backend.user.repository.UserRepository;
import com.mantasguajiras.backend.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;

        @Override
        public UserResponse create(UserRequest request) {

                validateUniqueFields(
                                request.getUsername(),
                                request.getPhone(),
                                null);

                User user = userMapper.toEntity(request);

                user.setRole(Role.SELLER);

                user.setPassword(
                                passwordEncoder.encode(request.getPassword()));

                return userMapper.toResponse(
                                userRepository.save(user));
        }

        @Override
        @Transactional(readOnly = true)
        public UserResponse findById(UUID id) {

                User user = findEntity(id);

                return userMapper.toResponse(user);
        }

        @Override
        public UserResponse update(
                        UUID id,
                        UpdateUserRequest request) {

                User user = findEntity(id);

                validateUniqueFields(
                                request.getUsername(),
                                request.getPhone(),
                                id);

                userMapper.updateEntity(request, user);

                return userMapper.toResponse(
                                userRepository.save(user));
        }

        @Override
        public void changePassword(
                        UUID id,
                        ChangePasswordRequest request) {

                User user = findEntity(id);

                if (!passwordEncoder.matches(
                                request.getCurrentPassword(),
                                user.getPassword())) {

                        throw new BusinessException(
                                        "La contraseña actual es incorrecta.");
                }

                user.setPassword(
                                passwordEncoder.encode(
                                                request.getNewPassword()));

                userRepository.save(user);
        }

        @Override
        public void delete(UUID id) {

                User user = findEntity(id);

                if (user.getRole() == Role.ADMIN) {

                        long adminCount = userRepository.countByRole(Role.ADMIN);

                        if (adminCount <= 1) {
                                throw new BusinessException(
                                                "No se puede eliminar al último administrador del sistema.");
                        }
                }

                userRepository.delete(user);
        }

        private User findEntity(UUID id) {

                return userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado."));
        }

        private void validateUniqueFields(
                        String username,
                        String phone,
                        UUID currentId) {

                userRepository.findByUsername(username)
                                .filter(user -> currentId == null
                                                || !user.getId().equals(currentId))
                                .ifPresent(user -> {
                                        throw new DuplicateResourceException(
                                                        "El nombre de usuario ya está registrado.");
                                });

                userRepository.findByPhone(phone)
                                .filter(user -> currentId == null
                                                || !user.getId().equals(currentId))
                                .ifPresent(user -> {
                                        throw new DuplicateResourceException(
                                                        "El número de teléfono ya está registrado.");
                                });
        }

        @Override
        public UserResponse register(RegisterRequest request) {

                validateUniqueFields(
                                request.getUsername(),
                                request.getPhone(),
                                null);

                User user = User.builder()
                                .username(request.getUsername())
                                .phone(request.getPhone())
                                .password(
                                                passwordEncoder.encode(
                                                                request.getPassword()))
                                .role(Role.SELLER)
                                .active(true)
                                .build();

                return userMapper.toResponse(
                                userRepository.save(user));
        }

        @Override
        @Transactional(readOnly = true)
        public List<UserResponse> findAll() {

                return userRepository.findAll()
                                .stream()
                                .map(userMapper::toResponse)
                                .toList();
        }

        @Override
        public UserResponse promoteToAdmin(UUID id) {

                User user = findEntity(id);

                if (user.getRole() == Role.ADMIN) {
                        throw new BusinessException(
                                        "El usuario ya es administrador.");
                }

                user.setRole(Role.ADMIN);

                return userMapper.toResponse(
                                userRepository.save(user));
        }
}
