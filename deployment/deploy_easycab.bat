@echo off
rem Script para configurar el entorno según el PC desde el que se ejecuta
rem y levantar los servicios correspondientes con Docker Compose en Windows.

echo Selecciona el tipo de máquina:
echo 1. PC1 (Kafka, Zookeeper, EC_Central, Postgres)
echo 2. PC2 (EC_DE, EC_S)
echo 3. PC3 (EC_Customer)
set /p OPTION=Introduce el número de la opción:

rem Cargar las variables de entorno desde el archivo .env
for /f "tokens=1,2 delims==" %%a in (.env) do set %%a=%%b

cd ..
if "%OPTION%"=="1" (
    echo Configurando entorno para PC1 (Kafka, Zookeeper, EC_Central, Postgres)...
    set KAFKA_IP=%KAFKA_IP%
    set ZOOKEEPER_IP=%ZOOKEEPER_IP%
    set CENTRAL_IP=%CENTRAL_IP%
    docker-compose up -d kafka zookeeper ec_central postgres --build
) else if "%OPTION%"=="2" (
    echo Configurando entorno para PC2 (EC_DE, EC_S)...
    set KAFKA_IP=%KAFKA_IP%
    set CENTRAL_IP=%CENTRAL_IP%
    set SENSOR_IP=%SENSOR_IP%
    set DE_IP=%DE_IP%
    docker-compose up -d ec_de ec_s --build
) else if "%OPTION%"=="3" (
    echo Configurando entorno para PC3 (EC_Customer)...
    set KAFKA_IP=%KAFKA_IP%
    set CENTRAL_IP=%CENTRAL_IP%
    docker-compose up -d ec_customer --build
) else (
    echo Opción no válida. Por favor, selecciona 1, 2 o 3.
)
pause