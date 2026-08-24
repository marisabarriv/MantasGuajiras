package com.mantasguajiras.backend.config;

import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MovementTypeRepository movementTypeRepository;
    private final SourceTypeRepository sourceTypeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${initial-admin.username}")
    private String initialAdminUsername;

    @Value("${initial-admin.phone}")
    private String initialAdminPhone;

    @Value("${initial-admin.password}")
    private String initialAdminPassword;

    @Override
    public void run(String... args) {

        createMovementType(
                "IN",
                "Entrada de inventario."
        );

        createMovementType(
                "OUT",
                "Salida de inventario."
        );

        createMovementType(
                "ADJUSTMENT",
                "Ajuste de inventario."
        );

        createSourceType(
                "PURCHASE",
                "Movimiento generado por una compra."
        );

        createSourceType(
                "SALE",
                "Movimiento generado por una venta."
        );

        createSourceType(
                "PRODUCTION",
                "Movimiento generado por producción."
        );

        createSourceType(
                "ORDER",
                "Movimiento generado por un pedido."
        );

        createSourceType(
                "ADJUSTMENT",
                "Movimiento generado por un ajuste de inventario."
        );

        createInitialAdmin();
    }

    private void createInitialAdmin() {

        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }

        validateInitialAdminData();

        validateInitialAdminUniqueFields();

        User admin = User.builder()
                .username(initialAdminUsername)
                .phone(initialAdminPhone)
                .password(
                        passwordEncoder.encode(initialAdminPassword)
                )
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
    }

    private void validateInitialAdminData() {

        if (initialAdminUsername == null
                || initialAdminUsername.isBlank()) {

            throw new IllegalStateException(
                    "INITIAL_ADMIN_USERNAME no está configurado."
            );
        }

        if (initialAdminPhone == null
                || initialAdminPhone.isBlank()) {

            throw new IllegalStateException(
                    "INITIAL_ADMIN_PHONE no está configurado."
            );
        }

        if (initialAdminPassword == null
                || initialAdminPassword.isBlank()) {

            throw new IllegalStateException(
                    "INITIAL_ADMIN_PASSWORD no está configurado."
            );
        }
    }

    private void validateInitialAdminUniqueFields() {

        if (userRepository.findByUsername(initialAdminUsername)
                .isPresent()) {

            throw new IllegalStateException(
                    "El username del administrador inicial "
                            + "ya está registrado: "
                            + initialAdminUsername
            );
        }

        if (userRepository.findByPhone(initialAdminPhone)
                .isPresent()) {

            throw new IllegalStateException(
                    "El teléfono del administrador inicial "
                            + "ya está registrado."
            );
        }
    }

    private void createMovementType(
            String name,
            String description) {

        if (movementTypeRepository.findByName(name).isEmpty()) {

            movementTypeRepository.save(
                    MovementType.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build()
            );
        }
    }

    private void createSourceType(
            String name,
            String description) {

        if (sourceTypeRepository.findByName(name).isEmpty()) {

            sourceTypeRepository.save(
                    SourceType.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build()
            );
        }
    }
}