package transacoes_distribuidas.services;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import transacoes_distribuidas.exceptions.InvalidOperation;
import transacoes_distribuidas.exceptions.NullFields;
import transacoes_distribuidas.infra.Consortium;
import transacoes_distribuidas.dto.in.*;
import transacoes_distribuidas.dto.out.*;

import transacoes_distribuidas.exceptions.ResourceNotFoundException;
import transacoes_distribuidas.model.*;
import transacoes_distribuidas.presenters.PresenterOperation;
import transacoes_distribuidas.presenters.PresenterTransaction;

import java.util.*;
import java.util.concurrent.BlockingQueue;

@Component
public class BankService {
    @Autowired
    private Bank bank;
    @Autowired
    private Map<String, Consortium> consortium;
    @Autowired
    private Map<String, String> consortium2;
    @Autowired
    private HttpService httpService;

    @Autowired
    private BlockingQueue<Transaction> blockingQueue;

    @Autowired
    private TwoPhaseCommitCoordinator twoPhaseCommitCoordinator;

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);



    /** Método usado para realizar o login. Para realizar o login precisa apenas passar o cpf e uma senha.
     * Se eu encontrar, em qualquer uma das contas no banco, uma conta que tenha o  cpf do individuo e a senha
     * informada, será considerado autenticado.
     *
     * @param req{"id": str, "password": str} -> id é o cpf do usuário
     * @return AuthResponse{"bankCode": str, "accountId": str, "cpf": str}
     */
    public AuthResponse getAccountToLogin(AuthRequest req){
        logger.info(String.format("LOGIN - Iniciando a busca pelo usuario{%s}", req.cpf));
        AuthResponse res = null;
        // Obtem todas as contas
        List<Account> accounts = this.bank.findAllAccountsByCpf(req.cpf);
        // Se tive alguma resposta
        if (accounts != null){
            // Saio percorrendo todas as contas para ver se encontro a combinação de cpf e senha em alguma
            for (Account account: accounts) {
                if(account.getPassword().equals(req.password)){
                    res = new AuthResponse(account.getBankCode(), account.getAccountId(), req.cpf);
                    logger.info(String.format("LOGIN - O usuario{%s} foi autenticado e tem permissão de logar", req.cpf));
                    return res;
                }
            }
        }
        logger.info(String.format("LOGIN - O usuario{%s} não foi autenticado", req.cpf));
        throw new ResourceNotFoundException("Senha ou cpf estão errados");
    }

    /** Usado para criar as contas dos tipos: Fisica, Juridica e Conjunta
     *
     * @param req -> Objeto para criar uma conta genérica.
     * @return Informações com o código da conta, código do banco e uma mensagem.
     */
    public CreateAccountResponse createAccount(CreateAccountRequest req){
        Account aux;
        CreateAccountResponse res = null;
        logger.info(String.format("CREATE ACC - Iniciando o processo de criar conta %s", req.accountType));
        // Verifico o tipo de conta a ser criada
        switch (req.accountType) {
            case FISICA:
                // Se tiver campos vazios
                if(req.cpf1 == null || req.password == null || req.cpf1.trim().isEmpty() || req.password.trim().isEmpty()){
                    throw new NullFields("O CPF ou a senha estão vazios");
                }
                aux = this.bank.createAccountFisica(req.password, req.cpf1);
                if (aux != null){
                    res = new CreateAccountResponse(Bank.getBankCode(), aux.getAccountId(), "Conta fisica criada com sucesso");
                }
                break;
            case JURIDICA:
                // Se tiver campos vazios
                if(req.cnpj == null || req.password == null || req.cnpj.trim().isEmpty() || req.password.trim().isEmpty()){
                    throw new NullFields("O CPF ou a senha estão vazios");
                }
                aux = this.bank.createAccountJuridica(req.password, req.cnpj);
                if (aux != null){
                    res = new CreateAccountResponse(Bank.getBankCode(), aux.getAccountId(), "Conta Juridica criada com sucesso");
                }
                break;
            case CONJUNTA:
                // Se tiver campos vazios
                if(req.cpf1 == null || req.cpf1.trim().isEmpty() || req.cpf2 == null || req.cpf2.trim().isEmpty() || req.password == null  || req.password.trim().isEmpty()){
                    throw new NullFields("Os CPFs ou a senha estão vazios");
                }
                aux = this.bank.createAccountConjunta(req.password, req.cpf1, req.cpf2);
                if (aux != null){
                    // uma observação é que eu retorno o cpf que foi passado para ser criado
                    res = new CreateAccountResponse(Bank.getBankCode(), aux.getAccountId(), "Conta Conjunta criada com sucesso");
                }
                break;
            default:// Não faz nada
                throw new InvalidOperation("Não existe conta do tipo desejado");
        }
        logger.info(String.format("CREATE ACC - Conta %s criada com numero de conta %s", req.accountType, res.accountId));
        return res;
    }


    /** Obtem todas as contas de todos os bancos que a pessoa com o cpf tem acesso no consorcio.
     *
     * @param id -> cpf da pessoa
     * @return
     */
    public AccountsResponse getAccounts(String id){
        logger.info(String.format("ACCOUNTS - Obtendo as contas que o usuario{%s} pode acessar", id));
        //
        AccountsResponse resp = new AccountsResponse();
        resp.accountsToUse = new ArrayList<>();
        resp.cpf = id;
        //
        String uri;
        AccountsResponse aux;
        // vou sair pedindo a todos os bancos do consorcio
        Consortium[] allConsortiums = Consortium.getAllConsortiums();
        for (Consortium consortium: allConsortiums) {
            logger.info(String.format("ACCOUNTS - Solicitando as contas que usuario{%s} pode acessar para banco{%s}", id, consortium.getBankCode()));
            try{
                // Se eu estiver analisando o elemento que seja eu mesmo
                if (consortium.getBankCode().equals(this.bank.getBankCode())){
                    // Chamo o método que busca interno
                    aux = getAccountsIn(id);
                    // Pego tudo que achei e jogo na resposta
                    resp.accountsToUse.addAll(aux.accountsToUse);
                }
                // Se estiver analisando outro banco
                else{
                    // Monto a uri
                    uri  = consortium.getBankUrl() + "bank/accountsIn/" + id;
                    // Faço a requisição e trato
                    aux = this.httpService.getAccountsResponseData(uri);
                    // Pego tudo que achei e jogo na resposta
                    resp.accountsToUse.addAll(aux.accountsToUse);
                    logger.info(String.format("ACCOUNTS - Foram encontrados %d contas que usuario{%s} pode usar", aux.accountsToUse.size(), id));
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                //System.out.println(e.toString());
                //e.printStackTrace();
                // Se deu algum erro, não faz nada
            }
        }
        if (resp.accountsToUse.size() == 0){
            throw new ResourceNotFoundException("Não foi encontrado nenhuma conta para o cpf especificado");
        }
        return resp;
    }

    /** Obtem todas as contas no banco local que a pessoa com o cpf tem acesso.
     *
     * @param cpf -> cpf da pessoa
     * @return
     */
    public AccountsResponse getAccountsIn(String cpf){
        logger.info(String.format("ACCOUNTSIN - Obtendo as contas que o usuario{%s} pode acessar no proprio banco", cpf));
        // Obtenho as contas que o cpf tem acesso no banco
        List<Account> accounts = this.bank.findAllAccountsByCpf(cpf);
        logger.info(String.format("ACCOUNTSIN - Achei a conta{%s} no proprio banco", cpf));

        AccountsResponse res = new AccountsResponse();
        res.cpf = cpf;
        res.accountsToUse = new ArrayList<>();

        // Monta o formato de resposta
        for (Account acc: accounts) {
            res.accountsToUse.add(new BalanceResponse(acc.getBankCode(), acc.getAccountId(), acc.getBalance()));
        }

        logger.info(String.format("ACCOUNTSIN - Foram encontrados %d contas que usuario{%s} pode usar", res.accountsToUse.size(), cpf));
        return res;
    }



    /** Adiciona uma transação na fila para processamento posterior.
     * É feita uma validação da requisição com base no tipo da mesma.
     *
     * @param req -> Objeto para criar uma transação.
     * @return Um esquema com tid da transação com o status.
     * @throws InterruptedException -> Exceção levantada ao tentar inserir uma transação na fila e não conseguir.
     */
    public CreateTransactionResponse openTransaction(CreateTransactionRequest req) throws InterruptedException {
        Transaction transaction = null;
        CreateTransactionResponse res = new CreateTransactionResponse();
        // Analiso qual o tipo de transferência devo fazer
        switch (req.transactionType){
            case TRANSFER:
                // Se a operação não for valida
                if (! isValidTransfer(req)){
                    throw new InvalidOperation("O valor total das operações de saque é diferente do valor das operações deposito. Não pode haver 2 operações com a mesma conta");
                }
                break;
            case DEPOSIT:
                // Se a operação não for valida
                if (! isValidDeposit(req)){
                    throw new InvalidOperation("A transação de deposito precisa ter somente uma operação, sendo ela do tipo deposito. Não pode haver 2 operações com a mesma conta");
                }
                break;
            case PAYMENT:
                // Se a operação não for valida
                if (! isValidPayment(req)){
                    throw new InvalidOperation("A transação de pagamento precisa ter somente operações do tipo saque. Não pode haver 2 operações com a mesma conta");
                }

                break;
        }
        // OQ FAZER QUANDO transaction FOR NULL??
        // Cria a transação
        transaction = PresenterTransaction.dtoToModel(req);
        logger.info(String.format("SERVICE TRANSACTION REQUEST - A transação{%s} foi validada com sucesso", transaction.getTid()));
        // Monta a resposta
        res.tid = transaction.getTid();
        res.transactionStatus = transaction.getTransactionStatus();
        // Coloca a transação na fila que será processada
        this.blockingQueue.put(transaction);
        logger.info(String.format("SERVICE TRANSACTION REQUEST - A transação{%s} foi adicionada na fila para processamento", transaction.getTid()));

        return res;
    }


    /** Método usado para verificar uma transação, com base no tid da mesma.
     * ! Aplicado para fins de debug e testes !
     *
     * @param tid -> id global da transação
     * @return A transação especificada
     */
    public Transaction getTransactionByTid(String tid){
        Transaction transaction = this.bank.findTransactionInHistory(tid);
        return transaction;
    }


    // ============================================================================================================= //
    // ================================= BLOCO DE FUNÇÕES EXCLUSIVAS DO 2PC ====================================== //
    // ============================================================================================================= //

    /** Chama o método que implementa a lógica do estado de PREPARE do 2PC
     *
     * @param operation
     * @return
     */
    public _2PCResponse canCommit(OperationRequest operation){
        logger.info(String.format("SERVICE 2PC - Pedido de PREPARE recebido para operação{%s}", operation.oid));
        return this.twoPhaseCommitCoordinator.requestCanCommit(PresenterOperation.dtoToModel(operation));
    }

    /** Chama o método que implementa a lógica do estado de COMMIT do 2PC
     *
     * @param operation
     * @return
     */
    public _2PCResponse doCommit(OperationRequest operation){
        logger.info(String.format("SERVICE 2PC - Pedido de COMMIT recebido para operação{%s}", operation.oid));
        return this.twoPhaseCommitCoordinator.requestDoCommit(PresenterOperation.dtoToModel(operation));
    }

    /** Chama o método que implementa a lógica do estado de ABORT do 2PC
     *
     * @param operation
     * @return
     */
    public _2PCResponse abort(OperationRequest operation){
        logger.info(String.format("SERVICE 2PC - Pedido de ABORT recebido para operação{%s}", operation.oid));
        return this.twoPhaseCommitCoordinator.requestDoAbort(PresenterOperation.dtoToModel(operation));
    }


    // ============================================================================================================= //
    // ================================= BLOCO DE FUNÇÕES PRIVADAS AUXILIARES ====================================== //
    // ============================================================================================================= //
    /** Verifico se a requisição do tipo transferência é valida.
     * NENHUMA CONTA (BANCO_CODE E CONTA_CODE podem aparecer mais de uma vez)
     * Precisa ter operações de depósito e de saque. O valor total de saque tem que ser igual ao de deposito.
     *
     * @param req
     * @return true se for valida e false se não for
     */
    private boolean isValidTransfer(CreateTransactionRequest req){
        Set<String> seenOperations = new HashSet<>();// para ver se tem operação duplicada
        float balanceTotal=0;// Tem que resultar 0, pois significa que o saldo retirado é igual ao saldo depositado
        /** Verifico se o saldo retirado é igual ao depositado */
        for (OperationRequest operation: req.operations) {
            // teste de conta duplicada
            String combinedKey = operation.accountCode + operation.bankCode;
            if (!seenOperations.add(combinedKey)) {
                return false; // Duplicata encontrada
            }

            if (operation.operationType == OperationType.DEPOSIT) {
                balanceTotal = balanceTotal + operation.value;
            } else if (operation.operationType == OperationType.WITHDRAW) {
                balanceTotal = balanceTotal - operation.value;
            }
        }
        return (balanceTotal == 0);
    }

    /** Verifico se a requisição do tipo deposito é valida.
     * Só pode haver uma operação e tem que ser do tipo DEPOSIT.
     *
     * @param req
     * @return true se for valida e false se não for
     */
    private boolean isValidDeposit(CreateTransactionRequest req){
        Set<String> seenOperations = new HashSet<>();// para ver se tem operação duplicada
        // Só pode haver deposito em uma conta especifica
        if (req.operations.size() != 1){
            return false;
        }
        // Só pode ter operação do tipo deposito
        for (OperationRequest operation: req.operations) {
            // verificando se tem operação duplicada
            String combinedKey = operation.accountCode + operation.bankCode;
            if (!seenOperations.add(combinedKey)) {
                return false; // Duplicata encontrada
            }

            if (operation.operationType != OperationType.DEPOSIT) {
                return false;
            }
        }
        return true;
    }

    /** Verifico se a requisição do tipo pagamento é valida.
     * Só deve ter operações WITHDRAW, com apenas uma DEPOSIT. Sendo a DEPOSIT pro banco com BankCode = 0
     *
     * @param req
     * @return true se for valida e false se não for
     */
    private boolean isValidPayment(CreateTransactionRequest req){
        Set<String> seenOperations = new HashSet<>();// para ver se tem operação duplicada
        boolean aux = false;
        int i = -1;
        int j = -1;

        // Só pode ter operação do tipo saque
        for (OperationRequest operation: req.operations) {
            i =i+1;
            // Verifica se tem conta duplicada
            String combinedKey = operation.accountCode + operation.bankCode;
            if (!seenOperations.add(combinedKey)) {
                return false; // Duplicata encontrada
            }

            // Se achei uma operação de deposito, mas é a primeira que encontro
            if (operation.operationType == OperationType.DEPOSIT && aux == false){
                // Verifico se os campos não são 0 e 0
                if (operation.bankCode.equals("0")){
                    req.operations.remove(i);
                    aux = true;
                    return true;
                }
                else{
                    return false;
                }
            }
            // Se achei uma operação de deposito,mas já havia achado outra antes
            if (operation.operationType == OperationType.DEPOSIT && aux == true) {
                //req.operations.remove(i); Nem precisa se dar ao trabalho de remover, já está errado
                return false;
            }
        }
        // Removendo a conta que tem o deposito no banco 0

        return true;
    }

}
