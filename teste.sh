#!/bin/bash

# URLs da API
BANK_URL1=""http://172.16.103.10:8080/""
BANK_URL2=""http://172.16.103.11:8080/""
BANK_URL3=""http://172.16.103.12:8080/""

# Nome dos Bancos
BANK_CODE1="1"
BANK_CODE2="2"
BANK_CODE3="3"

# Usuários de teste
CPF1="445677"
CPF2="111111"
CPF3="888888"
CPF4="444444"
CPF5="555555"
CPF6="666666"
CNPJ1="121212"
CNPJ2="131313"

PASSWORD1="445677"
PASSWORD2="111111"
PASSWORD3="888888"
PASSWORD4="444444"
PASSWORD5="555555"
PASSWORD6="666666"
PASSWORD7="121212"
PASSWORD8="131313"


# =============================================================== #
# ========= FUNÇÕES BÁSICAS PARA REQUISIÇÕES POST E GET ========= #
# =============================================================== #
# Função para realizar uma requisição POST com JSON
post_request() {
    local url=$1
    local data=$2
    curl -s -X POST -H "Content-Type: application/json" -d "$data" "$url"
}

# Função para realizar uma requisição GET
get_request() {
    local url=$1
    curl -s -X GET "$url"
}


# =============================================================== #
      # ========= FUNÇÕES DE CASOS DE USO ========= #
# =============================================================== #
# Função para cria contas
create_account() {
    echo "Testando criação de conta..."
    local data=$2
    local url=$1
data=$(cat <<EOF
$data
EOF
)
    post_request "$url/user/createAccount" "$data"
}


transaction() {
    url=$1 
    transaction_type=$2
    source_bank_code=$3
    source_cpf=$4
    operations=$5
    echo -e "\nRealizando $transaction_type"
    data=$(cat <<EOF
{
    "transactionType": "$transaction_type",
    "source": {
        "bankCode": "$source_bank_code",
        "cpf": "$source_cpf"
    },
    "operations": $operations
}
EOF
)
    post_request "$url/user/openTransaction" "$data"
}


# =============================================================== #
        # ========= BLOCO DE CRIAÇÃO DE CONTAS ========= #
# =============================================================== #
# CPF1 criou conta individual em todos os bancos
create_account $BANK_URL1 '{ "cpf1": "'$CPF1'", "password": "'$PASSWORD1'", "accountType": "FISICA" }'
create_account $BANK_URL2 '{ "cpf1": "'$CPF1'", "password": "'$PASSWORD2'", "accountType": "FISICA" }'
create_account $BANK_URL3 '{ "cpf1": "'$CPF1'", "password": "'$PASSWORD1'", "accountType": "FISICA" }'
# CPF2 só cricou conta individual no banco 2
create_account $BANK_URL2 '{ "cpf1": "'$CPF2'", "password": "'$PASSWORD2'", "accountType": "FISICA" }'
# CPF1 criou conjunta com CPF3 no banco 3 (CPF3 Não tem conta individual no sistema)
create_account $BANK_URL3 '{ "cpf1": "'$CPF1'", "cpf2": "'$CPF3'" ,"password": "'$PASSWORD3'", "accountType": "CONJUNTA" }'
# CPF2 criou conjunta com CPF3 no banco 3 (CPF3 Não tem conta individual no sistema)
create_account $BANK_URL3 '{ "cpf1": "'$CPF2'", "cpf2": "'$CPF3'" ,"password": "'$PASSWORD4'", "accountType": "CONJUNTA" }'
# CNPJ1 criou uma conta individual em banco 1 e banco 2
create_account $BANK_URL1 '{ "cnpj": "'$CNPJ1'", "password": "'$PASSWORD5'", "accountType": "JURIDICA" }'
create_account $BANK_URL2 '{ "cnpj": "'$CNPJ1'", "password": "'$PASSWORD6'", "accountType": "JURIDICA" }'

# =============================================================== #
          # ========= BLOCO DE DEPOSITOS ========= #
# =============================================================== #
# Todas as contas Individuais de CPF1 recebem 800 R$ 
transaction $BANK_URL1 "DEPOSIT" $BANK_CODE1 $CPF1 '[{"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF1'", "value": 800}]'
transaction $BANK_URL1 "DEPOSIT" $BANK_CODE1 $CPF1 '[{"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF1'", "value": 800}]'
transaction $BANK_URL1 "DEPOSIT" $BANK_CODE1 $CPF1 '[{"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1'", "value": 800}]'   
# Todas as contas Individuais de CPF2 recebem 240 R$                                       
transaction $BANK_URL2 "DEPOSIT" $BANK_CODE2 $CPF2 '[{"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF2'", "value": 240}]' 

echo "Depósito em uma conta que não existe"
transaction $BANK_URL2 "DEPOSIT" $BANK_CODE2 $CPF1 '[{"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF2'", "value": 2000}]' 
echo "Depósito em uma conta conjunta"
# Conta conjunta de CPF1 e CPF3 recebe 2000 R$
transaction $BANK_URL2 "DEPOSIT" $BANK_CODE2 $CPF1 '[{"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF3'", "value": 2000}]' 

# =============================================================== #
          # ========= BLOCO DE TRANSFERENCIA ========= #
# =============================================================== #
# O CPF1 fez login no banco1 e vai transferir dinheiro das suas contas do banco 1, 2 e 3 para o CNPJ1 no banco 1
transaction $BANK_URL1 "TRANSFER" $BANK_CODE1 $CPF1 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF1'", "value": 80},
                                           {"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1'", "value": 80},
                                           {"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF1'", "value": 80},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CNPJ1'", "value": 240}]' &

# CPF2 vai retirar dinheiro da sua conta no banco 1 e transferir para cpf1 no banco 1 (VAI DAR ERRO PQ N EXISTE ESSA CONTA DO BANCO 2)
transaction $BANK_URL2 "TRANSFER" $BANK_CODE2 $CPF2 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF2'", "value": 100},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF1'", "value": 100}]' &

# CNPJ1 vai retirar dinheiro da sua conta no banco 1 e transferir para cpf1 no banco 1
transaction $BANK_URL2 "TRANSFER" $BANK_CODE2 $CNPJ1 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CNPJ1'", "value": 2},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF1'", "value": 2}]' &

# CPF3 vai retirar dinheiro da sua conta no banco 2 e transferir para cpf1 no banco 1
transaction $BANK_URL2 "TRANSFER" $BANK_CODE2 $CPF3 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF3'", "value": 100},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF1'", "value": 100}]' &

# CPF3 vai retirar dinheiro da sua conta no banco 2 e transferir para cpf2 no banco 2
transaction $BANK_URL2 "TRANSFER" $BANK_CODE2 $CPF3 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF3'", "value": 100},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF2'", "value": 100}]' &

# CPF3 vai retirar dinheiro da sua conta no banco 2 e transferir para cnjp1 no banco2
transaction $BANK_URL2 "TRANSFER" $BANK_CODE2 $CPF3 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF3'", "value": 100},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CNPJ1'", "value": 100}]' &

# CPF3 vai retirar dinheiro da sua conta no banco 2 e transferir para cpf1 no banco2
transaction $BANK_URL2 "TRANSFER" $BANK_CODE2 $CPF3 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF3'", "value": 100},
                                          {"operationType": "DEPOSIT", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF1'", "value": 100}]' &

wait

# =============================================================== #
          # ========= BLOCO DE PAGAMENTOS ========= #
# =============================================================== #
## FOCO EM ZERAR TODOS OS SALDOS ##
# Zerando o saldo de CNPJ1
transaction $BANK_URL2 "PAYMENT" $BANK_CODE2 $CNPJ1 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CNPJ1'", "value": 238},
                                          {"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CNPJ1'", "value": 100}]' &

# Zerando o saldo de CPF3
transaction $BANK_URL3 "PAYMENT" $BANK_CODE3 $CPF3 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1$CPF3'", "value": 1600},
                                          {"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF2$CPF3'", "value": 0}]' &

# Zerando o saldo de CPF2
transaction $BANK_URL2 "PAYMENT" $BANK_CODE2 $CPF2 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF2'", "value": 340}]' &

# Zerando o saldo de CPF1
transaction $BANK_URL1 "PAYMENT" $BANK_CODE1 $CPF1 '[{"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE1'", "accountCode": "'$CPF1'", "value": 822},
                                          {"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE2'", "accountCode": "'$CPF1'", "value": 820},
                                          {"operationType": "WITHDRAW", "bankCode": "'$BANK_CODE3'", "accountCode": "'$CPF1'", "value": 720}]' &
wait
