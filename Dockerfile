# Multi-stage build: build with Maven, then run with a lightweight JRE.
FROM maven:3-eclipse-temurin-25 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
# -C (strict checksums) rejects a corrupt/empty dependency download instead of
# silently using it; no separate dependency:go-offline (known to write empty
# jars for some transitives).
RUN mvn -B -q -C package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/*.jar /app/app.jar
EXPOSE 8384
ENTRYPOINT ["java", "-jar", "app.jar"]
