#!/bin/bash

SERVER_IP="192.168.100.5"
SERVER_PORT="8443"
CERT_FILE="/shared/server_cert.cer"
TRUSTSTORE_FILE="/client/truststore.jks"
TRUSTSTORE_PASSWORD="your_password"
APP_JAR="/app/EC_DE.jar"

## 1. Verificar que el servidor esté accesible
##echo "Verificando conectividad con el servidor ${SERVER_IP}:${SERVER_PORT}..."
##nc -zv "$SERVER_IP" "$SERVER_PORT"
#if [ $? -ne 0 ]; then
#    echo "Error: No se pudo conectar al servidor en ${SERVER_IP}:${SERVER_PORT}. Verifica la red y el servidor."
#    exit 1
#fi

# 2. Descargar el certificado del servidor (si no está disponible localmente)
if [ ! -f "$CERT_FILE" ]; then
    echo "Descargando el certificado del servidor..."
    echo -n | openssl s_client -connect "${SERVER_IP}:${SERVER_PORT}" -servername "$SERVER_IP" | \
        openssl x509 > "$CERT_FILE"
    if [ $? -ne 0 ]; then
        echo "Error: No se pudo descargar el certificado del servidor."
        exit 1
    fi
fi

# 3. Crear el truststore e importar el certificado del servidor
echo "Creando el truststore e importando el certificado del servidor..."
if [ -f "$TRUSTSTORE_FILE" ]; then
    rm -f "$TRUSTSTORE_FILE"
fi

keytool -import -trustcacerts -alias server-cert -file "$CERT_FILE" \
    -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD" -noprompt

if [ $? -ne 0 ]; then
    echo "Error: No se pudo importar el certificado al truststore."
    exit 1
fi

# 4. Ejecutar la aplicación cliente
echo "Iniciando la aplicación cliente..."
java -Djavax.net.ssl.trustStore="$TRUSTSTORE_FILE" \
     -Djavax.net.ssl.trustStorePassword="$TRUSTSTORE_PASSWORD" \
     -Djavax.net.debug=ssl,handshake \
     -jar "$APP_JAR"

if [ $? -ne 0 ]; then
    echo "Error: La aplicación cliente no pudo iniciarse correctamente."
    exit 1
fi
