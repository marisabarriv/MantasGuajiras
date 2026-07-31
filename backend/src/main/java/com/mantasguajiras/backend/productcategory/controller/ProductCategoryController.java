package com.mantasguajiras.backend.productcategory.controller;
import org.springframework.web.bind.annotation.RestController;
import com.mantasguajiras.backend.productcategory.dto.requests.ProductCategoryRequest;
import com.mantasguajiras.backend.productcategory.dto.response.ProductCategoryResponse;
import com.mantasguajiras.backend.productcategory.service.ProductCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
    
    @RestController
    @RequestMapping("/api/product-categories")
    @RequiredArgsConstructor
    @Tag(name = "Product Categories")
    public class ProductCategoryController {
        private final ProductCategoryService service;

        @Operation(summary = "Listar categorías")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
        })
        @GetMapping
        public List<ProductCategoryResponse> findAll() {
            return service.findAll();
        }

        @Operation(summary = "Obtener categoría por ID")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
        })
        @GetMapping("/{id}")
        public ProductCategoryResponse findById(@PathVariable Short id) {
            return service.findById(id);
        }

        @Operation(summary = "Crear nueva categoría")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")
        })
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ProductCategoryResponse create(
            @Valid @RequestBody ProductCategoryRequest request) {

            return service.create(request);
        }

        @Operation(summary = "Actualizar categoría existente")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
        })
        @PutMapping("/{id}")
        public ProductCategoryResponse update(
        @PathVariable Short id,
        @Valid @RequestBody ProductCategoryRequest request) {

            return service.update(id, request);
        }

        @Operation(summary = "Eliminar categoría")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
        })
        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Short id) {
            service.delete(id);
        }
    }

