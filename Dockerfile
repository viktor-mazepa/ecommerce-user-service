# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Preload dependencies for better caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
RUN ./mvnw -q -DskipTests dependency:go-offline

# Build
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar /app/user-service.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/user-service.jar"]
