package com.mantasguajiras.backend.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.sale.dto.requests.SaleItemRequest;
import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.dto.response.SaleItemResponse;
import com.mantasguajiras.backend.sale.dto.response.SaleResponse;
import com.mantasguajiras.backend.sale.service.SaleService;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/**
 * Precios de venta y precios especiales (DES-01 a DES-15, VEN-02 a VEN-06,
 * VEN-11, VEN-17). Caso de referencia del negocio:
 * normal $80.000 → especial $70.000 = descuento $10.000 (12,5 %).
 */
class SalePricingTest extends IntegrationTestBase {

    @Autowired private SaleService saleService;

    private static SaleItemRequest normal(UUID productId, String quantity) {
        return SaleItemRequest.builder()
                .productId(productId).quantity(new BigDecimal(quantity)).specialPrice(false).build();
    }

    private static SaleItemRequest withFinalPrice(UUID productId, String quantity, String finalPrice) {
        return SaleItemRequest.builder()
                .productId(productId).quantity(new BigDecimal(quantity))
                .specialPrice(true).finalUnitPrice(new BigDecimal(finalPrice)).build();
    }

    private static SaleItemRequest withDiscount(UUID productId, String quantity, String percentage) {
        return SaleItemRequest.builder()
                .productId(productId).quantity(new BigDecimal(quantity))
                .specialPrice(true).discountPercentage(new BigDecimal(percentage)).build();
    }

    private SaleResponse sell(SaleItemRequest... items) {
        return saleService.create(SaleRequest.builder().items(List.of(items)).build());
    }

    private static SaleItemResponse line(SaleResponse sale, UUID productId) {
        return sale.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst().orElseThrow();
    }

    @Test
    void casoDeReferencia80000A70000EnUnaSolaUnidadJuntoAOtroProductoAPrecioNormal() {

        ProductResponse wayuu = blanket("80000", "5");
        ProductResponse sencilla = blanket("45000", "5");

        SaleResponse sale = sell(
                withFinalPrice(wayuu.getId(), "1", "70000"),
                normal(sencilla.getId(), "2"));

        SaleItemResponse special = line(sale, wayuu.getId());
        assertThat(special.getUnitPrice()).as("precio normal guardado").isEqualByComparingTo("80000");
        assertThat(special.getFinalUnitPrice()).as("precio aplicado").isEqualByComparingTo("70000");
        assertThat(special.getDiscountPercentage()).as("porcentaje").isEqualByComparingTo("12.50");
        assertThat(special.getUnitPrice().subtract(special.getFinalUnitPrice()))
                .as("descuento en pesos").isEqualByComparingTo("10000");
        assertThat(special.getSubtotal()).isEqualByComparingTo("70000");

        SaleItemResponse regular = line(sale, sencilla.getId());
        assertThat(regular.getFinalUnitPrice()).isEqualByComparingTo("45000");
        assertThat(regular.getDiscountPercentage()).isEqualByComparingTo("0");
        assertThat(regular.getSubtotal()).isEqualByComparingTo("90000");

        assertThat(sale.getTotal()).as("total de la venta").isEqualByComparingTo("160000");

        assertThat(stock(wayuu.getId())).isEqualByComparingTo("4");
        assertThat(stock(sencilla.getId())).isEqualByComparingTo("3");

        assertThat(productService.findById(wayuu.getId()).getUnitPrice())
                .as("el precio especial NO cambia el precio base")
                .isEqualByComparingTo("80000");
    }

    @Test
    void porcentajeDeDescuentoCalculaElPrecioFinal() {

        ProductResponse wayuu = blanket("80000", "3");

        SaleItemResponse item = line(sell(withDiscount(wayuu.getId(), "1", "12.5")), wayuu.getId());

        assertThat(item.getFinalUnitPrice()).isEqualByComparingTo("70000");
        assertThat(item.getDiscountPercentage()).isEqualByComparingTo("12.5");
    }

    @Test
    void ventaAntiguaConservaElPrecioAplicadoAunqueCambieElPrecioDelProducto() {

        ProductResponse wayuu = blanket("80000", "3");
        UUID saleId = sell(withFinalPrice(wayuu.getId(), "1", "70000")).getId();

        // Sube el precio del producto después de la venta.
        ProductRequest update = product("Manta", "#", "95000", null)
                .name(wayuu.getName()).purchasePrice(new BigDecimal("95000")).build();
        productService.update(wayuu.getId(), update);

        SaleItemResponse item = line(saleService.findById(saleId), wayuu.getId());
        assertThat(item.getUnitPrice()).as("precio normal de ese día").isEqualByComparingTo("80000");
        assertThat(item.getFinalUnitPrice()).as("precio cobrado").isEqualByComparingTo("70000");
    }

    @Test
    void precioEspecialIgualOMayorAlNormalSeRechazaSinTocarInventario() {

        ProductResponse wayuu = blanket("80000", "3");

        assertThatThrownBy(() -> sell(withFinalPrice(wayuu.getId(), "1", "80000")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("menor al precio normal");
        assertThatThrownBy(() -> sell(withFinalPrice(wayuu.getId(), "1", "90000")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> sell(withFinalPrice(wayuu.getId(), "1", "0")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("mayor que cero");

        assertThat(stock(wayuu.getId())).isEqualByComparingTo("3");
    }

    @Test
    void descuentoDel100PorCientoOMasSeRechaza() {

        ProductResponse wayuu = blanket("80000", "3");

        assertThatThrownBy(() -> sell(withDiscount(wayuu.getId(), "1", "100")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("menor al 100%");
        assertThatThrownBy(() -> sell(withDiscount(wayuu.getId(), "1", "-5")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("negativo");
    }

    @Test
    void noSeEnvianDescuentoYPrecioFinalALaVezNiPrecioEspecialDesactivadoConDescuento() {

        ProductResponse wayuu = blanket("80000", "3");

        SaleItemRequest both = SaleItemRequest.builder()
                .productId(wayuu.getId()).quantity(BigDecimal.ONE).specialPrice(true)
                .discountPercentage(new BigDecimal("10")).finalUnitPrice(new BigDecimal("70000")).build();
        assertThatThrownBy(() -> sell(both)).isInstanceOf(BusinessException.class);

        SaleItemRequest inconsistent = SaleItemRequest.builder()
                .productId(wayuu.getId()).quantity(BigDecimal.ONE).specialPrice(false)
                .finalUnitPrice(new BigDecimal("70000")).build();
        assertThatThrownBy(() -> sell(inconsistent)).isInstanceOf(BusinessException.class);
    }

    @Test
    void productoDesactivadoNoSePuedeVenderYSuHistorialSeConserva() {

        ProductResponse wayuu = blanket("80000", "3");
        UUID oldSale = sell(normal(wayuu.getId(), "1")).getId();

        productService.delete(wayuu.getId());

        assertThat(productService.findAll(false)).noneMatch(product -> product.getId().equals(wayuu.getId()));
        assertThatThrownBy(() -> sell(normal(wayuu.getId(), "1")))
                .hasMessageContaining("no está activo");
        assertThat(saleService.findById(oldSale).getItems()).hasSize(1);
        assertThat(stock(wayuu.getId())).isEqualByComparingTo("2");
    }

    @Test
    void lasVentasNoSeModificanNiSeEliminan() {

        ProductResponse wayuu = blanket("80000", "3");
        SaleResponse sale = sell(normal(wayuu.getId(), "1"));

        SaleRequest change = SaleRequest.builder().items(List.of(normal(wayuu.getId(), "2"))).build();
        assertThatThrownBy(() -> saleService.update(sale.getId(), change))
                .hasMessageContaining("no pueden modificarse");
        assertThatThrownBy(() -> saleService.delete(sale.getId()))
                .hasMessageContaining("no pueden eliminarse");

        SaleResponse unchanged = saleService.findById(sale.getId());
        assertThat(unchanged.getTotal()).isEqualByComparingTo("80000");
        assertThat(stock(wayuu.getId())).isEqualByComparingTo("2");
    }

    @Test
    void mantaEnFraccionesNoSeVende() {

        ProductResponse wayuu = blanket("80000", "3");

        assertThatThrownBy(() -> sell(normal(wayuu.getId(), "1.5")))
                .isInstanceOf(RuntimeException.class);
        assertThat(stock(wayuu.getId())).isEqualByComparingTo("3");
    }
}
