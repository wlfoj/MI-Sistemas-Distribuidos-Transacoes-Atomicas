#!/bin/sh

# Executa o script env.sh para criar o arquivo env-config.js
/usr/share/nginx/html/env.sh

# Inicia o Nginx
nginx -g 'daemon off;'
