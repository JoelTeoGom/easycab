#!/bin/bash

# Encuentra todos los contenedores ec-customer y ec-de y reinícialos
containers=$(sudo docker container ls --filter "name=practica-easycab_ec_customer" --filter "name=practica-easycab_ec_de" --format "{{.ID}}")

if [ -z "$containers" ]; then
  echo "No se encontraron contenedores ec-customer o ec-de para reiniciar."
else
  echo "Reiniciando contenedores ec-customer y ec-de..."
  sudo docker container restart $containers
  echo "Contenedores reiniciados."
fi