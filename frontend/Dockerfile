# Etapa de build
FROM node:14-slim as build

WORKDIR /app

# Copia os arquivos package.json e package-lock.json
COPY package*.json ./

# Instala as dependências
RUN npm install

# Copia o restante do código
COPY . .

# Compila a aplicação
RUN npm run build

# Etapa de produção
FROM nginx:stable-alpine-slim

# Copia os arquivos compilados para a pasta padrão do nginx
COPY --from=build /app/build /usr/share/nginx/html

# Copia o script para injetar variáveis de ambiente
COPY ./env.sh /usr/share/nginx/html/env.sh

# Expõe a porta 80
EXPOSE 80

# Copia o script de entrada
COPY ./entrypoint.sh /entrypoint.sh

# Adiciona permissão de execução aos scripts
RUN chmod +x /usr/share/nginx/html/env.sh /entrypoint.sh

# Inicia o nginx com o script de ambiente
# ENTRYPOINT ["/entrypoint.sh"]
CMD ["/bin/sh", "-c", "/usr/share/nginx/html/env.sh && nginx -g 'daemon off;'"]


# docker build -t wolivej/front-banco-app .

# docker run -it -p 3000:80 -p 8080:8080 -e BANK_CODE=1 wolivej/front-app
# docker run -it --network=host -e BANK_CODE=1 wolivej/front-app

