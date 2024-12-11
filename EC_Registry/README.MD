Aquí tienes un resumen claro y estructurado de los comandos que necesitas ejecutar, quién debe hacerlo y qué hacen:

---

### **En el servidor EC_Registry**

1. **Generar el keystore del servidor (`keystore.p12`)**
   **Comando**:
   ```bash
   keytool -genkeypair -alias myalias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650
   ```
   **Responsable**: Administrador del servidor EC_Registry.

   **Qué hace**:
    - Crea un archivo `keystore.p12` que contiene la clave privada y un certificado autofirmado para habilitar HTTPS en el servidor.

2. **Exportar el certificado público del servidor**
   **Comando**:
   ```bash
   keytool -export -alias myalias -file server_cert.cer -keystore keystore.p12 -storetype PKCS12
   ```
   **Responsable**: Administrador del servidor EC_Registry.

   **Qué hace**:
    - Extrae el certificado público del servidor desde el keystore y lo guarda en un archivo `server_cert.cer`.
    - Este archivo se distribuye a los clientes para que confíen en el servidor.

---

### **En cada cliente EC_DE**

1. **Crear un truststore del cliente (`truststore.jks`)**
   **Comando**:
   ```bash
   keytool -import -trustcacerts -alias ec_registry -file server_cert.cer -keystore truststore.jks
   ```
   **Responsable**: Administrador de cada cliente EC_DE.

   **Qué hace**:
    - Importa el certificado público del servidor (`server_cert.cer`) en un truststore (`truststore.jks`) para que el cliente confíe en el servidor.

2. **(Opcional) Verificar el contenido del truststore**
   **Comando**:
   ```bash
   keytool -list -v -keystore truststore.jks
   ```
   **Responsable**: Administrador de cada cliente EC_DE.

   **Qué hace**:
    - Verifica que el truststore contiene correctamente el certificado del servidor.

---

### **Resumen Total**

#### **En el Servidor EC_Registry**
- **2 comandos**:
    1. Generar el `keystore.p12`.
    2. Exportar el certificado público (`server_cert.cer`).

#### **En Cada Cliente EC_DE**
- **1 obligatorio y 1 opcional**:
    1. Crear el `truststore.jks` (obligatorio).
    2. Verificar el contenido del `truststore.jks` (opcional).

---

De este modo, tendrás la configuración completa para que el servidor y los clientes puedan comunicarse de forma segura.