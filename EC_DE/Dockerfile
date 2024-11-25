# Build stage for Parent and EC_Central
FROM maven:3.9.4-eclipse-temurin-21 AS build-central
WORKDIR /build

# Copiar el pom.xml padre
COPY pom.xml /build/pom.xml

# Copiar los archivos de los módulos para que estén disponibles durante la instalación del padre
COPY EC_Central /build/EC_Central
COPY EC_DE /build/EC_DE
COPY EC_Customer /build/EC_Customer
COPY EC_S /build/EC_S

# Instalar el proyecto padre para que esté disponible para los módulos
RUN mvn -f /build/pom.xml clean install -DskipTests

# Construir e instalar EC_Central
RUN mvn -f /build/EC_Central/pom.xml clean install -DskipTests

# Build stage for EC_DE
FROM maven:3.9.4-eclipse-temurin-21 AS build-de
WORKDIR /build

# Copiar el pom.xml padre y los archivos de EC_DE desde el contexto de construcción
COPY pom.xml /build/pom.xml
COPY EC_DE /build/EC_DE

# Copiar el repositorio Maven desde build-central para reutilizar dependencias
COPY --from=build-central /root/.m2 /root/.m2

# Construir EC_DE
RUN mvn -f /build/EC_DE/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado de EC_DE al contenedor final
COPY --from=build-de /build/EC_DE/target/ec-de-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]