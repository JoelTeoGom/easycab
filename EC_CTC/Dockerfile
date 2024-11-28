# Etapa de construcción con Maven y JDK 21
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copia el archivo pom.xml del proyecto principal y el directorio EC_CTC
COPY pom.xml /build/pom.xml
COPY EC_CTC /build/EC_CTC

# Construir el módulo EC_CTC
RUN mvn -f /build/EC_CTC/pom.xml clean package -DskipTests

# Etapa de ejecución con JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia el JAR generado desde la etapa de construcción
COPY --from=build /build/EC_CTC/target/EC_CTC-0.0.1-SNAPSHOT-exec.jar /app/EC_CTC.jar

# Expone el puerto en el que la aplicación se ejecutará
EXPOSE 8082

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/EC_CTC.jar"]
