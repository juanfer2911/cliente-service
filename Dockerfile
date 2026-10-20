# ── Etapa 1: compilación ─────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copiamos pom.xml primero para aprovechar la caché de capas
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiamos el código fuente y empaquetamos (sin tests)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Etapa 2: imagen de runtime ───────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar solo el JAR generado
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]