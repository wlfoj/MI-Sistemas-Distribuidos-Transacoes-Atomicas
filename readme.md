# TEC502 MI-Concorrência-Conectividade: Transações Bancárias Distribuídas

### Sumário
------------
+ [Como executar no LARSID](#0-como-executar-no-larsid)
+ [Introdução](#1-introdução)
+ [Visão geral](#2-visão-geral)

+ [Discussão sobre produto](#3-discussão-sobre-o-produto)
+ &nbsp;&nbsp;&nbsp;[Transações Atômicas](#31-transação-atômica-e-2pc)
+ &nbsp;&nbsp;&nbsp;[Confiabilidade](#32-confiabilidade-e-resiliência)

+ [Protocolos de Comunicações e Mensagens](#4-protocolos-de-comunicações-e-mensagens)
+ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[Entre Banco e Usuário](#42-entre-banco-e-interface-gráfica)
+ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[Entre Bancos](#43-entre-bancos-banco-a-banco)
+ [Conclusão](#5-conclusões)

# 0. Como executar no LARSID
O sistema faz o uso do Java17+ e do React 18.3.1, porém o ambiente é construído por meio de containers do Docker.

### 1. Faça pull das imagens utilizadas
Abra o terminal e execute os comandos abaixo para baixar as imagens da API do Banco e da Interface gráfica no Doocker Hub.
```
docker pull wolivej/back-banco-app
docker pull wolivej/front-banco-app
```

### 2. Iniciando o sistema
Como o banco é entendido pelo conjunto da API e da interface gráfica, será preciso instância os dois serviços em um mesmo container. Sendo assim, basta acessar um computador, conforme a lista em  backend\src\main\java\transacoes_distribuidas\infra\Consortium.java e modificar o arquivo compose.yaml para que as váriaveis de ambiente BANK_CODE estejam coerentes com o ip do computador utilizado. Por exemplo, se você estiver no computador com ip 172.16.103.8 use o BANK_CODE=1. Feito isso, basta executar o código abaixo.
```
docker-compose up
```
Repita o processo em cada computador diferente, se atentando aos que apresentam o ip especificado no arquivo de Consortium.java.

# 1. Introdução
O avanço da tecnologia tem possibilitado a integração de diversos dispositivos por meio da internet, proporcionando facilitações em atividades de diversas naturezas. A gerência de diversos dispositivos que estão distribuídos em nós pela internet se mostra uma complexidade a ser lidada quando se fala em Internet das Coisas (do inglês Internet of Things, IoT). Sistemas bancários em que não há a presença de uma unidade controladora central (como o Banco Central), são um verdadeiro desafio. Open Banking é o nome dado ao sistema onde bancos se unem e expões suas API's a outros (com autorização do cliente), para que um determinado cliente possa ter acesso a seus dados de diferente meios, tornando o dia a dia do cliente ainda mais prático. 

Devido a natureza de sistemas distribuídos, os nós do sistema devem entrar em consenso e estabelecer um protocolo bem definido para troca de mensagens. Garantir que todos os nós de um sistema enxerguem o mesmo estado é uma tarefa nada trivial. Para garantir essa consistência e atomicidade se usa protocolos de transações atômicas, como o Two Phase Commit. Uma característica de sistemas distribuídos para transações que podem partir de qualquer nó é o uso da arquitetura peer-to-peer. 

Aplicações peer-to-peer (P2P) são sistemas distribuídos que permitem a comunicação direta e o compartilhamento de recursos entre dispositivos conectados em uma rede, sem a necessidade de um servidor central. Em uma arquitetura P2P, cada nó (ou "peer") pode atuar tanto como cliente quanto como servidor, compartilhando e solicitando dados de outros nós.

Diante do problema apresentado, foi proposto a implementação de um sistema distribuído onde há vários bancos, hosts diferentes, e usuários que podem realizar movimentação em qualquer uma de suas contas. O usuário deve ser capaz de se cadastrar em vários bancos do sistema e, por meio de qualquer banco, acessar todas as suas contas. O sistema deveria ser desenvolvido utilizando o protocolo HTTP para realizar as comunicações, porém foi permitido utilizar frameworks para o desenvolvimento de API e de interfaces gráficas single-page. 

O sistema desenvolvido possui 2 elementos principais: backend, o sistema que expõe os serviços realizados pelos bancos; frontend, a aplicação gráfica por meio da qual o usuário usufruirá dos serviços bancários. 
O backend foi desenvolvido em Java17, Spring Boot, e o frontend em React 18. O relatório é dividido em 3 partes principais, excluindo a introdução e as etapas de como configurar o sistema, sendo elas: a visão geral do problema desenvolvido, as discussões detalhadas sobre a solução apresentada e as conclusões.


# 2. Visão geral
Em backend há os arquivos referentes ao serviço da API do banco. Em frontend há os arquivos referentes ao serviço da interface gráfica de um banco em questão. Fez-se o emprego do Docker, tanto na etapa de desenvolvimento quanto de produção, para isolar o sistema de eventuais problemas que podem ocorrer ao utilizar uma máquina compartilhada. O código do produto está comentado, exceto em nomes da variaveis e métodos auto-explicativos.

O sistema bancário desenvolido não utiliza a arquitetura peer-to-peer (2P2), onde um banco poderá ser tanto um cliente quanto um servidor em determinado momento. Por exemplo, quando um banco precisa perguntar aos demais se determinado cpf possui alguma conta registrada em cada um deles. Tal situação se repete quando um banco recebe uma requisição de transação e precisa comunicar os demais participantes.

Desenvolveu-se os sistemas de frontend e backend com tecnologias diferentes, tendo em vista a facilidade de se encontrar informações para possíveis problemas que surgissem, além de um pequeno conhecimento prévio do desenvolvedor nesta abordagem. O frontend possui 8 telas, sendo elas: tela de login, tela de cadastro de conta física, tela de cadastro de conta jurídica, tela de cadastro de conta conjunta, tela inicial, tela de pagamentos, tela de depósito e tela de transferência.

Para a utilização do sistema, o cliente deverá selecionar o banco que o mesmo deseja utilizar, acessando o endereço ao qual a interface do banco está exposta. O usuário, por meio de um navegador web, poderá acessar qualquer interface de qualquer banco por meio de requisições HTTP para a interface. Toda a comunicação entre o usuário, a aplicação frontend e a aplicação backend se dá por meio do protocolo HTTP. A **figura 1** ilustra o sistema desenvolvido. Em  resumo, o usuário acessa apenas a interface, visto que a mesma que encaminha suas solicitações para o banco.

<p align="center"><b>Figura  1</b> - Diagrama da arquitetura da Solução</p>
<p align="center">
  <img src="img\figura1.png" alt="arquitetura do sistema" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

Uma operação é uma tarefa com objetivo de diminuir o saldo (saque) de um conta ou aumentar (deposito). Uma transação é conceituada como sendo um conjunto de operações que deve ser efetuada de forma atômica. As transações podem ser do tipo de pagamento, depósito ou transferência.

Uma transação de depósito envolve a inserção de dinheiro no sistema. Dado que o usuário está autenticado no sistema, ou seja, já fez login, o mesmo poderá realizar depósito em qualquer outra conta presente no sistema bancário. Sendo assim, tal operação deve envolver apenas uma operação de depósito.

Uma transação de pagamento envolve a retirada de dinheiro do sistema. Dado que o usuário está autenticado no sistema, o mesmo poderá retirar parte de saldo de qualquer uma de suas contas, em qualquer banco, para realizar o pagamento. Não há nenhuma validação do pagamento, visto que o objetivo aqui é apenas o de retirar o dinheiro do sistema. Sendo assim, tal transação deve envolver apenas operações de saque.

Uma transação de transferência realiza a movimentação de saldo de N contas para uma única de destino. O usuário poderá retirar dinheiro de qualquer conta, desde que seja sua, e enviar para qualquer conta no sistema. O saldo retirado deverá ser igual ao depositado.

O sistema faz o uso de um protocolo de transações atômicas chamado Two Phase Commit. Desta forma, busca-se garantir que todos os nós do sistema distribuído verão o mesmo estado global.


# 3. Discussão sobre o produto
O sistema desenvolvido busca apresentar uma solução para o sistema de banco distribuidos, sem um banco central coordenador, para a tarefa de realizar transferências, pagamentos de conta e depósitos entre contas. Um mesmo usuário pode movimentar saldo das contas em diferentes bancos, isto é, após realizar o login em determinado banco. Todos os bancos expõe uma API Restful, desenvolvida com Java Spring Boot 3, pela qual toda a comunicação (seja com cliente ou outro banco) é feita. Em cada nó do sistema deverá haver o aplicativo da interface frontend, desenvolvida com o React, e o aplicativo do backend executando (veja **figura 1**). Sendo assim, cada computador do LARSID executará os dois processos para constituir um único nó. 

O serviço do banco possui uma thread para processamento exclusivo de transações. Sempre que chega uma requisição de transação no banco, a mesma será validada e depositada em fila para que a thread consuma posteriormente.

Dentro do sistema, para torná-lo confiável e robusto, fez-se o uso de mutex para cada conta criada, impedindo que duas solicitações pudessem gerar algum problema no saldo de determinada conta ao concorrerem em uma operação. Com o uso do 2PC, caso uma conta esteja sendo utilizada em uma transação, a mesma ficará impossibilitada de participar de outra transação até que a mesma termine. Desta forma, lida-se com cenários de concorrência local ou distribuído.

O sistema desenvolvido permite ao usuário, por meio de telas na interface gráfica, a criação de contas, sendo elas: do tipo física (**figura 5**), do tipo conta conjunta (**figura 6**) ou do tipo conta jurídica (**figura 7**). É possível alterar apenas o saldo das contas, conforme discutido em sessão, não sendo necessário alterar nome da pessoa e data de nascimento por exemplo. Um usuário do sistema, desde que possua contas em outros bancos do sistema global, poderá utilizar destas contas para realizar transferências e pagamentos, conforme **figura 8** e **figura 9**.


<p align="center"><b>Figura 5</b> - Tela cadastro conta física.</p>
<p align="center">
  <img src="img\cadastro fisica.png" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

<p align="center"><b>Figura 6</b> - Tela cadastro conta conjunta.</p>
<p align="center">
  <img src="img\cadastro conjunta.png" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

<p align="center"><b>Figura 7</b> - Tela cadastro conta jurídica.</p>
<p align="center">
  <img src="img\cadastro juridica.png" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

<p align="center"><b>Figura 8</b> - Tela transferências.</p>
<p align="center">
  <img src="img\transferencia.png" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

<p align="center"><b>Figura 9</b> - Tela de pagamentos.</p>
<p align="center">
  <img src="img\pagamentos.png" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

O usuário poderá realizar deposito em qualquer conta do sistema (**figura 10**). O usuário poderá realizar pagamentos de qualquer natureza, mediante o código do pagamento. Tanto o pagamento quanto a transferência podem retirar dinheiro de todas as contas de um determinado usuário. Ao entrar na tela de pagamento ou transferência aparecerá as contas que o usuário deseja retirar dinheiro para transferir (serão apenas as suas próprias contas). O destino do pagamento ou da transferência é único, ou seja, não pode haver mais de uma conta de destino para as transferências. Caso o usuário opte por realizar transferências para várias contas, deverá fazer uma transação de transferência para cada conta de destino.

<p align="center"><b>Figura 10</b> - Tela de depositos.</p>
<p align="center">
  <img src="img\teladeposito.png" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

Todas as chamadas a API de um banco ocorrem de forma síncrona, exceto a de transações. Quando uma solicitação de transação é recebida, o banco avalia a requisição, buscando incoerências, e caso esteja incorreta responderá uma mensagem de erro padrão (ver sessão de comunicação e protocolos). Caso a transação recebida seja aprovada, uma mensagem de aceitação é devolvida ao usuário, e a requisição é depositada em uma fila de transações cujo processamento se dará por uma outra thread. O processamento da transação em si, na thread, é feito de forma síncrona, ou seja, é feita uma requisição para cada participante e aguarda-se a resposta. 

#### 3.1 Transação Atômica e 2PC
Considerando os padrões para uma transação atômica, escolheu-se utilizar o Two Phase Commit (2PC) para aplicação no sistema. Outras alternativas foram consideradas, como SAGAS e o algoritmo de Ricart-Argwala. O SAGA não tem o foco em atomicidade de uma transação, isso é obtido por meio de uma atomicidade eventual, e por isso não foi escolhido. O algoritmo de Ricart-Argwala trata todo o ambiente como uma região crítica distribuída. O 2PC não apresenta todo sua operação em conformidade com o conceito de transações atômicas distribuídas, e por isso foi o escolhido.  

O Two Phase Commit é um protocolo conhecido de transações atômicas cujo principal objetivo é garantir que, ao fim de uma transação, todos os nós do sistema vejam o mesmo estado do todo. O 2PC apresenta 2 fases principais, mais uma etapa de erro. A primeira fase é chamada de *prepare*, nesta fase o banco que iniciou a transação (dito coordenador da transação) perguntará a todos os participantes da transação se eles conseguem realizar determinada operação. Assim que um participante receber uma solicitação de *prepare*, deverá realizar uma trava da conta envolvida na transação, desta forma nenhuma outra operação poderá usar a mesma até que a trava seja removida. Caso todos os participantes aceitem realizar a operação, será iniciada a segunda fase. 

A segunda fase é chamada de *commit*, e ao receber uma solicitação de *commit*, o banco em questão deve de fato realizar a operação e retirar a trava colocada na conta (conforme **figura 2**). Caso haja algum banco que sinalize que não poderá realizar a operação, na fase de Prepare, a fase de *abort* é iniciada. Na fase de *abort*, envia-se um comando para todos os bancos envolvidos para retirarem as travas colocadas nas contas, conforme **figura 3** e **figura 4**. Na implementação adotada, caso uma mensagem não seja entregue e recebida dentro de 5 segundos em uma fase de Prepare, será tratada como uma falha e o *abort* será iniciado.

Visando um maior controle sobre as operações realizadas, adotou-se um sistema de atribuição de identificadores globais. Uma transação possui um identificador de transação (tid) de duas partes, a primeira identifica o código do banco em que ela foi gerada e a segunda parte identifica o número da transação nesse banco (como exemplo, o tid 1.44 localiza a 44º transação do banco de id 1 ). Uma operação tem seu identificador de operação (oid) em 3 partes, as duas primeiras partes correspondem ao identificador da transação, a terceira parte diz respeito ao número da operação dentro da transação que ela faz parte (como exemplo, oid 1.44.2 localiza a 2º operação na 44º transação do banco de id 1).

<p align="center"><b>Figura 2</b> - Exemplo de transação bem sucedida.</p>
<p align="center">
  <img src="img\figura2.png" alt="arquitetura" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

<p align="center"><b>Figura 3</b> - Exemplo de falha no inicio da transação.</p>
<p align="center">
  <img src="img\figura3.png" alt="arquitetura" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

<p align="center"><b>Figura 4</b> - Exemplo de falha no fim da transação.</p>
<p align="center">
  <img src="img\figura4.png" alt="arquitetura" width='400px'>
</p>
<p align="center">Fonte: Autor</p>

#### 3.2 Confiabilidade e Resiliência
Para tentar tornar o sistem mais confiável e robusto, aplicou-se padrões de resiliência ao sistema, buscando sanar as principais falhas que podem ocorrer.

##### 3.2.1 Timeouts
Ao realizar uma requisição para outro banco, a resposta esperada deverá ser recebida em até 5 segundos, caso contrário o banco que fez a solicitação tratará o banco contatado como indisponível. Tal indisponibilidade de um participante de uma transação disparará um evento de *rollback* no banco que está coordenando a transação, ocasionando um abortar na transação.

##### 3.2.2 Novas tentativas (Retries)
Há uma thread especial que o único propósito é consumir uma fila de novas tentativas, para que assim possa executá-las. Caso a thread não consiga processar a nova tentativa com sucesso, a mesma é recolocada ao final da fila novamente. Não há novas tentativas caso algum nó falhe em receber a requisição na fase de prepare. Está sendo considerado que se uma requisição de *commit* ou *abort* for entregue, ela será processada.

Quando uma mensagem de aborte não pode ser entregue, seja por indisponibilidade de do banco participante ou algum erro no banco coordenador da transação, a requisição é colocada em uma fila de *retries* (novas tentativas).

Quando, em uma requisição do prepare do 2PC, a resposta é que a conta já está sendo usada em outra transação, toda a transação sofre um aborte e a mesma é colocada ao final da fila de processamento de transações novamente. Desta forma, não considero que a transação falhou por ter alguma conta ocupada e nem mantenho todas as outras contas, envolvidas nas demais operações, travadas por conta disso.

Considera-se que, quando um banco informa que ele poderá executar uma operação (fase de prepare), há uma garantia de que se receber a confirmação ele a executará. Logo, se o coordenador da transação não conseguir enviar a mensagem de confirmação, a mesma será colocada na fila de *retries*, como é feito com as mensagens de aborte.

##### 3.2.3 Operações idempotentes
Considerando que mensagens podem se perder e que erros podem acontecer no durante o processamento de mensagens, atribuiu-se características de idempotência a alguns endpoints chave. Os endpoints de *commit* e *abort* de foram pensados para não que sua duplicação não ocasione nenhum problema ao sistema.

Imagine que durante o processamento de uma requisição de *abort* vinda do banco A, o banco B perdeu a conexão com a rede. No sistema desenvolvido, o banco B teria realizado o *abort*, porém o banco A não saberia disso, visto que o mesmo recebeu um time out da resposta. Tal fato leva o banco A a enviar novamente a requisição. Por conta desse exemplo, desenvolveu-se os tratamentos para as requisições de *abort* e *commit* para serem idempotentes. 

# 4. Protocolos de Comunicações e Mensagens
Todo o sistema se comunica por meio de mensagens json, recebidas por meio de uma API Rest. Os campos utilizados em cada mensagem enviada foram selecionados para serem autoexplicativos.

#### 4.1 Tratador de exceção
O sistema apresenta um mecanismo que capta exceções geradas, e não tratadas em código, e retorna uma mensagem especial para quem tiver solicitado qualquer tipo de informação. Em resumo,
caso alguma exceção seja levantada ao processar uma requisição, uma resposta especial será lançada. A resposta segue o modelo abaixo, apresentando um campo 'message' com as informações do erro.
```
{
    "message": "A transação de pagamento precisa ter somente operações do tipo saque",
    "description": "uri=/user/openTransaction"
}
```

## 4.2 Entre Banco e Interface Gráfica
Cada aplicação gráfica (frontend) está associada a um serviço de backend (banco), ambos devem operar no mesmo host/computador. Com isso, as aplicações 
devem fazer requisições para os seus respectivos bancos e aguardar por respostas dos mesmos. Veremos como tal troca de mensagem foi implementada.

#### 4.2.1 Autenticação do usuário no banco
Endpoint: /auth  
Metodo: POST  
Para que um usuário consigo ter acesso a todas as suas contas e realizar transferências, o mesmo precisa fazer login no sistema. Para fazer login, basta informar o seu cpf
e qualquer uma das senhas que o mesmo tiver cadastrado em alguma conta no banco em questão. O banco espera receber um json no seguinte formato:
```
{
    "cpf": "80480",
    "password": "1234"
}
```
O banco irá retornar um json contendo as informações do usuário que serão necessárias para que o mesmo continue a usar  do sistema.
Como não foi feito o uso de tokens ou algo do tipo para autenticação segura, tais dados são salvos no localStorage na aplicação frontend. O código da
conta que é retornado, diz respeito a conta que possui a senha passada. Segue um exemplo de resposta do banco.

```
{
    "bankCode": "2",
    "accountId": "32432432",
    "cpf": "32432432"
}
```

#### 4.2.2 Criação de conta
Endpoint: /createAccount  
Metodo: POST  
Para que um usuário possa se cadastrar em um sistema, o mesmo deverá enviar um json contendo as informações necessárias para cadastro. Segue abaixo exemplos de json para 
criar contas do tipo fisica, juridica e conjunta.
***Conta Física***
```
{
    "cpf1": "32432432",
    "password": "123",
    "accountType": "FISICA"
}
```
***Conta Jurídica***
```
{
    "cnpj": "24247741",
    "password": "123",
    "accountType": "JURIDICA"
}
```
***Conta Conjunta***
```
{
    "cpf1": "32432432",
    "cpf2": "756565",
    "password": "123",
    "accountType": "CONJUNTA"
}
```
O banco irá retornar um json contendo informações da conta e uma mensagem, caso seja possível de ser criada. Segue exemplo abaixo.

```
{
    "accountId": "32432432",
    "bankCode": "2",
    "message": "Conta fisica criada com sucesso"
}
```

#### 4.2.3 Obtenção das contas acessadas pelo CPF globalmente
Endpoint: /accounts/{cpf} 
Metodo: GET  
Para que um usuário consiga realizar transferências, o mesmo precisa saber quais contas pode acessar. Sendo assim, é feita uma solicitação no endpoint, passando o seu próprio
cpf como parâmetro.

O banco irá retornar um json contendo as contas que determinado usuário poderá utilizar (realizar saques). O usuário verá um status completo das suas contas (banco em que ela foi registrada, número da conta e saldo).
Segue abaixo um exemplo de resposta.
```
{
    "accountsToUse": [
        {
            "accountCode": "32432432",
            "bankCode": "2",
            "value": 900.0
        },
        {
            "accountCode": "324324320800",
            "bankCode": "2",
            "value": 2.0
        },
        {
            "accountCode": "32432432",
            "bankCode": "3",
            "value": 2.0
        }
    ],
    "cpf": "32432432"
}
```

#### 4.2.4 Iniciar transação
Endpoint: /openTransaction  
Metodo: POST  
Para um realizar uma transação (seja do tipo transferência, deposito ou pagamento ), será preciso enviar informações de todas as contas que estão envolvidas na transação,
e os valores movimentados em cada operação. Para que o sistema interprete as transações como transferência, deposito ou pagamento é só colocar o campo 'transactionType' como 
TRANSFER, DEPOSIT ou PAYMENT respectivamente. Segue um exemplo abaixo para a transação do tipo transferência.
```
{
    "transactionType": "TRANSFER",
    "source": {
        "bankCode": "1",
        "cpf": "181815"
    },
    "operations": [
        {
            "operationType": "WITHDRAW",
            "bankCode": "1",
            "accountCode": "3625623",
            "value": 100
        },
        {
            "operationType": "WITHDRAW",
            "bankCode": "2",
            "accountCode": "1277",
            "value": 100
        },
        {
            "operationType": "DEPOSIT",
            "bankCode": "2",
            "accountCode": "7777",
            "value": 400
        }
    ]
}
```
O banco irá retornar uma resposta contendo as informações da transação, caso tenha sido possível criar a mesma, como o tid e o status da mesma. Segue exemplo abaixo.
```
{
    "tid": "2.2",
    "transactionStatus": "WAITING_COMMIT"
}
```

#### 4.2.5 Verificar o status de uma transação
Endpoint: /transaction/{tid}
Metodo: GET  
Embora não seja um requisito do sistema, e nem esteja sendo sendo consumido pela aplicação frontend, este recurso foi implementado para que fosse possível realizar testes e auxiliar no processo de debug.
O tid da transação é passada como parâmetro da requisição.
O banco irá retornar um json contendo todas as informações da transação (basta ver a classe Transaction no pacote model). 
```
{
    "tid": "2.2",
    "transactionType": "DEPOSIT",
    "source": {
        "bankCode": "2",
        "cpf": "32432432"
    },
    "transactionStatus": "CONCLUDED",
    "createAt": "2024-06-24T17:48:27.7602564",
    "operations": [
        {
            "oid": "2.2.0",
            "operationType": "DEPOSIT",
            "bankCode": "2",
            "accountCode": "32432432",
            "value": 900.0
        }
    ]
}
```

## 4.3 Entre Bancos (Banco a Banco)
Cada está em um host/computador diferente, logo precisa-se de um acordo para as comunicações entre os mesmos. 
Veremos como tal troca de mensagem foi implementada.

#### 4.3.1 Obtenção das contas acessadas pelo CPF locamente
Endpoint: /accountsIn/{id}
Metodo: GET  
Cada banco tem seu conjunto de contas, um determinado usuário pode criar um conta individual em cada banco e várias conjuntas. 
Para que um determinado usuário connsiga ter acesso a todas as contas que o mesmo pode acessar, ele precisa pedir a um banco tal informação e como
o usuário pode ter contas em vários bancos, o banco acessado deverá enviar requisições para os outros bancos pedindo as contas que o usuário tem acesso .
O CPF do usuário, ou CNPJ, é enviado por meio do parâmetro da solicitação GET. 

O banco irá retornar um json contendo as informações das contas que o usuário poderá acessar globalmente (em todos os bancos). Segue um exemplo de resposta do banco.
```
{
    "accountsToUse": [
        {
            "accountCode": "32432432",
            "bankCode": "2",
            "value": 900.0
        },
        {
            "accountCode": "324324320800",
            "bankCode": "2",
            "value": 2.0
        },
        {
            "accountCode": "32432432",
            "bankCode": "3",
            "value": 2.0
        }
    ],
    "cpf": "32432432"
}
```

#### 4.3.2 Verificando a possibilidade de commit
Endpoint: /prepare/{accountId}
Metodo: POST  
Seguindo a lógica do 2PC, é necessário que um banco pergunte a outro se o mesmo poderá efetuar a operação. Para isso, é informado o número da conta
envolvida na transação por meio do parâmetro GET {accountId} e envia-se a operação que o mesmo deverá verificar a disponibilidade.

```
{
    "sourceBankCode" = "2",
    "operation" = {
                    "oid" = "2.2.1",
                    "operationType" = "DEPOSIT",
                    "bankCode" = "2",
                    "accountCode" = "2564",
                    "value" = 100.0
                   }
}
```
O banco irá retornar uma mensagem informando se consegue ou não realizar a operação. o responseNode pode informar se o banco poderá fazer o commit,
se não poderá ou se a conta já está sendo usada, sendo os valores associados YES_CAN_COMMIT, NOT_CAN_COMMIT, ACCOUNT_IN_USE respectivamente.
```
{
    "oid" = "2.2.1",
    "accountCode" = "2564",
    "responseNode" = "YES_CAN_COMMIT" 
}
```

#### 4.3.3 Executando o commit
Endpoint: /commit/{accountId}
Metodo: POST  
Seguindo a lógica do 2PC, é necessário que um banco confirme se a operação deverá ser feita. Para isso, é informado o número da conta
envolvida na transação por meio do parâmetro GET {accountId} e envia-se a operação que o mesmo deverá realizar o commit (deve ser a mesma usada no passo de prepare).

```
{
    "sourceBankCode" = "2",
    "operation" = {
                    "oid" = "2.2.1",
                    "operationType" = "DEPOSIT",
                    "bankCode" = "2",
                    "accountCode" = "2564",
                    "value" = 100.0
                   }
}
```
O banco irá retornar uma mensagem informando se realizou ou não a operação. O responseNode apresenta mensagens referentes a operação de commit, 
podendo assumir WITHOUT_AUTHORIZATION_TO_COMMIT, COMMITTED, ACCOUNT_NOT_IN_LOCK que são, respectivamente, 
quando envio uma operação diferente da usada na fase anterior do 2PC, quando consigo fazer o commit, quando a conta ainda não passou pela fase anterior.
Segue um exemplo de resposta.
```
{
    "oid" = "2.2.1",
    "accountCode" = "2564",
    "responseNode" = "COMMITTED" 
}
```

#### 4.3.4 Abortando uma transação
Endpoint: /abort/{accountId}
Metodo: POST  
Seguindo a lógica do 2PC, é necessário que um banco informe ao outro quando uma operação de prepare deve ser desfeita (deve retirar a trava da conta). Para isso, é informado o número da conta
envolvida na transação por meio do parâmetro GET {accountId} e envia-se a operação utilizada para fazer o prepare.
```
{
    "sourceBankCode" = "2",
    "operation" = {
                    "oid" = "2.2.1",
                    "operationType" = "DEPOSIT",
                    "bankCode" = "2",
                    "accountCode" = "2564",
                    "value" = 100.0
                   }
}
```
O banco irá retornar uma mensagem informando se realizou ou não o abort, porém o recebimento desta confirmação não tem muita importância para quem mandou. O responseNode apresenta mensagens referentes a operação de commit,
podendo assumir WITHOUT_AUTHORIZATION_TO_COMMIT ou ABORTTED que são, respectivamente,
quando envio uma operação diferente da usada na fase anterior do 2PC ou quando consegui retirar a trava.
Segue um exemplo de resposta.
```
{
    "oid" = "2.2.1",
    "accountCode" = "2564",
    "responseNode" = "ABORTTED" 
}
```


# 5. Conclusão
O produto desenvolvido atende aos principais requisitos apresentados na situação-problema. Durante o processo de desenvolvimento, pode-se aprender sobre técnicas de redes e aplicadas em sistemas distribuídos (como a conectividade), e também características de sistemas com threads e peer-to-peer (como a concorrência local e distribuída).

Embora o sistema implementado atenda de forma eficaz o problema proposto, o mesmo pode ser melhorado em aspectos de algoritmos e na utilização de mais threads para o processamento de requisições de transações. Outro ponto a ser melhorado é a nomeação dos dispositivos e a descoberta de serviços, na solução apresentada é necessário especificar o endereço de cada serviço. Outro ponto a ser melhorado, seria a adoção de protocolos de segurança e criptografia. Caso se utilize de um projeto totalmente assíncrono para as transações, a confiabilidade do sistema poderá ser aumentada, visto que o intervalo de tempo da falha crítica poderá ser menor.

Diante do exposto, este projeto serviu para aprofundar os conhecimentos relativos a atomicidade e comunicação em sistemas distribuídos, como também serviu para aprimorar habilidades necessárias para uma formação profissional.