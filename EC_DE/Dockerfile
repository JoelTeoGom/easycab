# Build stage for EC_Central
FROM maven:3.9.4-eclipse-temurin-21 AS build-central
WORKDIR /build/ec-central

# Copy EC_Central files from the build context
COPY EC_Central/pom.xml .
COPY EC_Central/src ./src

RUN mvn clean install -DskipTests

# Build stage for EC_DE
FROM maven:3.9.4-eclipse-temurin-21 AS build-de
WORKDIR /build/ec-de

# Copy EC_DE files from the build context
COPY EC_DE/pom.xml .
COPY EC_DE/src ./src

# Copy the Maven repository from build-central
COPY --from=build-central /root/.m2 /root/.m2

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build-de /build/ec-de/target/ec-de-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]