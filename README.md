### README: Configuración y Despliegue de EasyCab con Docker Compose

Este documento explica cómo configurar y desplegar los servicios del proyecto EasyCab utilizando Docker Compose y un script de despliegue para simplificar la configuración en diferentes máquinas. El proyecto está distribuido en varios microservicios que interactúan entre sí y se comunican a través de Kafka.

### Estructura del Proyecto

El sistema está dividido en varios servicios, cada uno con su propio contenedor Docker:

- **Postgres**: Base de datos para almacenar la información de la aplicación.
- **Kafka** y **Zookeeper**: Para la mensajería entre los diferentes componentes.
- **EC_Central**: Central de control de la aplicación, que coordina la comunicación entre los clientes y los taxis.
- **EC_DE**: Módulo de Digital Engine que gestiona la lógica de los taxis.
- **EC_Customer**: Módulo para la comunicación con los clientes.
- **EC_S**: Módulo de sensores para simular la interacción con los taxis.

### Prerrequisitos

- Tener Docker y Docker Compose instalados.
- Configurar las IPs en el archivo `.env` para adaptarse a la red de las máquinas.

### Configuración de Variables de Entorno

El archivo `.env` en la carpeta raíz define las IPs de cada máquina:

```
KAFKA_IP=192.168.1.10
ZOOKEEPER_IP=192.168.1.10
CENTRAL_IP=192.168.1.10
DE_IP=192.168.1.20
SENSOR_IP=192.168.1.20
SENSOR_PORT=8083
```

Asegúrate de ajustar las IPs de acuerdo con las direcciones de tu red.

### Docker Compose

El archivo de configuración de Docker Compose define todos los servicios mencionados anteriormente, sus dependencias, y cómo interactúan entre sí. Puedes encontrar el archivo `docker-compose.yml` [aquí](./docker-compose.yml). Este archivo utiliza las variables de entorno definidas en el archivo `.env` para la configuración de los servicios.

### Script de Despliegue

Existen dos versiones del script de despliegue para facilitar el proceso según el sistema operativo:

- **Script para Unix (Linux/macOS):** [`deploy_easycab.sh`](./deployment/deploy_easycab.sh)
- **Script para Windows:** [`deploy_easycab.bat`](./deployment/deploy_easycab.bat)

### Uso de los Scripts de Despliegue

1. **Configura el archivo `.env`** con las IPs de las máquinas y los puertos necesarios.
2. **Ejecuta el script de despliegue** en cada máquina correspondiente:

    - En sistemas Unix:
      ```bash
      ./deployment/deploy_easycab.sh
      ```
    - En sistemas Windows:
      ```cmd
      deployment\deploy_easycab.bat
      ```

3. Selecciona el tipo de máquina cuando se te pida:

    - En PC1, selecciona la opción `1` para iniciar Kafka, Zookeeper, EC_Central y Postgres.
    - En PC2, selecciona la opción `2` para iniciar EC_DE y EC_S.
    - En PC3, selecciona la opción `3` para iniciar EC_Customer.

### Cómo Funcionan los Scripts

Ambos scripts realizan las siguientes tareas:

1. **Cargan las variables de entorno** desde el archivo `.env`.
2. **Inician los servicios correspondientes** según la opción seleccionada por el usuario, utilizando `docker-compose` para desplegar los contenedores.

### Notas Importantes

- Asegúrate de que las IPs configuradas en el archivo `.env` corresponden a las IPs de cada máquina en la red.
- Los servicios Kafka y Zookeeper deben iniciarse antes de los servicios que dependan de ellos (como `EC_Central`, `EC_DE` y `EC_S`).
- El script de despliegue facilita la configuración automática, cargando las variables de entorno y utilizando `docker-compose` para crear los servicios.

### Problemas Comunes

- **Error de conexión a Postgres**: Verifica que el contenedor de Postgres esté corriendo y que la IP y las credenciales sean correctas.
- **Error de conexión a Kafka**: Asegúrate de que Kafka esté disponible en la IP configurada en el archivo `.env`.
- **Faltan dependencias**: Asegúrate de que los servicios `kafka` y `zookeeper` estén iniciados antes de levantar `EC_Central` o cualquier otro servicio que los necesite.

Para más detalles, revisa la configuración de cada servicio en el archivo [`docker-compose.yml`](./docker-compose.yml).