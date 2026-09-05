package com.mantasguajiras.backend.config;
import com.mantasguajiras.backend.unit.entity.Unit;
import com.mantasguajiras.backend.unit.repository.UnitRepository;
import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.productcategory.repository.ProductCategoryRepository;
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
    private final ProductCategoryRepository productCategoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UnitRepository unitRepository;

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

        createProductCategories();
        createUnits();
        createInitialAdmin();
    }

    // =========================================
    // CATEGORÍAS DE PRODUCTOS
    // =========================================

    private void createProductCategories() {

        createProductCategory(
                "Tela",
                "Tela disponible para producción.",
                1
        );

        createProductCategory(
                "Manta",
                "Mantas disponibles para venta.",
                2
        );
    }

    private void createProductCategory(
            String name,
            String description,
            int displayOrder) {

        if (
                productCategoryRepository
                        .existsByNameIgnoreCase(name)
        ) {
            return;
        }

        productCategoryRepository.save(
                ProductCategory.builder()
                        .name(name)
                        .description(description)
                        .displayOrder(displayOrder)
                        .active(true)
                        .build()
        );
    }

    // =========================================
    // ADMINISTRADOR INICIAL
    // =========================================

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

        if (
                initialAdminUsername == null
                        || initialAdminUsername.isBlank()
        ) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_USERNAME no está configurado."
            );
        }

        if (
                initialAdminPhone == null
                        || initialAdminPhone.isBlank()
        ) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_PHONE no está configurado."
            );
        }

        if (
                initialAdminPassword == null
                        || initialAdminPassword.isBlank()
        ) {
            throw new IllegalStateException(
                    "INITIAL_ADMIN_PASSWORD no está configurado."
            );
        }
    }

    private void validateInitialAdminUniqueFields() {

        if (
                userRepository
                        .findByUsername(initialAdminUsername)
                        .isPresent()
        ) {
            throw new IllegalStateException(
                    "El username del administrador inicial "
                            + "ya está registrado: "
                            + initialAdminUsername
            );
        }

        if (
                userRepository
                        .findByPhone(initialAdminPhone)
                        .isPresent()
        ) {
            throw new IllegalStateException(
                    "El teléfono del administrador inicial "
                            + "ya está registrado."
            );
        }
    }

    // =========================================
    // TIPOS DE MOVIMIENTO
    // =========================================

    private void createMovementType(
            String name,
            String description) {

        if (
                movementTypeRepository
                        .findByName(name)
                        .isEmpty()
        ) {
            movementTypeRepository.save(
                    MovementType.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build()
            );
        }
    }

    // =========================================
    // TIPOS DE ORIGEN
    // =========================================

    private void createSourceType(
            String name,
            String description) {

        if (
                sourceTypeRepository
                        .findByName(name)
                        .isEmpty()
        ) {
            sourceTypeRepository.save(
                    SourceType.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build()
            );
        }
    }

    private void createUnits() {

    createUnit(
            "Metro",
            "m"
    );

    createUnit(
            "Unidad",
            "#"
    );
}

private void createUnit(
        String name,
        String abbreviation) {

    if (
            unitRepository
                    .existsByAbbreviationIgnoreCase(abbreviation)
    ) {
        return;
    }

    unitRepository.save(
            Unit.builder()
                    .name(name)
                    .abbreviation(abbreviation)
                    .active(true)
                    .build()
    );
}
}
