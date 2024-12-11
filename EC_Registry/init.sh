#!/bin/bash

KEYSTORE_PASSWORD="your_password"
KEY_ALIAS="myalias"
KEYSTORE_FILE="/server/keystore.p12"
CERT_FILE="/shared/server_cert.cer"
SAN_CONFIG_FILE="/tmp/san.cnf"

# 1. Crear un archivo de configuración para SAN
echo "Creando archivo de configuración para SAN..."
cat > "$SAN_CONFIG_FILE" << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
C = ES
ST = Madrid
L = Madrid
O = EasyCab
OU = IT
CN = 192.168.100.5

[v3_req]
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
IP.1 = 192.168.100.5
DNS.1 = localhost
EOF

# 2. Eliminar keystore existente (si lo hay)
if [ -f "$KEYSTORE_FILE" ]; then
    echo "Eliminando keystore existente en $KEYSTORE_FILE..."
    rm -f "$KEYSTORE_FILE"
fi

# 3. Generar clave privada y CSR
echo "Generando clave privada y solicitud de firma de certificado (CSR)..."
openssl req -new -newkey rsa:2048 -nodes -keyout /tmp/server.key -out /tmp/server.csr -config "$SAN_CONFIG_FILE"

if [ $? -ne 0 ]; then
    echo "Error: No se pudo generar la CSR."
    exit 1
fi

# 4. Generar certificado autofirmado con SAN
echo "Generando certificado autofirmado..."
openssl x509 -req -days 3650 -in /tmp/server.csr -signkey /tmp/server.key -out /tmp/server.crt -extensions v3_req -extfile "$SAN_CONFIG_FILE"

if [ $? -ne 0 ]; then
    echo "Error: No se pudo generar el certificado."
    exit 1
fi

# 5. Crear keystore con el certificado
echo "Importando certificado y clave privada al keystore..."
openssl pkcs12 -export -in /tmp/server.crt -inkey /tmp/server.key -out "$KEYSTORE_FILE" -name "$KEY_ALIAS" -passout pass:"$KEYSTORE_PASSWORD"

if [ $? -ne 0 ]; then
    echo "Error: No se pudo crear el keystore."
    exit 1
fi

# 6. Exportar el certificado en formato PEM
echo "Exportando el certificado público..."
openssl x509 -in /tmp/server.crt -out "$CERT_FILE" -outform PEM

if [ $? -ne 0 ]; then
    echo "Error: No se pudo exportar el certificado."
    exit 1
fi

echo "Certificado exportado correctamente en $CERT_FILE."

# 7. Iniciar el servidor con depuración SSL habilitada
echo "Iniciando el servidor con depuración SSL habilitada..."
java -Djavax.net.ssl.keyStore="$KEYSTORE_FILE" \
     -Djavax.net.ssl.keyStorePassword="$KEYSTORE_PASSWORD" \
     -Djavax.net.debug=ssl,handshake \
     -jar /app/EC_Registry.jar
