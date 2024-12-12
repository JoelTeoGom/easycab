# Build stage using Maven and OpenJDK 21
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy the parent pom.xml and the entire EC_Central module
COPY pom.xml /build/pom.xml
COPY EC_Central /build/EC_Central
COPY EC_LOGGING /build/EC_LOGGING

# Instalar la biblioteca logging-library primero
RUN mvn -f /build/EC_LOGGING/pom.xml clean install -DskipTests

# Build the EC_Central module
RUN mvn -f /build/EC_Central/pom.xml clean package -DskipTests

# Runtime stage using JDK 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the generated executable jar from the build stage
COPY --from=build /build/EC_Central/target/EC_CENTRAL-0.0.1-SNAPSHOT-exec.jar /app/EC_Central.jar

# Expose the application port
EXPOSE 8080 9090

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/EC_Central.jar"]