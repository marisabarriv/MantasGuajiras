package com.mantasguajiras.backend.common.monitoring;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Revisa cada pocos minutos el mismo estado que publica
 * {@code /actuator/health} (base de datos y espacio en disco) y deja una
 * línea ERROR en el log si algo falla, y otra cuando se recupera.
 * Así un problema queda registrado aunque nadie esté mirando.
 */
@Configuration
@EnableScheduling
public class HealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(HealthMonitor.class);

    private final HealthEndpoint healthEndpoint;

    private Status lastStatus = Status.UP;

    public HealthMonitor(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @Scheduled(
            initialDelayString = "${app.monitoring.health-check-ms:300000}",
            fixedDelayString = "${app.monitoring.health-check-ms:300000}")
    public void check() {

        HealthDescriptor health = healthEndpoint.health();
        record(health.getStatus(), describe(health));
    }

    void record(Status status, String components) {

        if (!Status.UP.equals(status)) {
            log.error("Estado del sistema: {} ({})", status, components);
        } else if (!Status.UP.equals(lastStatus)) {
            log.info("Estado del sistema recuperado: UP");
        }

        lastStatus = status;
    }

    static String describe(HealthDescriptor health) {
        if (health instanceof CompositeHealthDescriptor composite) {
            Map<String, HealthDescriptor> components = composite.getComponents();
            return components.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue().getStatus())
                    .collect(Collectors.joining(", "));
        }
        return String.valueOf(health.getStatus());
    }
}
