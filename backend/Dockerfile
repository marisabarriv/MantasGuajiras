# =========================================================
# Backend de Mantas Guajiras (perfil prod). Se usa desde deploy/.
# Las claves NUNCA van en la imagen: llegan por variables de entorno
# (deploy/.env). .dockerignore excluye .env y application-dev.yml.
# =========================================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml lombok.config ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q -DskipTests -Djacoco.skip=true package \
    && cp target/backend-*.jar /app.jar

FROM eclipse-temurin:21-jre-alpine
# Usuario sin privilegios; logs en un volumen.
RUN addgroup -S mantas && adduser -S mantas -G mantas \
    && mkdir -p /app/logs && chown -R mantas:mantas /app
WORKDIR /app
COPY --from=build /app.jar app.jar
USER mantas

ARG APP_COMMIT=desconocido
ENV APP_COMMIT=${APP_COMMIT} \
    SPRING_PROFILES_ACTIVE=prod \
    LOG_FILE=/app/logs/mantasguajiras.log \
    TZ=America/Bogota

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# Memoria máxima 512 MB; si se agotara, el proceso termina y Docker lo
# reinicia (docs/12-rendimiento-y-monitoreo.md §4).
ENTRYPOINT ["sh", "-c", "exec java -Xmx512m -XX:+ExitOnOutOfMemoryError -Duser.timezone=$TZ -jar app.jar"]
