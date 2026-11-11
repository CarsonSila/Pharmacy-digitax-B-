# ─────────────────────────────────────────────────────────
# Stage 1: Build — compile the Spring Boot JAR
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# Copy Gradle wrapper first (cached layer — only re-downloaded when changed)
COPY HeThongBanThuoc_BE/HeThongBanThuoc/gradlew .
COPY HeThongBanThuoc_BE/HeThongBanThuoc/gradle ./gradle
RUN chmod +x ./gradlew

# Copy build scripts (another cached layer)
COPY HeThongBanThuoc_BE/HeThongBanThuoc/build.gradle .
COPY HeThongBanThuoc_BE/HeThongBanThuoc/settings.gradle .

# Pre-download dependencies (cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon --quiet || true

# Copy source code
COPY HeThongBanThuoc_BE/HeThongBanThuoc/src ./src

# Build the production JAR (skip tests — run tests in CI before this step)
RUN ./gradlew bootJar --no-daemon -x test

# ─────────────────────────────────────────────────────────
# Stage 2: Runtime — minimal JRE image
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create non-root user for security
RUN groupadd -r pharmacore && useradd -r -g pharmacore -m pharmacore

# Create uploads directory and set ownership
RUN mkdir -p /app/uploads && chown pharmacore:pharmacore /app/uploads

# Copy the JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown pharmacore:pharmacore app.jar

# Switch to non-root user
USER pharmacore

# Expose application port
EXPOSE 8080

# Health check — Spring Boot Actuator endpoint
# (Add spring-boot-starter-actuator to build.gradle to enable this)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers:
#   -XX:+UseContainerSupport      — respect Docker memory limits
#   -XX:MaxRAMPercentage=75.0     — use 75% of container RAM for heap
#   -Djava.security.egd           — faster SecureRandom (important for JWT)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
