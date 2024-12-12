# Etapa de construcción con Maven y JDK 21
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copiar el archivo pom.xml del proyecto principal y los submódulos necesarios
COPY pom.xml /build/pom.xml
COPY EC_CTC /build/EC_CTC
COPY EC_LOGGING /build/EC_LOGGING

# Instalar la biblioteca logging-library primero
RUN mvn -f /build/EC_LOGGING/pom.xml clean install -DskipTests

# Descargar dependencias y construir el módulo EC_CTC
RUN mvn -f /build/EC_CTC/pom.xml clean package -DskipTests

# Etapa de ejecución con JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado desde la etapa de construcción
COPY --from=build /build/EC_CTC/target/EC_CTC-0.0.1-SNAPSHOT-exec.jar /app/EC_CTC.jar

# Expone el puerto en el que la aplicación se ejecutará
EXPOSE 8082

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/EC_CTC.jar"]