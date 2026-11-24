package com.mantasguajiras.backend.support;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.product.service.ProductService;
import com.mantasguajiras.backend.productcategory.repository.ProductCategoryRepository;
import com.mantasguajiras.backend.unit.repository.UnitRepository;
import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.repository.UserRepository;

/**
 * Base de las pruebas de integración: base de datos de PRUEBA (perfil
 * test), sesión de administrador y ayudantes para crear productos.
 * Cada prueba crea sus propios datos con nombres únicos, así no dependen
 * del orden de ejecución.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    protected static final String ADMIN = "admintest";

    @Autowired protected ProductService productService;
    @Autowired protected InventoryRepository inventoryRepository;
    @Autowired protected ProductCategoryRepository categoryRepository;
    @Autowired protected UnitRepository unitRepository;
    @Autowired protected UserRepository userRepository;

    @BeforeEach
    void loginAsAdmin() {
        loginAs(ADMIN, "ADMIN");
    }

    @AfterEach
    void logout() {
        SecurityContextHolder.clearContext();
    }

    protected static void loginAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    protected static String unique(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Vendedor nuevo y activo (para probar quién hizo cada operación). */
    protected String newSeller() {
        String username = "v" + UUID.randomUUID().toString().substring(0, 8);
        userRepository.save(User.builder()
                .username(username)
                .phone("31" + String.valueOf(System.nanoTime()).substring(4, 12))
                .password("{noop}no-se-usa")
                .role(Role.SELLER)
                .active(true)
                .build());
        return username;
    }

    private UUID categoryId(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst().orElseThrow().getId();
    }

    private UUID unitId(String abbreviation) {
        return unitRepository.findByAbbreviationIgnoreCase(abbreviation).orElseThrow().getId();
    }

    protected ProductRequest.ProductRequestBuilder product(String category, String unit, String price, String stock) {
        return ProductRequest.builder()
                .categoryId(categoryId(category))
                .unitId(unitId(unit))
                .name(unique(category))
                .unitPrice(new BigDecimal(price))
                .minimumWholesaleQuantity((short) 0)
                .purchasable(true)
                .manufacturable(false)
                .active(true)
                .initialStock(stock == null ? null : new BigDecimal(stock));
    }

    protected ProductResponse blanket(String price, String stock) {
        return productService.create(product("Manta", "#", price, stock).build());
    }

    protected ProductResponse fabric(String price, String stock) {
        return productService.create(product("Tela", "m", price, stock).build());
    }

    protected BigDecimal stock(UUID productId) {
        return inventoryRepository.findById(productId)
                .map(inventory -> inventory.getQuantity())
                .orElse(BigDecimal.ZERO);
    }
}
