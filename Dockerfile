# ---------- Stage 1: build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY mvnw .
COPY .mvn ./.mvn
COPY pom.xml .

RUN chmod +x ./mvnw
RUN ./mvnw -B -q -e -DskipTests dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
