#!/bin/bash

# Ruta del archivo .env
ENV_FILE="../.env"

# Definir todas las variables requeridas con sus valores por defecto
required_vars=(
  # IPs de los PCs
  "IP_PC_1=10.112.153.217"  # IP de PC1 donde se ejecutan Kafka y Zookeeper
  "IP_PC_2=10.112.153.217"  # IP de PC2 donde se ejecutan ec_central y Postgres
  "IP_PC_3=10.112.153.217"  # IP de PC3 donde se ejecutan EC_DE

  # Variables derivadas basadas en las IPs de los PCs
  "POSTGRES_IP=\${IP_PC_2}"
  "ZOOKEEPER_IP=\${IP_PC_1}"
  "KAFKA_IP=\${IP_PC_1}"
  "CENTRAL_IP=\${IP_PC_2}"
  "CENTRAL_PORT=8081"

  # Puertos para EC_DE (taxis)
  "HOST_PORT_1=8001"
  "CONTAINER_PORT_1=8080"
  "SENSOR_PORT_1=5001"

  "HOST_PORT_2=8002"
  "CONTAINER_PORT_2=8080"
  "SENSOR_PORT_2=5002"

  "HOST_PORT_3=8003"
  "CONTAINER_PORT_3=8080"
  "SENSOR_PORT_3=5003"

  "HOST_PORT_4=8004"
  "CONTAINER_PORT_4=8080"
  "SENSOR_PORT_4=5004"

  # Puertos para EC_Customer
  "CUSTOMER_PORT_A=9001"
  "CUSTOMER_PORT_B=9002"
  "CUSTOMER_PORT_C=9003"
  "CUSTOMER_PORT_D=9004"

  # Puerto de contenedor para EC_Customer
  "CONTAINER_PORT_CUSTOMER=8080"

  # Puertos para EC_S
  "SENSOR_HOST_PORT_A=10001"
  "SENSOR_HOST_PORT_B=10002"
  "SENSOR_HOST_PORT_C=10003"
  "SENSOR_HOST_PORT_D=10004"

  # Puerto de contenedor para EC_S
  "SENSOR_CONTAINER_PORT=9090"

  # Nombres y puertos de componentes EC_DE
  "DE_IP_A=ec_de_a"
  "DE_PORT_A=8081"

  "DE_IP_B=ec_de_b"
  "DE_PORT_B=8082"

  "DE_IP_C=ec_de_c"
  "DE_PORT_C=8083"

  "DE_IP_D=ec_de_d"
  "DE_PORT_D=8084"
)

# Crear el archivo .env si no existe
if [ ! -f "$ENV_FILE" ]; then
  touch "$ENV_FILE"
  echo "Archivo .env creado."
fi

# Función para verificar y agregar variables faltantes
add_missing_vars() {
  for var in "${required_vars[@]}"; do
    var_name="${var%%=*}"
    default_value="${var#*=}"

    # Verificar si la variable existe en .env
    if ! grep -q "^${var_name}=" "$ENV_FILE"; then
      echo "${var}" >> "$ENV_FILE"
      echo "Añadida la variable ${var_name} con valor por defecto ${default_value}"
    fi
  done
}

# Función para corregir puertos que no sean numéricos
fix_invalid_ports() {
  # Iterar sobre las variables de puertos de EC_Customer
  for client in A B C D; do
    var_name="CUSTOMER_PORT_${client}"
    var_value=$(grep "^${var_name}=" "$ENV_FILE" | cut -d '=' -f2)
    if ! [[ "$var_value" =~ ^[0-9]+$ ]]; then
      # Define el puerto predeterminado basado en el cliente
      case "$client" in
        A) default_port=9001 ;;
        B) default_port=9002 ;;
        C) default_port=9003 ;;
        D) default_port=9004 ;;
        *) default_port=9000 ;;
      esac
      sed -i.bak "s/^${var_name}=.*/${var_name}=${default_port}/" "$ENV_FILE"
      echo "Corrigiendo ${var_name} a valor predeterminado: ${default_port}"
    fi
  done

  # Iterar sobre las variables de puertos para EC_DE y otros servicios
  for id in 1 2 3 4; do
    for prefix in HOST_PORT CONTAINER_PORT SENSOR_PORT; do
      var_name="${prefix}_${id}"
      var_value=$(grep "^${var_name}=" "$ENV_FILE" | cut -d '=' -f2)
      if ! [[ "$var_value" =~ ^[0-9]+$ ]]; then
        if [[ "$prefix" == "HOST_PORT" ]]; then
          default_port=$((8000 + id))
        elif [[ "$prefix" == "CONTAINER_PORT" ]]; then
          default_port=$((8080 + id))
        elif [[ "$prefix" == "SENSOR_PORT" ]]; then
          default_port=$((9090 + id))
        fi
        sed -i.bak "s/^${var_name}=.*/${var_name}=${default_port}/" "$ENV_FILE"
        echo "Corrigiendo ${var_name} a valor predeterminado: ${default_port}"
      fi
    done
  done
}

# Exportar las variables del archivo .env al entorno
export_variables() {
  set -o allexport
  source "$ENV_FILE"
  set +o allexport
  echo "Variables de entorno exportadas."
}

# Ejecutar funciones
add_missing_vars
fix_invalid_ports
export_variables

echo "Verificación, corrección y exportación de variables de entorno completada en $ENV_FILE."