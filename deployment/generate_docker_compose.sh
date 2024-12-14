#!/bin/bash

# Ruta del archivo .env
ENV_FILE="../.env"

# Función para mostrar el uso correcto del script
mostrar_uso() {
  echo "Uso: $0 <tipo_pc> [numero_de_clientes_o_taxis]"
  echo "tipo_pc: 1 (Kafka, Zookeeper y EC_Customer), 2 (Postgres, EC_Central, CTC, Nginx y logging), 3 (EC_Registry y EC_DE)"
  echo "numero_de_clientes_o_taxis: Número de EC_Customer a generar para tipo_pc=1, o EC_DE a generar para tipo_pc=3"
  echo "Ejemplos:"
  echo "  $0 1 3  # Para tipo_pc=1 con 3 EC_Customer"
  echo "  $0 2    # Para tipo_pc=2 (EC_Central, Postgres, CTC, Nginx y logging)"
  echo "  $0 3 2  # Para tipo_pc=3 con 2 EC_DE y EC_Registry"
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
    container_name: postgres_easycab
    environment:
      POSTGRES_DB: easycab
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    networks:
      easycab_network:
        ipv4_address: 192.168.100.100
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
      LOGSTASH_HOST: logstash
      LOGSTASH_PORT: 5050
      SPRING_APPLICATION_NAME: EC_Central
      SPRING_DATASOURCE_URL: jdbc:postgresql://192.168.100.100:5432/easycab
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_KAFKA_BOOTSTRAP-SERVERS: 192.168.100.3:9092
      SERVER_PORT: 8081
      BROKER_ADDRESS: 192.168.100.3:9092
      CTC_URL: http://192.168.100.200:8082/traffic
    ports:
      - "8081:8081"
      - "9090:9090"
    networks:
      easycab_network:
        ipv4_address: 192.168.100.4
      shared-network: {}
    depends_on:
      - postgres
EOF
}

# Función para añadir EC_CTC
add_ec_ctc_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  ec_ctc:
    container_name: ec_ctc
    build:
      context: .
      dockerfile: ./EC_CTC/Dockerfile
    image: ec-ctc:latest
    environment:
      LOGSTASH_HOST: logstash
      LOGSTASH_PORT: 5050
      SPRING_APPLICATION_NAME: EC_CTC
    ports:
      - "8082:8082"
    networks:
      - easycab_network
      - shared-network
    depends_on:
      - ec_central
EOF
}

# Función para añadir Nginx
add_nginx_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  nginx:
    build:
      context: ./taxi-tracker-nginx
      dockerfile: Dockerfile
    container_name: taxi-tracker-nginx
    ports:
      - "8080:80"
    networks:
      easycab_network:
        ipv4_address: 192.168.100.14
    depends_on:
      - ec_central
EOF
}

# Función para añadir Elasticsearch, Logstash y Kibana en PC2
add_logging_services() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"

  elasticsearch:
    image: elasticsearch:8.9.0
    container_name: elasticsearch
    environment:
      - node.name=elasticsearch
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - esdata:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      shared-network:
        ipv4_address: 192.168.101.9

  logstash:
    image: logstash:8.9.0
    container_name: logstash
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
    ports:
      - "5050:5000"
      - "5044:5044"
    depends_on:
      - elasticsearch
    networks:
      shared-network:
        ipv4_address: 192.168.101.10

  kibana:
    image: kibana:8.9.0
    container_name: kibana
    environment:
      ELASTICSEARCH_HOSTS: "http://elasticsearch:9200"
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      shared-network:
        ipv4_address: 192.168.101.11
EOF
}

# Función para añadir EC_Registry en PC3
add_ec_registry_service() {
  cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  ec_registry:
    build:
      context: .
      dockerfile: EC_Registry/Dockerfile
    container_name: ec_registry
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://192.168.100.100:5432/easycab
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: org.postgresql.Driver
    volumes:
      - server:/server
      - shared:/shared
    ports:
      - "8443:8443"
    networks:
      easycab_network:
        ipv4_address: 192.168.100.5
EOF
}

# Función para añadir servicios EC_DE (taxis) en PC3
add_ec_de_services() {
  for i in $(seq 1 "$NUM_DE"); do
    host_port_var="HOST_PORT_${i}"
    container_port_var="CONTAINER_PORT_${i}"
    sensor_port_var="SENSOR_PORT_${i}"

    host_port="${!host_port_var}"
    container_port="${!container_port_var}"
    sensor_port="${!sensor_port_var}"

    if [[ -z "$host_port" || -z "$container_port" || -z "$sensor_port" ]]; then
      echo "Error: HOST_PORT_${i}, CONTAINER_PORT_${i} o SENSOR_PORT_${i} no están definidos en .env."
      exit 1
    fi

    ec_de_ip="192.168.100.$((100 + i))"

    cat <<EOF >> "$DOCKER_COMPOSE_FILE"
  ec_de_$i:
    build:
      context: .
      dockerfile: ./EC_DE/Dockerfile
    image: ec-de:latest
    volumes:
      - client:/client
      - shared:/shared
    environment:
      SPRING_APPLICATION_NAME: EC_DE
      SPRING_KAFKA_BOOTSTRAP-SERVERS: ${IP_PC_1}:9092
      CENTRAL_IP: ${IP_PC_2}
      CENTRAL_PORT: ${CENTRAL_PORT}
      TAXI_ID: $i
      SENSOR_PORT: ${sensor_port}
      REGISTRY_URL: https://192.168.100.5:8443/taxis
    ports:
      - "${host_port}:${container_port}"
    stdin_open: true
    tty: true
    depends_on:
      - ec_registry
    networks:
      easycab_network:
        ipv4_address: ${ec_de_ip}
EOF
  done
}

# Función para añadir servicios EC_Customer en PC1
add_ec_customer_services() {
  for i in $(seq 1 "$NUM_CLIENTES"); do
    client_id=$(echo "$i" | awk '{printf "%c", $1 + 96}')  # Genera letras 'a', 'b', 'c', 'd', etc.
    customer_port_var="CUSTOMER_PORT_$(echo "$client_id" | tr '[:lower:]' '[:upper:]')"
    customer_port="${!customer_port_var}"

    if [ -z "$customer_port" ]; then
      echo "Error: CUSTOMER_PORT_${client_id^^} no está configurado en .env."
      exit 1
    fi

    ec_customer_ip="192.168.100.$((200 + i))"

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

# Añadir servicios según el tipo de PC
case "$1" in
  1)  # PC1: Kafka, Zookeeper y EC_Customer
    echo "Configurando para PC1: Kafka, Zookeeper y EC_Customer"
    add_zookeeper_service
    add_kafka_service
    add_ec_customer_services
    ;;
  2)  # PC2: Postgres, EC_Central, CTC, Nginx + Elasticsearch, Logstash, Kibana
    echo "Configurando para PC2: Postgres, EC_Central, CTC, Nginx y Logging"
    add_postgres_service
    add_ec_central_service
    add_ec_ctc_service
    add_nginx_service
    add_logging_services
    ;;
  3)  # PC3: EC_Registry y EC_DE
    echo "Configurando para PC3: EC_Registry y EC_DE"
    add_ec_registry_service
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
  server:
  shared:
  client:
  esdata:

networks:
  easycab_network:
    driver: bridge
    ipam:
      config:
        - subnet: 192.168.100.0/24
  shared-network:
    driver: bridge
    ipam:
      config:
        - subnet: 192.168.101.0/24
EOF

echo "Archivo ${DOCKER_COMPOSE_FILE} creado con éxito."
echo
echo "Para iniciar los servicios, usa el siguiente comando:"
echo "docker-compose -f ${DOCKER_COMPOSE_FILE} up --build -d"