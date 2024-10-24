#!/bin/bash

# Script para configurar el entorno según el PC desde el que se ejecuta
# y levantar los servicios correspondientes con Docker Compose.

function show_help {
    echo "Uso: $0 [opción]"
    echo
    echo "Opciones:"
    echo "  -h, --help      Muestra esta ayuda y termina."
    echo "  1               Configura y levanta los servicios para PC1 (Kafka, Zookeeper, EC_Central, Postgres)."
    echo "  2               Configura y levanta los servicios para PC2 (EC_DE, EC_S)."
    echo "  3               Configura y levanta los servicios para PC3 (EC_Customer)."
    echo "  e               Edita el archivo .env antes de configurar los servicios."
    echo
    echo "Descripción:"
    echo "  Este script configura el entorno y levanta los servicios de EasyCab utilizando Docker Compose."
    echo "  Selecciona el tipo de máquina y el script se encargará de configurar las variables de entorno"
    echo "  y levantar los contenedores correspondientes."
    echo
    echo "Ejemplos:"
    echo "  $0 1    Configura el entorno para PC1 y levanta los servicios asociados."
    echo "  $0 2    Configura el entorno para PC2 y levanta los servicios asociados."
    echo "  $0 3    Configura el entorno para PC3 y levanta los servicios asociados."
    echo "  $0 e    Abre el archivo .env para editar antes de configurar el entorno."
}

# Comprobar si el usuario ha pedido ayuda
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
    show_help
    exit 0
fi

echo "Selecciona el tipo de acción:"
echo "1. Configurar y levantar los servicios para PC1 (Kafka, Zookeeper, EC_Central, Postgres)"
echo "2. Configurar y levantar los servicios para PC2 (EC_DE, EC_S)"
echo "3. Configurar y levantar los servicios para PC3 (EC_Customer)"
echo "e. Editar el archivo .env"
read -p "Introduce la opción: " OPTION

# Cargar las variables de entorno desde el archivo .env
set -a
source .env
set +a

# Permitir la edición del archivo .env antes de continuar
if [[ "$OPTION" == "e" ]]; then
    echo "Abriendo el archivo .env para editar..."
    vim ../.env
    echo "Archivo .env editado. Reinicia el script para aplicar los cambios."
    exit 0
fi

cd ..
case $OPTION in
  1)
    echo "Configurando entorno para PC1 (Kafka, Zookeeper, EC_Central, Postgres)..."
    docker-compose up -d kafka zookeeper ec_central postgres --build
    ;;
  2)
    echo "Configurando entorno para PC2 (EC_DE, EC_S)..."
    docker-compose up -d ec_de ec_s --build
    ;;
  3)
    echo "Configurando entorno para PC3 (EC_Customer)..."
    docker-compose up -d ec_customer --build
    ;;
  *)
    echo "Opción no válida. Por favor, selecciona 1, 2, 3, o 'e' para editar el archivo .env, o usa -h para ver la ayuda."
    ;;
esac