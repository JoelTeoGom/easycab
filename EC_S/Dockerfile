# Etapa de construcción
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el archivo de configuración y el pom.xml
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el archivo JAR generado en la etapa de construcción
COPY --from=build /app/target/ec-s-0.0.1-SNAPSHOT.jar /app/ECS.jar

# Exponer el puerto de la aplicación
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/ECS.jar"]