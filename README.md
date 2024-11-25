### README: Configuración y Despliegue de EasyCab con Docker Compose

Este proyecto despliega una arquitectura distribuida para simular un sistema de gestión de taxis, donde cada componente representa una función dentro del ecosistema EasyCab. Los microservicios se orquestan con Docker Compose, y un script de despliegue automatiza la configuración en diferentes máquinas. La comunicación entre los servicios se maneja principalmente a través de Kafka.

### Estructura del Proyecto

EasyCab está compuesto por los siguientes servicios:

- **Postgres**: Base de datos que almacena información de la aplicación.
- **Kafka** y **Zookeeper**: Gestionan la mensajería entre los componentes.
- **EC_Central**: Controla la comunicación entre clientes y taxis.
- **EC_DE**: Módulo de taxis que maneja la lógica de operación de cada taxi.
- **EC_Customer**: Interfaz de comunicación con los clientes.
- **EC_S**: Simula sensores conectados a los taxis.

### Prerrequisitos

1. Instalar Docker y Docker Compose.
2. Configurar las IPs en el archivo `.env` para que se adapten a la red de cada máquina en el sistema distribuido.

### Configuración de Variables de Entorno

Las configuraciones de red y puertos se establecen en el archivo `.env` ubicado en la carpeta raíz del proyecto. Aquí se definen las IPs para los diferentes servicios y las máquinas:

```dotenv
# Ejemplo de variables en el archivo .env
KAFKA_IP=192.168.1.10
ZOOKEEPER_IP=192.168.1.10
CENTRAL_IP=192.168.1.20
DE_IP=192.168.1.30
POSTGRES_IP=192.168.1.40
```

Asegúrate de ajustar las IPs de cada máquina para que coincidan con las direcciones de red correspondientes.

### Generación de Archivos Docker Compose

El archivo principal de Docker Compose (`docker-compose.yml`) se utiliza junto a un script para generar configuraciones personalizadas por cada tipo de máquina, permitiendo que cada computadora en la red ejecute los servicios correspondientes.

- **docker-compose.yml**: Contiene la definición base de los servicios y redes del sistema.
- **docker-compose.override.yml**: Utilizado para configuraciones personalizadas en máquinas específicas.

### Script de Despliegue

Para obtener más información sobre cómo desplegar EasyCab, consulta la [documentación de despliegue](./deployment/DEPLOYMENT.md).

### Cómo Funcionan los Scripts

1. **Carga las variables de entorno** desde el archivo `.env`.
2. **Genera archivos Docker Compose personalizados** en función de la selección del usuario.
3. **Despliega los servicios** con Docker Compose, ajustando las configuraciones necesarias para cada máquina.

El script de despliegue simplifica el proceso de configuración y despliegue de servicios distribuidos en múltiples máquinas.

### Script de Configuración Automática de Variables de Entorno: `setup_env.sh`

Este script asegura que el archivo `.env` contenga todas las variables necesarias y corrige los puertos incorrectos. Ejecuta el script con:

```bash
./setup_env.sh
```

### Ejemplo de Configuración por Tipo de Máquina

Para iniciar servicios en una máquina específica, el script de despliegue generará un archivo Docker Compose correspondiente:

- **PC1**: Kafka, Zookeeper y EC_Customer
- **PC2**: EC_Central y Postgres
- **PC3**: EC_DE y EC_S

Cada archivo `docker-compose` específico se generará en función de la configuración seleccionada.

### Notas Importantes

- **Asegúrate de que las IPs y puertos** en el archivo `.env` coincidan con las configuraciones de red y los servicios.
- **Kafka y Zookeeper deben estar activos** antes de iniciar los servicios que dependen de ellos.
- **La red** definida en `docker-compose` debe tener configurada una subred personalizada para habilitar IPs fijas.

### Resolución de Problemas Comunes

- **Error de conexión a Postgres**: Verifica que el contenedor esté activo y que la configuración de IPs y credenciales sea correcta.
- **Problemas de conexión a Kafka**: Asegúrate de que Kafka esté disponible en la IP configurada.
- **Variables faltantes en `.env`**: Usa `setup_env.sh` para agregar variables faltantes o corregir errores en las configuraciones de puerto.
