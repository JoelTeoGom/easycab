# Fase de compilación usando Maven y OpenJDK 21
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar pom.xml y resolver dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests

# Fase de ejecución usando JDK 21
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiar el jar generado desde la fase de compilación
COPY --from=build /app/target/ec-central-0.0.1-SNAPSHOT.jar /app/EC_Central.jar

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/EC_Central.jar"]