package com.mantasguajiras.backend.order.service.impl;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.common.util.UnitUtils;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.order.dto.requests.OrderPaymentRequest;
import com.mantasguajiras.backend.order.dto.requests.OrderRequest;
import com.mantasguajiras.backend.order.dto.response.OrderPaymentResponse;
import com.mantasguajiras.backend.order.dto.response.OrderResponse;
import com.mantasguajiras.backend.order.entity.Order;
import com.mantasguajiras.backend.order.entity.OrderPayment;
import com.mantasguajiras.backend.order.repository.OrderRepository;
import com.mantasguajiras.backend.order.service.OrderService;
import com.mantasguajiras.backend.product.entity.Product;
import com.mantasguajiras.backend.product.repository.ProductRepository;
import com.mantasguajiras.backend.security.util.SecurityUtils;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Mantas personalizadas: encargos de clientes con diseño y abono.
 *
 * - Al crearla se descuenta la tela usada del inventario (origen ORDER).
 *   La manta va directo al cliente: no entra al inventario de mantas.
 * - El abono inicial y los pagos posteriores quedan en order_payment.
 * - Pagada = lo abonado alcanza el valor de la manta.
 * Todo en una transacción: si falta tela, no queda nada registrado.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryMovementService inventoryMovementService;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(String status) {

        return orderRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .filter(order -> status == null
                        || status.isBlank()
                        || ("PAID".equalsIgnoreCase(status) && order.isPaid())
                        || ("PENDING".equalsIgnoreCase(status) && !order.isPaid()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return toResponse(findOrder(id));
    }

    @Override
    public OrderResponse create(OrderRequest request) {

        BigDecimal total = money(request.getTotal());

        BigDecimal initialPayment = Boolean.TRUE.equals(request.getPaidInFull())
                ? total
                : money(request.getInitialPayment() != null
                        ? request.getInitialPayment()
                        : BigDecimal.ZERO);

        if (initialPayment.compareTo(total) > 0) {
            throw new BusinessException(
                    "El abono (" + format(initialPayment) + ") no puede ser mayor "
                            + "que el valor de la manta (" + format(total) + ").");
        }

        Product fabric = productRepository.findById(request.getFabricProductId())
                .orElseThrow(() -> new ResourceNotFoundException("La tela elegida no existe."));

        if (UnitUtils.isWholeUnit(fabric.getUnit())) {
            throw new BusinessException(
                    fabric.getName() + " no es una tela: se maneja en unidades. "
                            + "Elige una tela (se mide en metros).");
        }

        User currentUser = currentUser();

        Order order = Order.builder()
                .name(request.getName().trim())
                .clientName(blankToNull(request.getClientName()))
                .total(total)
                .paidAmount(BigDecimal.ZERO)
                .fabricProduct(fabric)
                .fabricQuantity(request.getFabricQuantity().setScale(2, RoundingMode.HALF_UP))
                .createdBy(currentUser)
                .build();

        if (initialPayment.signum() > 0) {
            addPaymentToOrder(order, initialPayment, currentUser);
        }

        Order saved = orderRepository.save(order);

        // Descuenta la tela (valida existencias con la fila bloqueada).
        inventoryMovementService.registerMovement(
                fabric.getId(),
                "OUT",
                "ORDER",
                saved.getId(),
                saved.getFabricQuantity(),
                "Manta personalizada: " + saved.getName()
                        + (saved.getClientName() != null
                                ? " (cliente: " + saved.getClientName() + ")"
                                : "")
        );

        return toResponse(saved);
    }

    @Override
    public OrderResponse addPayment(UUID id, OrderPaymentRequest request) {

        Order order = orderRepository.findByIdForUpdate(id)
                .filter(found -> Boolean.TRUE.equals(found.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("La manta personalizada no existe."));

        if (order.isPaid()) {
            throw new BusinessException("Esta manta ya está pagada completa.");
        }

        BigDecimal amount = money(request.getAmount());

        if (amount.compareTo(order.getBalance()) > 0) {
            throw new BusinessException(
                    "El pago (" + format(amount) + ") es mayor que el saldo pendiente ("
                            + format(order.getBalance()) + ").");
        }

        addPaymentToOrder(order, amount, currentUser());

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse update(UUID id, OrderRequest request) {
        throw new BusinessException(
                "Las mantas personalizadas no se modifican después de registrarse. "
                        + "Para pagos usa \"Registrar pago\".");
    }

    @Override
    public void delete(UUID id) {
        throw new BusinessException(
                "Las mantas personalizadas no se eliminan: forman parte de las ventas.");
    }

    // =========================================================
    // AYUDANTES
    // =========================================================

    private void addPaymentToOrder(Order order, BigDecimal amount, User user) {

        order.getPayments().add(OrderPayment.builder()
                .order(order)
                .amount(amount)
                .createdBy(user)
                .build());

        order.setPaidAmount(order.getPaidAmount().add(amount));
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .filter(order -> Boolean.TRUE.equals(order.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("La manta personalizada no existe."));
    }

    private User currentUser() {
        return SecurityUtils.currentUsername()
                .flatMap(userRepository::findByUsername)
                .orElse(null);
    }

    private OrderResponse toResponse(Order order) {

        return OrderResponse.builder()
                .id(order.getId())
                .name(order.getName())
                .clientName(order.getClientName())
                .total(order.getTotal())
                .paidAmount(order.getPaidAmount())
                .balance(order.getBalance().max(BigDecimal.ZERO))
                .status(order.isPaid() ? "PAID" : "PENDING")
                .fabricProductId(order.getFabricProduct() != null ? order.getFabricProduct().getId() : null)
                .fabricProductName(order.getFabricProduct() != null ? order.getFabricProduct().getName() : null)
                .fabricQuantity(order.getFabricQuantity())
                .createdByUsername(order.getCreatedBy() != null ? order.getCreatedBy().getUsername() : null)
                .createdAt(order.getCreatedAt())
                .payments(order.getPayments().stream()
                        .map(payment -> OrderPaymentResponse.builder()
                                .id(payment.getId())
                                .amount(payment.getAmount())
                                .createdByUsername(payment.getCreatedBy() != null
                                        ? payment.getCreatedBy().getUsername()
                                        : null)
                                .createdAt(payment.getCreatedAt())
                                .build())
                        .toList())
                .build();
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** $45.000 o $1.500,5 (formato colombiano). */
    private static String format(BigDecimal value) {
        java.text.NumberFormat number =
                java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("es-CO"));
        number.setMaximumFractionDigits(2);
        return "$" + number.format(value);
    }
}
