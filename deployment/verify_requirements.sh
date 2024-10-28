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