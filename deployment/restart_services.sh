#!/bin/bash

# Encuentra todos los contenedores que contienen 'ec-customer' o 'ec-de' en el nombre
containers=$(sudo docker container ls --filter "name=ec_customer" --filter "name=ec_de" --format "{{.ID}}")

if [ -z "$containers" ]; then
  echo "No se encontraron contenedores que contengan 'ec-customer' o 'ec-de' para reiniciar."
else
  echo "Reiniciando contenedores que contienen 'ec-customer' y 'ec-de'..."
  sudo docker container restart $containers
  echo "Contenedores reiniciados."
fi