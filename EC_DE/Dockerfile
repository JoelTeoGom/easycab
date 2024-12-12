# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the parent pom.xml and the EC_DE module
COPY pom.xml /build/pom.xml
COPY EC_DE /build/EC_DE
COPY EC_LOGGING /build/EC_LOGGING

RUN mvn -f /build/pom.xml clean install -DskipTests
# Build EC_DE
RUN mvn -f /build/EC_DE/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/EC_DE/target/ec-de-0.0.1-SNAPSHOT.jar /app/EC_DE.jar
COPY EC_DE/init.sh /app/init.sh
RUN chmod +x /app/init.sh

VOLUME /client
VOLUME /shared

EXPOSE 8080

# Command to run the script and start the application
CMD ["/app/init.sh"]
