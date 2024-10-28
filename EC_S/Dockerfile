# Etapa de construcción
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el pom.xml del proyecto padre
COPY ../pom.xml /build/pom.xml

# Copiar el módulo EC_S
COPY EC_S /build/EC_S

# Construir el módulo EC_S
RUN mvn -f /build/EC_S/pom.xml clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el archivo JAR generado en la etapa de construcción
COPY --from=build /build/EC_S/target/*.jar /app/ECS.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/ECS.jar"]