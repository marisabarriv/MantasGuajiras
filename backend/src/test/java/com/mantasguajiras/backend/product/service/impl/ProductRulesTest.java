package com.mantasguajiras.backend.product.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.mantasguajiras.backend.product.dto.requests.ProductRequest;
import com.mantasguajiras.backend.productcategory.entity.ProductCategory;
import com.mantasguajiras.backend.unit.entity.Unit;

/** Reglas de producto que el backend garantiza (sin base de datos). */
class ProductRulesTest {

    private static final ProductCategory MANTA =
            ProductCategory.builder().name("Manta").build();

    private static final ProductCategory TELA =
            ProductCategory.builder().name("Tela").build();

    private static final Unit UNIDAD =
            Unit.builder().name("Unidad").abbreviation("#").build();

    private static final Unit METRO =
            Unit.builder().name("Metro").abbreviation("m").build();

    // ---------- categoría ↔ unidad ----------

    @Test
    void mantaEnUnidadEsValida() {
        assertThatCode(() -> ProductServiceImpl.validateCategoryMatchesUnit(MANTA, UNIDAD))
                .doesNotThrowAnyException();
    }

    @Test
    void telaEnMetrosEsValida() {
        assertThatCode(() -> ProductServiceImpl.validateCategoryMatchesUnit(TELA, METRO))
                .doesNotThrowAnyException();
    }

    @Test
    void mantaEnMetrosSeRechaza() {
        assertThatThrownBy(() -> ProductServiceImpl.validateCategoryMatchesUnit(MANTA, METRO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mantas");
    }

    @Test
    void telaEnUnidadSeRechaza() {
        assertThatThrownBy(() -> ProductServiceImpl.validateCategoryMatchesUnit(TELA, UNIDAD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("telas");
    }

    // ---------- precios ----------

    @Test
    void sinPrecioDeCompraSeUsaElUnitario() {
        ProductRequest request = ProductRequest.builder()
                .unitPrice(new BigDecimal("50000"))
                .build();

        ProductServiceImpl.validatePrices(request);

        assertThat(request.getPurchasePrice()).isEqualByComparingTo("50000");
    }

    @Test
    void preciosIgualesConDistintaEscalaSonValidos() {
        ProductRequest request = ProductRequest.builder()
                .unitPrice(new BigDecimal("50000"))
                .purchasePrice(new BigDecimal("50000.00"))
                .build();

        assertThatCode(() -> ProductServiceImpl.validatePrices(request))
                .doesNotThrowAnyException();
    }

    @Test
    void preciosDistintosSeRechazan() {
        ProductRequest request = ProductRequest.builder()
                .unitPrice(new BigDecimal("50000"))
                .purchasePrice(new BigDecimal("40000"))
                .build();

        assertThatThrownBy(() -> ProductServiceImpl.validatePrices(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- cantidades ----------

    @Test
    void stockMinimoDecimalEnMantaSeRechaza() {
        ProductRequest request = ProductRequest.builder()
                .minimumStock(new BigDecimal("2.5"))
                .build();

        assertThatThrownBy(() -> ProductServiceImpl.validateQuantities(request, UNIDAD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock mínimo");
    }

    @Test
    void stockInicialDecimalEnMantaSeRechaza() {
        ProductRequest request = ProductRequest.builder()
                .initialStock(new BigDecimal("1.25"))
                .build();

        assertThatThrownBy(() -> ProductServiceImpl.validateQuantities(request, UNIDAD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock inicial");
    }

    @Test
    void enteroConDecimalesCeroEnMantaEsValido() {
        ProductRequest request = ProductRequest.builder()
                .minimumStock(new BigDecimal("3.00"))
                .initialStock(new BigDecimal("10.0"))
                .build();

        assertThatCode(() -> ProductServiceImpl.validateQuantities(request, UNIDAD))
                .doesNotThrowAnyException();
    }

    @Test
    void decimalesEnTelaSonValidos() {
        ProductRequest request = ProductRequest.builder()
                .minimumStock(new BigDecimal("2.5"))
                .initialStock(new BigDecimal("12.75"))
                .build();

        assertThatCode(() -> ProductServiceImpl.validateQuantities(request, METRO))
                .doesNotThrowAnyException();
    }
}
