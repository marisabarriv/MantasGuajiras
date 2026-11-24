package com.mantasguajiras.backend.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.inventorymovement.dto.requests.MovementFilter;
import com.mantasguajiras.backend.inventorymovement.dto.response.InventoryMovementResponse;
import com.mantasguajiras.backend.inventorymovement.service.InventoryMovementService;
import com.mantasguajiras.backend.order.dto.requests.OrderPaymentRequest;
import com.mantasguajiras.backend.order.dto.requests.OrderRequest;
import com.mantasguajiras.backend.order.dto.response.OrderResponse;
import com.mantasguajiras.backend.order.service.OrderService;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/** Mantas personalizadas y pagos (PRO-09 a PRO-17). */
class CustomOrderFlowTest extends IntegrationTestBase {

    @Autowired private OrderService orderService;
    @Autowired private InventoryMovementService movementService;

    /** Movimientos generados por una operación (todos los productos). */
    private java.util.List<InventoryMovementResponse> movementsOf(UUID sourceId) {
        return movementService.search(new MovementFilter(null, null, "ORDER", null, null, null))
                .stream().filter(movement -> sourceId.equals(movement.getSourceId())).toList();
    }

    private OrderRequest.OrderRequestBuilder order(UUID fabricId) {
        return OrderRequest.builder()
                .name(unique("Manta de Lucía")).clientName("Lucía")
                .total(new BigDecimal("150000")).initialPayment(new BigDecimal("50000"))
                .fabricProductId(fabricId).fabricQuantity(new BigDecimal("2.5"));
    }

    private static OrderPaymentRequest payment(String amount, UUID requestId) {
        return new OrderPaymentRequest(new BigDecimal(amount), requestId);
    }

    @Test
    void conAbonoQuedaPendienteDescuentaTelaYNoSumaMantasTerminadas() {

        ProductResponse tela = fabric("20000", "10");

        OrderResponse saved = orderService.create(order(tela.getId()).build());

        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getPaidAmount()).isEqualByComparingTo("50000");
        assertThat(saved.getBalance()).isEqualByComparingTo("100000");
        assertThat(saved.getPayments()).hasSize(1);
        assertThat(saved.getCreatedByUsername()).isEqualTo(ADMIN);
        assertThat(stock(tela.getId())).isEqualByComparingTo("7.5");

        var movements = movementsOf(saved.getId());
        assertThat(movements).singleElement().satisfies(movement -> {
            assertThat(movement.getMovementTypeName()).isEqualTo("OUT");
            assertThat(movement.getSourceTypeName()).isEqualTo("ORDER");
        });
    }

    @Test
    void pagosHastaElSaldoLaDejanPagadaYNoSeAceptanPagosDeMas() {

        ProductResponse tela = fabric("20000", "10");
        OrderResponse saved = orderService.create(order(tela.getId()).build());

        assertThatThrownBy(() -> orderService.addPayment(saved.getId(), payment("100001", null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("mayor que el saldo");

        OrderResponse partial = orderService.addPayment(saved.getId(), payment("40000", null));
        assertThat(partial.getBalance()).isEqualByComparingTo("60000");
        assertThat(partial.getStatus()).isEqualTo("PENDING");

        OrderResponse paid = orderService.addPayment(saved.getId(), payment("60000", null));
        assertThat(paid.getBalance()).isEqualByComparingTo("0");
        assertThat(paid.getStatus()).isEqualTo("PAID");
        assertThat(paid.getPayments()).hasSize(3);

        assertThatThrownBy(() -> orderService.addPayment(saved.getId(), payment("1", null)))
                .hasMessageContaining("ya está pagada");
    }

    @Test
    void pagadaCompletaDesdeElInicio() {

        ProductResponse tela = fabric("20000", "10");

        OrderResponse saved = orderService.create(order(tela.getId()).paidInFull(true).build());

        assertThat(saved.getStatus()).isEqualTo("PAID");
        assertThat(saved.getPaidAmount()).isEqualByComparingTo("150000");
    }

    @Test
    void abonoMayorQueElValorOTelaInsuficienteSeRechazanSinTocarInventario() {

        ProductResponse tela = fabric("20000", "2");

        assertThatThrownBy(() -> orderService.create(order(tela.getId()).initialPayment(new BigDecimal("200000")).build()))
                .hasMessageContaining("no puede ser mayor");
        assertThatThrownBy(() -> orderService.create(order(tela.getId()).build()))
                .isInstanceOf(RuntimeException.class);

        assertThat(stock(tela.getId())).isEqualByComparingTo("2");
    }

    @Test
    void reintentosNoDuplicanLaPersonalizadaNiCobranDosVecesUnPago() {

        ProductResponse tela = fabric("20000", "10");
        UUID orderRequestId = UUID.randomUUID();

        OrderResponse first = orderService.create(order(tela.getId()).clientRequestId(orderRequestId).build());
        OrderResponse retry = orderService.create(order(tela.getId()).clientRequestId(orderRequestId).build());
        assertThat(retry.getId()).isEqualTo(first.getId());
        assertThat(stock(tela.getId())).as("tela descontada una sola vez").isEqualByComparingTo("7.5");

        UUID paymentRequestId = UUID.randomUUID();
        orderService.addPayment(first.getId(), payment("30000", paymentRequestId));
        OrderResponse afterRetry = orderService.addPayment(first.getId(), payment("30000", paymentRequestId));

        assertThat(afterRetry.getPayments()).as("abono inicial + un solo pago").hasSize(2);
        assertThat(afterRetry.getBalance()).isEqualByComparingTo("70000");

        // Ese identificador de pago no se puede reutilizar en otra personalizada.
        OrderResponse other = orderService.create(order(tela.getId()).build());
        assertThatThrownBy(() -> orderService.addPayment(other.getId(), payment("1000", paymentRequestId)))
                .hasMessageContaining("otra manta");
    }

    @Test
    void lasPersonalizadasNoSeModificanNiSeEliminan() {

        ProductResponse tela = fabric("20000", "10");
        OrderResponse saved = orderService.create(order(tela.getId()).build());

        assertThatThrownBy(() -> orderService.update(saved.getId(), order(tela.getId()).build()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> orderService.delete(saved.getId()))
                .isInstanceOf(BusinessException.class);
    }
}
