FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el pom.xml padre y los archivos de EC_DE desde el contexto de construcción
COPY ../pom.xml /build/pom.xml
COPY EC_DE /build/EC_DE

# Construir EC_DE
RUN mvn -f /build/EC_DE/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado de EC_DE al contenedor final
COPY --from=build /build/EC_DE/target/ec-de-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]