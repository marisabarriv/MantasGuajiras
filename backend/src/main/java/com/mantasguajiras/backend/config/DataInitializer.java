package com.mantasguajiras.backend.config;

import com.mantasguajiras.backend.movementtype.entity.MovementType;
import com.mantasguajiras.backend.movementtype.repository.MovementTypeRepository;
import com.mantasguajiras.backend.sourcetype.entity.SourceType;
import com.mantasguajiras.backend.sourcetype.repository.SourceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MovementTypeRepository movementTypeRepository;
    private final SourceTypeRepository sourceTypeRepository;

    @Override
    public void run(String... args) {

        createMovementType("ENTRADA", "Entrada de inventario.");
        createMovementType("SALIDA", "Salida de inventario.");
        createMovementType("AJUSTE", "Ajuste de inventario.");

        createSourceType("COMPRA", "Movimiento generado por una compra.");
        createSourceType("VENTA", "Movimiento generado por una venta.");
        createSourceType("PRODUCCION", "Movimiento generado por producción.");
        createSourceType("PEDIDO", "Movimiento generado por un pedido.");
        createSourceType("AJUSTE", "Movimiento generado por un ajuste de inventario.");
    }

    private void createMovementType(String name, String description) {

        if (movementTypeRepository.findByName(name).isEmpty()) {

            movementTypeRepository.save(
                    MovementType.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build()
            );
        }
    }

    private void createSourceType(String name, String description) {

        if (sourceTypeRepository.findByName(name).isEmpty()) {

            sourceTypeRepository.save(
                    SourceType.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .build()
            );
        }
    }
}