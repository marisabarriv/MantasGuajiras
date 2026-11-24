package com.mantasguajiras.backend.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.productcategory.dto.requests.ProductCategoryRequest;
import com.mantasguajiras.backend.productcategory.dto.response.ProductCategoryResponse;
import com.mantasguajiras.backend.productcategory.service.ProductCategoryService;
import com.mantasguajiras.backend.support.IntegrationTestBase;
import com.mantasguajiras.backend.unit.dto.requests.UnitRequest;
import com.mantasguajiras.backend.unit.dto.response.UnitResponse;
import com.mantasguajiras.backend.unit.service.UnitService;

/** Datos maestros: categorías y unidades (solo ADMIN, ver RolePermissionsTest). */
class CatalogAdminTest extends IntegrationTestBase {

    @Autowired private ProductCategoryService categoryService;
    @Autowired private UnitService unitService;

    private static String shortUnique(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 6);
    }

    @Test
    void categoriasSeCreanEditanYDesactivanSinDuplicados() {
        String name = shortUnique("Cojín ");
        ProductCategoryResponse saved = categoryService.create(ProductCategoryRequest.builder()
                .name(name).description("Cojines tejidos").displayOrder(3).active(true).build());

        assertThat(categoryService.findById(saved.getId()).getName()).isEqualTo(name);
        assertThat(categoryService.findAll()).anyMatch(category -> category.getId().equals(saved.getId()));

        assertThatThrownBy(() -> categoryService.create(ProductCategoryRequest.builder()
                .name(name.toUpperCase()).displayOrder(1).active(true).build()))
                .isInstanceOf(DuplicateResourceException.class);

        ProductCategoryResponse renamed = categoryService.update(saved.getId(), ProductCategoryRequest.builder()
                .name(name + " grande").displayOrder(4).active(true).build());
        assertThat(renamed.getName()).isEqualTo(name + " grande");

        assertThatThrownBy(() -> categoryService.update(saved.getId(), ProductCategoryRequest.builder()
                .name("Manta").displayOrder(4).active(true).build()))
                .isInstanceOf(DuplicateResourceException.class);

        categoryService.delete(saved.getId());
        assertThat(categoryService.findById(saved.getId()).getActive()).isFalse();
        assertThatThrownBy(() -> categoryService.delete(saved.getId()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("ya se encuentra desactivada");
        assertThatThrownBy(() -> categoryService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void unidadesSeCreanEditanYEliminanSinDuplicados() {
        String name = shortUnique("Docena ");
        UnitResponse saved = unitService.create(UnitRequest.builder()
                .name(name).abbreviation("dz" + name.substring(name.length() - 3)).active(true).build());

        assertThat(unitService.findById(saved.getId()).getName()).isEqualTo(name);
        assertThat(unitService.findAll()).anyMatch(unit -> unit.getId().equals(saved.getId()));

        assertThatThrownBy(() -> unitService.create(UnitRequest.builder()
                .name(name).abbreviation("x" + name.substring(name.length() - 3)).active(true).build()))
                .isInstanceOf(DuplicateResourceException.class);

        UnitResponse renamed = unitService.update(saved.getId(), UnitRequest.builder()
                .name(name + "s").abbreviation(saved.getAbbreviation()).active(true).build());
        assertThat(renamed.getName()).isEqualTo(name + "s");

        assertThatThrownBy(() -> unitService.update(saved.getId(), UnitRequest.builder()
                .name("Metro").abbreviation(saved.getAbbreviation()).active(true).build()))
                .isInstanceOf(DuplicateResourceException.class);

        unitService.delete(saved.getId());
        assertThatThrownBy(() -> unitService.findById(saved.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
