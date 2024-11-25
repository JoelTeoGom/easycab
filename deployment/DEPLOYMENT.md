### Paso 1: Requisitos Previos

Antes de comenzar, asegúrate de que tienes:

- **Docker** y **Docker Compose** instalados en cada máquina.
- **Conexión de red** entre las máquinas para que puedan comunicarse utilizando las direcciones IP definidas.

### Paso 2: Crear el Script de Verificación de Requisitos (`verify_requirements.sh`)

Este script verificará si Docker y Docker Compose están instalados en la máquina y, de no estarlo, lo informará. Luego, ejecutará `setup_env.sh` para completar la configuración de `.env`.


```bash
#!/bin/bash

# Función para instalar Docker
install_docker() {
  echo "Instalando Docker..."
  sudo apt update
  sudo apt install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

  # Agrega la clave GPG oficial de Docker
  sudo mkdir -m 0755 -p /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

  # Configura el repositorio
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

  sudo apt update
  sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

  # Agrega el usuario actual al grupo docker para evitar usar sudo
  sudo usermod -aG docker $USER
}

# Función para instalar Docker Compose
install_docker_compose() {
  echo "Instalando Docker Compose..."
  sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
}

# Verifica si Docker está instalado
if ! command -v docker &> /dev/null; then
  echo "Docker no está instalado. Procediendo con la instalación..."
  install_docker
else
  echo "Docker ya está instalado."
fi

# Verifica si Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
  echo "Docker Compose no está instalado. Procediendo con la instalación..."
  install_docker_compose
else
  echo "Docker Compose ya está instalado."
fi

# Verifica si el archivo setup_env.sh existe
if [[ ! -f "setup_env.sh" ]]; then
  echo "El archivo setup_env.sh no se encuentra en el directorio actual. Asegúrate de que esté presente."
  exit 1
fi

# Ejecuta el script de configuración del archivo .env
echo "Docker y Docker Compose están instalados y configurados."
echo "Ejecutando el script de configuración del archivo .env..."
chmod +x setup_env.sh
./setup_env.sh

# Mensaje final
echo "Configuración completada. El archivo .env ha sido verificado y configurado."
echo "Es posible que necesites cerrar sesión y volver a iniciar sesión para aplicar los permisos de Docker."
```

Este script:
1. Verifica la presencia de Docker y Docker Compose.
2. Si ambos están instalados, ejecuta `setup_env.sh` para completar la configuración.

Para ejecutarlo:

```bash
chmod +x verify_requirements.sh
./verify_requirements.sh
```

### Paso 3: Script de Configuración de Variables de Entorno (`setup_env.sh`)

`setup_env.sh` crea y configura el archivo `.env` automáticamente con valores predeterminados. No necesitarás modificar `.env` manualmente, ya que este script generará todas las variables requeridas.

### Paso 4: Ejecución en Cada Máquina

Cada PC tiene un papel específico en la arquitectura de EasyCab. Aquí se explica cómo configurar cada máquina:

---

### Configuración de **PC1**: Kafka, Zookeeper y EC_Customer
Asegúrate primero de cambiar las IP de cada máquina en el archivo `setup_env.sh` antes de ejecutar el script de verificación y configuración.
1. **Ejecuta el script de verificación y configuración** en PC1:

   ```bash
   ./verify_requirements.sh
   ```

   Este comando verifica que Docker y Docker Compose estén instalados y luego configura el archivo `.env`.

2. **Ejecuta el script de despliegue** para iniciar los servicios de PC1:

   ```bash
   ./deployment/deploy_easycab.sh 1 <NUM_CLIENTES>
   ```

   Reemplaza `<NUM_CLIENTES>` con el número de instancias de `EC_Customer` que deseas desplegar. Por ejemplo, para iniciar 3 clientes:

   ```bash
   ./deployment/deploy_easycab.sh 1 3
   ```

---

### Configuración de **PC2**: EC_Central y Postgres

1. **Ejecuta el script de verificación y configuración** en PC2:

   ```bash
   ./verify_requirements.sh
   ```

2. **Ejecuta el script de despliegue** para iniciar los servicios de PC2:

   ```bash
   ./deployment/deploy_easycab.sh 2
   ```

   Este comando iniciará los servicios `EC_Central` y `Postgres` en PC2.

---

### Configuración de **PC3**: EC_DE y EC_S

1. **Ejecuta el script de verificación y configuración** en PC3:

   ```bash
   ./verify_requirements.sh
   ```

2. **Ejecuta el script de despliegue** para iniciar los servicios de PC3:

   ```bash
   ./deployment/deploy_easycab.sh 3 <NUM_DE>
   ```

   Reemplaza `<NUM_DE>` con el número de instancias de `EC_DE` (taxis) que deseas desplegar. Por ejemplo, para iniciar 2 taxis:

   ```bash
   ./deployment/deploy_easycab.sh 3 2
   ```
### Reinicio para activar los customers
Una vez que todos los servicios estén en funcionamiento, hay que reiniciar en el PC1 los customer para que empiecen a realizar peticiones.
Se puede usar el script `restart_services.sh`:
```bash
./restart_services.sh
```

---

### Paso 5: Validación de Despliegue

Verifica que cada servicio esté funcionando correctamente ejecutando `docker ps` en cada máquina para asegurarte de que todos los contenedores necesarios estén en ejecución.

---

### Explicación Detallada de los Scripts

#### `setup_env.sh`

El script `setup_env.sh` verifica que existan las variables necesarias en el archivo `.env` y, si no existen, las agrega con valores predeterminados. Este script configura automáticamente los valores de IP y puertos para los servicios en función de las direcciones de red definidas.

#### `verify_requirements.sh`

`verify_requirements.sh` comprueba que Docker y Docker Compose estén presentes en la máquina antes de continuar. Después de confirmar que están instalados, ejecuta `setup_env.sh` para completar la configuración de `.env`.

---

### Notas Adicionales

- **IPs de Red**: Las IPs en `.env` deben coincidir con las IPs asignadas a cada PC en tu red. Estas IPs están definidas en `setup_env.sh` y se configuran automáticamente.
- **Puertos Personalizados**: Si necesitas puertos específicos o adicionales, puedes modificar `setup_env.sh` para reflejar estos cambios.
