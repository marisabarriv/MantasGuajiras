package com.mantasguajiras.backend.inventorymovement.service.impl;

import com.mantasguajiras.backend.inventorymovement.dto.requests.MovementFilter;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.inventory.entity.Inventory;
import com.mantasguajiras.backend.inventory.repository.InventoryRepository;
import com.mantasguajiras.backend.inventorymovement.dto.requests.InventoryMovementRequest;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.entity.InventoryMovement;
import com.mantasguajiras.backend.inventorymovement.mapper.InventoryMovementMapper;
import com.mantasguajiras.backend.inventorymovement.repository.InventoryMovementRepository;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.security.util.SecurityUtils;
import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import com.mantasguajiras.backend.common.util.UnitUtils;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

        private static final int OBSERVATIONS_MAX_LENGTH = 255;

        private final InventoryMovementRepository inventoryMovementRepository;
        private final InventoryMovementMapper inventoryMovementMapper;
        private final InventoryRepository inventoryRepository;
        private final ProductRepository productRepository;
        private final MovementTypeRepository movementTypeRepository;
        private final SourceTypeRepository sourceTypeRepository;
        private final UserRepository userRepository;

        // =========================================================
        // CONSULTAS
        // =========================================================

        private static final java.util.Set<String> MOVEMENT_TYPES = java.util.Set.of("IN", "OUT");

        private static final java.util.Set<String> SOURCE_TYPES =
                        java.util.Set.of("SALE", "PURCHASE", "PRODUCTION", "ORDER", "ADJUSTMENT");

        @Override
        @Transactional(readOnly = true)
        public List<InventoryMovementResponse> search(MovementFilter filter) {

                String type = upperOrNull(filter.type());
                String source = upperOrNull(filter.source());
                String username = filter.username() != null && !filter.username().isBlank()
                                ? filter.username().trim().toLowerCase()
                                : null;

                if (type != null && !MOVEMENT_TYPES.contains(type)) {
                        throw new IllegalArgumentException(
                                        "Tipo de movimiento no válido: usa IN (entradas) u OUT (salidas).");
                }

                if (source != null && !SOURCE_TYPES.contains(source)) {
                        throw new IllegalArgumentException(
                                        "Origen no válido: usa SALE, PURCHASE, PRODUCTION, ORDER o ADJUSTMENT.");
                }

                if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
                        throw new IllegalArgumentException(
                                        "La fecha inicial no puede ser posterior a la fecha final.");
                }

                Specification<InventoryMovement> specification = (root, query, cb) -> {

                        List<Predicate> predicates = new java.util.ArrayList<>();

                        if (filter.productId() != null) {
                                predicates.add(cb.equal(root.get("product").get("id"), filter.productId()));
                        }
                        if (type != null) {
                                predicates.add(cb.equal(root.get("movementType").get("name"), type));
                        }
                        if (source != null) {
                                predicates.add(cb.equal(root.get("sourceType").get("name"), source));
                        }
                        if (username != null) {
                                predicates.add(cb.equal(
                                                cb.lower(root.join("createdBy", JoinType.LEFT).get("username")),
                                                username));
                        }
                        if (filter.from() != null) {
                                predicates.add(cb.greaterThanOrEqualTo(
                                                root.get("createdAt"), filter.from().atStartOfDay()));
                        }
                        if (filter.to() != null) {
                                predicates.add(cb.lessThan(
                                                root.get("createdAt"), filter.to().plusDays(1).atStartOfDay()));
                        }

                        return cb.and(predicates.toArray(Predicate[]::new));
                };

                return inventoryMovementRepository
                                .findAll(specification, Sort.by(Sort.Direction.DESC, "createdAt"))
                                .stream()
                                .map(inventoryMovementMapper::toResponse)
                                .toList();
        }

        private static String upperOrNull(String value) {
                return value != null && !value.isBlank() ? value.trim().toUpperCase() : null;
        }

        @Override
        @Transactional(readOnly = true)
        public InventoryMovementResponse findById(UUID id) {

                InventoryMovement movement = inventoryMovementRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento de inventario no encontrado con id: "
                                                                + id));

                return inventoryMovementMapper.toResponse(movement);
        }

        // =========================================================
        // MOVIMIENTO MANUAL (POST /api/inventory-movements)
        // =========================================================

        @Override
        @Transactional
        public InventoryMovementResponse create(
                        InventoryMovementRequest request) {

                Product product = findProduct(request.getProductId());

                MovementType movementType = movementTypeRepository.findById(
                                request.getMovementTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de movimiento no encontrado con id: "
                                                                + request.getMovementTypeId()));

                SourceType sourceType = sourceTypeRepository.findById(
                                request.getSourceTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de origen no encontrado con id: "
                                                                + request.getSourceTypeId()));

                // Las compras, ventas y producción generan sus movimientos
                // desde su propio módulo. Registrarlos aquí a mano dejaría
                // movimientos sin una compra/venta/producción real detrás.
                if (!"ADJUSTMENT".equalsIgnoreCase(sourceType.getName())) {
                        throw new BusinessException(
                                        "Los movimientos de compras, ventas y producción "
                                                        + "se registran desde su propio módulo. "
                                                        + "Aquí solo se permiten ajustes.");
                }

                return record(
                                product,
                                movementType,
                                sourceType,
                                request.getSourceId(),
                                request.getQuantity(),
                                request.getObservations());
        }

        // =========================================================
        // MOVIMIENTO DESDE OTROS MÓDULOS
        // =========================================================

        @Override
        @Transactional
        public InventoryMovementResponse registerMovement(
                        UUID productId,
                        String movementTypeName,
                        String sourceTypeName,
                        UUID sourceId,
                        BigDecimal quantity,
                        String observations) {

                Product product = findProduct(productId);

                return record(
                                product,
                                findMovementType(movementTypeName),
                                findSourceType(sourceTypeName),
                                sourceId,
                                quantity,
                                observations);
        }

        // =========================================================
        // AJUSTE POR CONTEO
        // =========================================================

        @Override
        @Transactional
        public InventoryMovementResponse adjustToCount(
                        UUID productId,
                        BigDecimal countedQuantity,
                        String reason) {

                if (countedQuantity == null
                                || countedQuantity.compareTo(BigDecimal.ZERO) < 0) {

                        throw new BusinessException(
                                        "La cantidad contada no puede ser negativa.");
                }

                Product product = findProduct(productId);

                validateQuantityForUnit(product, countedQuantity, "La cantidad contada");

                // Se bloquea la fila para calcular la diferencia sobre la
                // cantidad real del momento (record() vuelve a leerla dentro
                // de la misma transacción, ya bloqueada).
                BigDecimal currentQuantity = inventoryRepository
                                .findByIdForUpdate(productId)
                                .map(Inventory::getQuantity)
                                .orElse(BigDecimal.ZERO);

                BigDecimal difference = countedQuantity.subtract(currentQuantity);

                if (difference.signum() == 0) {
                        throw new BusinessException(
                                        "La cantidad contada es igual a la registrada ("
                                                        + format(currentQuantity)
                                                        + "). No hay nada que ajustar.");
                }

                String movementTypeName = difference.signum() > 0 ? "IN" : "OUT";

                String observations = "Ajuste por conteo: de "
                                + format(currentQuantity)
                                + " a "
                                + format(countedQuantity)
                                + (reason != null && !reason.isBlank()
                                                ? ". Motivo: " + reason.trim()
                                                : "");

                return record(
                                product,
                                findMovementType(movementTypeName),
                                findSourceType("ADJUSTMENT"),
                                null,
                                difference.abs(),
                                observations);
        }

        // =========================================================
        // LÓGICA COMÚN
        // =========================================================

        /**
         * Valida, actualiza la existencia y guarda el movimiento.
         * Todo ocurre en la transacción de quien llama: si algo falla,
         * no queda ni el cambio de cantidad ni el movimiento.
         */
        private InventoryMovementResponse record(
                        Product product,
                        MovementType movementType,
                        SourceType sourceType,
                        UUID sourceId,
                        BigDecimal quantity,
                        String observations) {

                validateQuantity(quantity);

                validateActiveProduct(product);

                validateQuantityForUnit(product, quantity, "La cantidad");

                validateActiveMovementType(movementType);

                validateActiveSourceType(sourceType);

                validateMovementSource(
                                movementType.getName(),
                                sourceType.getName());

                // Sin .id(...): con @MapsId Hibernate toma el id del
                // producto. Si se asigna a mano, save() hace merge()
                // y falla con "null identifier (Inventory)".
                Inventory inventory = inventoryRepository.findByIdForUpdate(product.getId())
                                .orElseGet(() -> Inventory.builder()
                                                .product(product)
                                                .quantity(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                applyMovement(
                                inventory,
                                product,
                                movementType.getName(),
                                quantity);

                inventory.setUpdatedAt(LocalDateTime.now());

                inventoryRepository.save(inventory);

                InventoryMovement movement = InventoryMovement.builder()
                                .product(product)
                                .movementType(movementType)
                                .sourceType(sourceType)
                                .sourceId(sourceId)
                                .quantity(quantity)
                                .observations(truncate(observations))
                                .createdBy(currentUser())
                                .build();

                InventoryMovement savedMovement = inventoryMovementRepository.save(movement);

                return inventoryMovementMapper.toResponse(savedMovement);
        }

        private void applyMovement(
                        Inventory inventory,
                        Product product,
                        String movementTypeName,
                        BigDecimal quantity) {

                BigDecimal currentQuantity = inventory.getQuantity() != null
                                ? inventory.getQuantity()
                                : BigDecimal.ZERO;

                if ("IN".equalsIgnoreCase(movementTypeName)) {

                        inventory.setQuantity(
                                        currentQuantity.add(quantity));

                } else if ("OUT".equalsIgnoreCase(movementTypeName)) {

                        BigDecimal newQuantity = currentQuantity.subtract(quantity);

                        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                                throw new BusinessException(
                                                "No hay suficiente inventario de "
                                                                + product.getName()
                                                                + ". Disponible: "
                                                                + format(currentQuantity)
                                                                + ", solicitado: "
                                                                + format(quantity)
                                                                + ".");
                        }

                        inventory.setQuantity(newQuantity);

                } else {

                        throw new BusinessException(
                                        "El tipo de movimiento no permite actualizar "
                                                        + "el inventario: "
                                                        + movementTypeName);
                }
        }

        private Product findProduct(UUID productId) {

                return productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Producto no encontrado con id: "
                                                                + productId));
        }

        private MovementType findMovementType(String name) {

                return movementTypeRepository
                                .findByName(name)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de movimiento no encontrado: "
                                                                + name));
        }

        private SourceType findSourceType(String name) {

                return sourceTypeRepository
                                .findByName(name)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de origen no encontrado: "
                                                                + name));
        }

        private User currentUser() {

                return SecurityUtils.currentUsername()
                                .flatMap(userRepository::findByUsername)
                                .orElse(null);
        }

        // =========================================================
        // VALIDACIONES
        // =========================================================

        private void validateQuantity(BigDecimal quantity) {

                if (quantity == null ||
                                quantity.compareTo(BigDecimal.ZERO) <= 0) {

                        throw new BusinessException(
                                        "La cantidad debe ser mayor que cero.");
                }
        }

        /**
         * Mantas (unidad "#") solo admiten cantidades enteras.
         * Telas (metros) admiten decimales.
         */
        private void validateQuantityForUnit(
                        Product product,
                        BigDecimal quantity,
                        String label) {

                if (UnitUtils.isWholeUnit(product.getUnit())
                                && quantity.stripTrailingZeros().scale() > 0) {

                        throw new BusinessException(
                                        label + " de " + product.getName()
                                                        + " debe ser un número entero, "
                                                        + "porque se maneja en unidades (#).");
                }
        }

        private void validateActiveProduct(Product product) {

                if (!Boolean.TRUE.equals(product.getActive())) {

                        throw new BusinessException(
                                        "El producto no está activo: "
                                                        + product.getName());
                }
        }

        private void validateActiveMovementType(
                        MovementType movementType) {

                if (!Boolean.TRUE.equals(movementType.getActive())) {

                        throw new BusinessException(
                                        "El tipo de movimiento está inactivo: "
                                                        + movementType.getName());
                }
        }

        private void validateActiveSourceType(
                        SourceType sourceType) {

                if (!Boolean.TRUE.equals(sourceType.getActive())) {

                        throw new BusinessException(
                                        "El tipo de origen está inactivo: "
                                                        + sourceType.getName());
                }
        }

        private void validateMovementSource(
                        String movementTypeName,
                        String sourceTypeName) {

                if ("PURCHASE".equalsIgnoreCase(sourceTypeName)
                                && !"IN".equalsIgnoreCase(movementTypeName)) {

                        throw new BusinessException(
                                        "Una compra solo puede generar una entrada de inventario.");
                }

                if ("SALE".equalsIgnoreCase(sourceTypeName)
                                && !"OUT".equalsIgnoreCase(movementTypeName)) {

                        throw new BusinessException(
                                        "Una venta solo puede generar una salida de inventario.");
                }

                if ("PRODUCTION".equalsIgnoreCase(sourceTypeName)
                                && !("IN".equalsIgnoreCase(movementTypeName)
                                                || "OUT".equalsIgnoreCase(movementTypeName))) {

                        throw new BusinessException(
                                        "Una producción solo puede generar entradas "
                                                        + "o salidas de inventario.");
                }

                if ("ADJUSTMENT".equalsIgnoreCase(sourceTypeName)
                                && !("IN".equalsIgnoreCase(movementTypeName)
                                                || "OUT".equalsIgnoreCase(movementTypeName))) {

                        throw new BusinessException(
                                        "Un ajuste de inventario solo puede generar "
                                                        + "una entrada o una salida.");
                }
        }

        // =========================================================
        // UTILIDADES
        // =========================================================

        private static String format(BigDecimal value) {

                return value == null
                                ? "0"
                                : value.stripTrailingZeros().toPlainString()
                                .replace('.', ','); // decimal con coma (es-CO)
        }

        private static String truncate(String text) {

                if (text == null || text.length() <= OBSERVATIONS_MAX_LENGTH) {
                        return text;
                }

                return text.substring(0, OBSERVATIONS_MAX_LENGTH);
        }
}
