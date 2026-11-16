# ---- Stage 1: build the jar with Maven ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy only pom.xml first so Docker caches dependencies between builds
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Now copy the source and build (tests are skipped; CI runs them separately)
COPY src ./src
RUN mvn -q package -DskipTests

# ---- Stage 2: slim runtime image ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT; application.properties already reads it
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
