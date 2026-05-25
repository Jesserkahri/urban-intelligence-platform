# =============================================================
# Urban Intelligence Platform - Production Dockerfile
# PHASE 5: Multi-stage build, slim runtime, non-root, healthcheck
# =============================================================

# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

# Build with offline mode + tests skipped (run tests in CI separately)
RUN mvn package -DskipTests -q && \
    ls -la target/*.jar

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

# Security: non-root user
RUN addgroup -S urban && adduser -S urban -G urban

WORKDIR /app

# Copy only the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Security: never run as root
USER urban

# JVM production flags
ENV JAVA_OPTS="\
    -Xmx512m \
    -Xms256m \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    "

# Actuator health endpoint
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]