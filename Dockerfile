# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || echo "Warning: Some dependencies may not be cached"

# Copy source code and build
COPY src ./src
# Build with verbose logging to see errors
# Split into compile and package to see where it fails
RUN mvn clean compile -B -e || (echo "=== Compilation failed ===" && exit 1)
RUN mvn package -DskipTests -B -e || (echo "=== Packaging failed ===" && exit 1)

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install wget for healthcheck
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

