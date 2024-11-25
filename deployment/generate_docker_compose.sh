#!/bin/bash

# Ruta del archivo .env
ENV_FILE="../.env"

# Función para mostrar el uso correcto del script
mostrar_uso() {
  echo "Uso: $0 <tipo_pc> [numero_de_clientes_o_taxis]"
  echo "tipo_pc: 1 (Kafka, Zookeeper y EC_Customer), 2 (EC_Central y Postgres), 3 (EC_DE)"
  echo "numero_de_clientes_o_taxis: Número de EC_Customer a generar para tipo_pc=1, o EC_DE a generar para tipo_pc=3"
  echo "Ejemplos:"
  echo "  $0 1 3  # Para tipo_pc=1 con 3 EC_Customer"
  echo "  $0 2    # Para tipo_pc=2 (EC_Central y Postgres)"
  echo "  $0 3 2  # Para tipo_pc=3 con 2 EC_DE"
}

# Verificar al menos un argumento
if [[ $# -lt 1 ]]; then
  echo "Error: Se requiere al menos un argumento."
  mostrar_uso
  exit 1
fi

# Cargar variables de entorno desde el archivo .env en el directorio raíz del proyecto
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: No se encontró el archivo .env en el directorio raíz."
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

# Determinar el nombre del archivo Docker Compose basado en el tipo de PC
case "$1" in
  1)
    if [[ $# -lt 2 ]]; then
      echo "Error: Para tipo_pc=1, se requiere un segundo argumento indicando el número de EC_Customer."
      mostrar_uso
      exit 1
    fi
    DOCKER_COMPOSE_FILE="../docker-compose.pc1.yml"
    NUM_CLIENTES="$2"
    ;;
  2)
    DOCKER_COMPOSE_FILE="../docker-compose.pc2.yml"
    ;;
  3)
    if [[ $# -lt 2 ]]; then
      echo "Error: Para tipo_pc=3, se requiere un segundo argumento indicando el número de EC_DE."
      mostrar_uso
      exit 1
    fi
    DOCKER_COMPOSE_FILE="../docker-compose.pc3.yml"
    NUM_DE="$2"
    ;;
  *)
    echo "Error: Opción no válida. Elija 1, 2 o 3."
    mostrar_uso
    exit 1
    ;;
esac

# Limpiar el archivo generado si existe
rm -f "$DOCKER_COMPOSE_FILE"

# Comienzo del archivo docker-compose
cat <<EOF > "$DOCKER_COMPOSE_FILE"
version: '3.9'
services:
EOF

# Función para añadir Zookeeper
add_zookeeper_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"
    networks:
      - easycab_network
EOF
}

# Función para añadir Kafka
add_kafka_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://${KAFKA_IP}:9092
      KAFKA_ZOOKEEPER_CONNECT: ${KAFKA_IP}:2181
      KAFKA_DEFAULT_REPLICATION_FACTOR: 1
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper
    networks:
      - easycab_network
EOF
}

# Función para añadir Postgres
add_postgres_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  postgres:
    image: postgres:14
    container_name: postgres
    environment:
      POSTGRES_DB: easycab
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    networks:
      easycab_network:
        ipv4_address: 192.168.100.30
    volumes:
      - postgres_data:/var/lib/postgresql/data
EOF
}

# Función para añadir EC_Central
add_ec_central_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  ec_central:
    build:
      context: .
      dockerfile: ./EC_Central/Dockerfile
    image: ec-central:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://${IP_PC_2}:5432/easycab
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_KAFKA_BOOTSTRAP-SERVERS: ${IP_PC_1}:9092
      SERVER_PORT: 8081
      BROKER_ADDRESS: ${KAFKA_IP}:9092
    ports:
      - "8081:8081"
      - "9090:9090"
    networks:
      - easycab_network
    depends_on:
      - postgres
EOF
}

# Función para añadir servicios EC_DE (taxis)
add_ec_de_services() {
  for i in $(seq 1 "$NUM_DE"); do
    host_port_var="HOST_PORT_${i}"
    container_port_var="CONTAINER_PORT_${i}"
    sensor_port_var="SENSOR_PORT_${i}"

    host_port="${!host_port_var}"
    container_port="${!container_port_var}"
    sensor_port="${!sensor_port_var}"

    # Validar que las variables estén definidas
    if [[ -z "$host_port" || -z "$container_port" || -z "$sensor_port" ]]; then
      echo "Error: HOST_PORT_${i}, CONTAINER_PORT_${i} o SENSOR_PORT_${i} no están definidos en .env."
      exit 1
    fi

    # Asignar una IP dentro de la subred 192.168.100.0/24 para EC_DE
    ec_de_ip="192.168.100.$((100 + i))"  # Ejemplo: 192.168.100.101, 192.168.100.102, etc.

    cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  ec_de_$i:
    build:
      context: .
      dockerfile: ./EC_DE/Dockerfile
    image: ec-de:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://${IP_PC_2}:5432/easycab
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_APPLICATION_NAME: EC_DE
      SPRING_KAFKA_BOOTSTRAP_SERVERS: ${IP_PC_1}:9092
      CENTRAL_IP: ${IP_PC_2}
      CENTRAL_PORT: ${CENTRAL_PORT}
      TAXI_ID: $i
      SENSOR_PORT: ${sensor_port}
    ports:
      - "${host_port}:${container_port}"
    networks:
      easycab_network:
        ipv4_address: ${ec_de_ip}
EOF
  done
}

# Función para añadir servicios de clientes EC_Customer
add_ec_customer_services() {
  for i in $(seq 1 "$NUM_CLIENTES"); do
    client_id=$(echo "$i" | awk '{printf "%c", $1 + 96}')  # Genera letras 'a', 'b', 'c', 'd', etc.

    # Convertir client_id a mayúsculas para buscar el puerto correcto
    customer_port_var="CUSTOMER_PORT_$(echo "$client_id" | tr '[:lower:]' '[:upper:]')"
    customer_port="${!customer_port_var}"

    # Verificar que el puerto esté configurado
    if [ -z "$customer_port" ]; then
      echo "Error: CUSTOMER_PORT_${client_id^^} no está configurado en .env."
      exit 1
    fi

    # Asignar una IP dentro de la subred 192.168.100.0/24 para EC_Customer
    ec_customer_ip="192.168.100.$((200 + i))"  # Ejemplo: 192.168.100.201, 192.168.100.202, etc.

    # Añadir al archivo Docker Compose
    cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  ec_customer_$client_id:
    build:
      context: .
      dockerfile: ./EC_Customer/Dockerfile
    image: ec-customer:latest
    environment:
      SPRING_APPLICATION_NAME: EC_Customer_$client_id
      SPRING_KAFKA_BOOTSTRAP_SERVERS: ${IP_PC_1}:9092
      CLIENT_ID: "$client_id"
      broker.address: ${IP_PC_1}:9092
      file: destinations${i}.txt
    ports:
      - "${customer_port}:${CONTAINER_PORT_CUSTOMER}"
    depends_on:
      - kafka
    networks:
      easycab_network:
        ipv4_address: ${ec_customer_ip}
EOF
  done
}

# Función para añadir servicios Kafka y Zookeeper en PC1
add_services_pc1() {
  add_zookeeper_service
  add_kafka_service
}

# Configuración según el tipo de PC
case "$1" in
  1)  # PC1: Kafka, Zookeeper y EC_Customer
    echo "Configurando para PC1: Kafka, Zookeeper y EC_Customer"
    add_services_pc1
    add_ec_customer_services
    ;;
  2)  # PC2: EC_Central y Postgres
    echo "Configurando para PC2: EC_Central y Postgres"
    add_postgres_service
    add_ec_central_service
    ;;
  3)  # PC3: EC_DE
    echo "Configurando para PC3: EC_DE"
    add_ec_de_services
    ;;
  *)
    echo "Error: Opción no válida. Elija 1, 2 o 3."
    mostrar_uso
    exit 1
    ;;
esac

# Agregar configuración de la red y volúmenes
cat <<EOF >> "$DOCKER_COMPOSE_FILE"
volumes:
  postgres_data:

networks:
  easycab_network:
    driver: bridge
    ipam:
      config:
        - subnet: 192.168.100.0/24
EOF

# Mensaje de confirmación y comando para ejecutar
echo "Archivo ${DOCKER_COMPOSE_FILE} creado con éxito."
echo
echo "Para iniciar los servicios, usa el siguiente comando:"
echo "docker-compose -f ${DOCKER_COMPOSE_FILE} up --build -d"