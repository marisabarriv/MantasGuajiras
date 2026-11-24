package com.mantasguajiras.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Arranca la aplicación completa contra la base de pruebas.
 * Si pasa, significa que las migraciones Flyway (V1…V21) se aplican
 * desde cero y que el esquema resultante coincide con las entidades
 * (ddl-auto: validate).
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
