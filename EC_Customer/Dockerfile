# Etapa de construcción
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el pom.xml del proyecto padre y los módulos
COPY pom.xml /build/pom.xml
COPY EC_LOGGING /build/EC_LOGGING
COPY EC_Central /build/EC_Central
COPY EC_DE /build/EC_DE
COPY EC_Customer /build/EC_Customer
COPY EC_S /build/EC_S
COPY EC_CTC /build/EC_CTC
COPY EC_Registry /build/EC_Registry


## Instalar el proyecto padre para que esté disponible para los módulos
#RUN mvn -f /build/pom.xml clean install -DskipTests

# Construir el módulo EC_Customer
RUN mvn -f /build/EC_Customer/pom.xml clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el archivo JAR generado en la etapa de construcción
COPY --from=build /build/EC_Customer/target/ec-customer-0.0.1-SNAPSHOT.jar /app/ECCustomer.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/ECCustomer.jar"]