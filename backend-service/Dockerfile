
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests


FROM eclipse-temurin:17-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY --from=build --chown=appuser:appuser /app/target/backend-service-0.0.1-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
