# Multi-stage build for PharmaCore Tax-Compliant POS Backend
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy gradle wrapper and configurations
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Grant execute rights to gradlew
RUN chmod +x ./gradlew

# Copy source code and database scripts
COPY src ./src

# Build production JAR (skipping tests)
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Set non-root user for security
RUN groupadd -r pharmacore && useradd -r -g pharmacore pharmacore

# Copy executable jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

USER pharmacore
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV DIGITAX_MOCK_MODE=true
ENV MPESA_MOCK_MODE=true

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

