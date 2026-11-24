package com.mantasguajiras.backend.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.product.dto.response.ProductResponse;
import com.mantasguajiras.backend.support.IntegrationTestBase;

/** Decisión del negocio (24/11/2026): no puede haber dos productos con el mismo nombre. */
class ProductNameTest extends IntegrationTestBase {

    @Test
    void nombreRepetidoSeRechazaSinDistinguirMayusculasNiEspacios() {

        ProductResponse original = blanket("80000", "1");
        String sameNameOtherCase = "  " + original.getName().toUpperCase().replace(" ", "   ") + " ";

        assertThatThrownBy(() -> productService.create(
                product("Manta", "#", "50000", null).name(original.getName()).build()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un producto llamado");

        assertThatThrownBy(() -> productService.create(
                product("Manta", "#", "50000", null).name(sameNameOtherCase).build()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void nombreDeUnProductoDesactivadoTambienSeRechazaYSugiereReactivarlo() {

        ProductResponse old = fabric("20000", null);
        productService.delete(old.getId());

        assertThatThrownBy(() -> productService.create(
                product("Tela", "m", "20000", null).name(old.getName()).build()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Reactívalo");
    }

    @Test
    void alEditarNoSePuedeUsarElNombreDeOtroPeroSiConservarElPropio() {

        ProductResponse first = blanket("80000", "1");
        ProductResponse second = blanket("60000", "1");

        assertThatThrownBy(() -> productService.update(second.getId(),
                product("Manta", "#", "60000", null).name(first.getName()).build()))
                .isInstanceOf(DuplicateResourceException.class);

        ProductResponse kept = productService.update(second.getId(),
                product("Manta", "#", "65000", null).name(second.getName()).build());
        assertThat(kept.getUnitPrice()).isEqualByComparingTo(new BigDecimal("65000"));
    }

    @Test
    void losEspaciosSobrantesSeLimpianAlGuardar() {

        String name = unique("Manta  Wayuu ");
        ProductResponse saved = productService.create(
                product("Manta", "#", "80000", null).name("  " + name + "  ").build());

        assertThat(saved.getName()).isEqualTo(name.trim().replaceAll("\\s+", " "));
    }
}
