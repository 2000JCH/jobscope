# ── Build Stage ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# 의존성 캐시 레이어 (build.gradle 변경 없으면 재사용)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ── Run Stage ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]