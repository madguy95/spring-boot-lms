# syntax=docker/dockerfile:1.7

# ==============================================================================
# Stage 1 — Build with Gradle (JDK 21, matches build.gradle toolchain)
# ==============================================================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Copy wrapper + build config FIRST so the dependency-resolution layer stays cached
# across source-only changes. When src/ changes but build.gradle doesn't, this layer is reused.
COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
RUN chmod +x gradlew

# Warm the dependency cache. `dependencies` is forgiving (won't fail on partial config)
# so we tolerate non-zero exit while still pulling most artifacts.
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Now copy sources and build the runnable boot jar. Tests are run in CI, not here.
COPY src src
RUN ./gradlew --no-daemon -x test bootJar

# ==============================================================================
# Stage 2 — Runtime (JRE 21 only — smaller image, less attack surface)
# ==============================================================================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Non-root user — least privilege. Spring Boot doesn't need root at runtime.
RUN groupadd --system spring \
    && useradd --system --gid spring --no-create-home --shell /usr/sbin/nologin spring

# Copy only the built fat-jar from the builder stage. Glob handles any artifact name.
COPY --from=builder /workspace/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring

# Render injects $PORT at runtime; 8080 is the local-docker fallback.
ENV PORT=8080
EXPOSE 8080

# Container-aware JVM defaults:
#   UseContainerSupport: respect cgroup memory limits set by Render/K8s
#   MaxRAMPercentage:    leave headroom for off-heap allocations (Netty buffers, metaspace, ...)
#   ExitOnOutOfMemoryError: fail fast so the orchestrator restarts a wedged JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# `--server.port=$PORT` overrides whatever the profile yml says, so Render's port routing works
# without us having to wire SERVER_PORT separately. exec replaces shell so signals (SIGTERM) reach
# the JVM and graceful shutdown can fire.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT}"]
