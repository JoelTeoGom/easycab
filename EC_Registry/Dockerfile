# Build stage using Maven and OpenJDK 21
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy the parent pom.xml and the entire EC_Registry module
COPY pom.xml /build/pom.xml
COPY EC_Registry /build/EC_Registry

# Build the EC_Registry module (skip tests to speed up the process)
RUN mvn -f /build/EC_Registry/pom.xml clean package -DskipTests

# Runtime stage using JDK 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the generated executable jar from the build stage
COPY --from=build /build/EC_Registry/target/EC_Registry-0.0.1-SNAPSHOT.jar /app/EC_Registry.jar

COPY EC_Registry/init.sh /app/init.sh
RUN chmod +x /app/init.sh
# Expose HTTPS port
EXPOSE 8443

VOLUME /server

CMD ["/app/init.sh"]
