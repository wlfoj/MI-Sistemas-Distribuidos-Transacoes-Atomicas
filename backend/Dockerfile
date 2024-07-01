#  ========== COM O BUILD ========== #
# Build Stage
FROM maven:3.8.3-openjdk-17-slim AS build
WORKDIR /app
COPY . .
# Compila a aplicação
RUN mvn clean install -DskipTests

# Final Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/transacoes-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
# Executa o jar
CMD ["java", "-jar", "app.jar"]


# docker build -t wolivej/banco-app .
# docker run --network=host -it -e BANK_CODE=1  wolivej/banco-app






