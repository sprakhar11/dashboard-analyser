# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY commons/pom.xml commons/pom.xml
COPY pingService/pom.xml pingService/pom.xml
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY commons/src commons/src
COPY pingService/src pingService/src
RUN ./mvnw package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/pingService/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
