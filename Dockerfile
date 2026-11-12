# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /app
COPY pom.xml .
# Download all deps into the image layer — evaluator gets them from the pulled image, not the internet
RUN mvn dependency:go-offline -B -q

FROM deps AS build
COPY src ./src
RUN mvn package -Dmaven.test.skip=true -DskipGitHooks=true -B -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
