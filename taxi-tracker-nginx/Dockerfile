# Usa la imagen oficial de Nginx
FROM nginx:alpine

# Copia la configuración de Nginx personalizada
COPY nginx.conf /etc/nginx/nginx.conf

# Copia los archivos estáticos de la página web
COPY index.html /usr/share/nginx/html/

# Exponer el puerto 80 para servir la página web
EXPOSE 80

# Iniciar Nginx
CMD ["nginx", "-g", "daemon off;"]
