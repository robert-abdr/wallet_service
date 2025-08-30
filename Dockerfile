FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app
COPY src ./src
COPY . .
RUN gradle spotlessApply --no-daemon && \
    gradle clean build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/service.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]