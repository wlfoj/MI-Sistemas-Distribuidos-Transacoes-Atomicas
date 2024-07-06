#!/bin/sh

# Cria o arquivo env-config.js com as variáveis de ambiente
echo "window._env_ = {" > /usr/share/nginx/html/env-config.js
echo "  BANK_CODE: \"$BANK_CODE\"," >> /usr/share/nginx/html/env-config.js
echo "  BANK_STRUCT: '$BANK_STRUCT'" >> /usr/share/nginx/html/env-config.js
echo "};" >> /usr/share/nginx/html/env-config.js

exec "$@"
