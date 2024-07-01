package transacoes_distribuidas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import transacoes_distribuidas.dto.out.ResponseNode;
import transacoes_distribuidas.dto.out._2PCResponse;
import transacoes_distribuidas.exceptions.AccountInUse;
import transacoes_distribuidas.exceptions.InvalidOperation;
import transacoes_distribuidas.exceptions.ResourceNotFoundException;
import transacoes_distribuidas.infra.Consortium;
import transacoes_distribuidas.model.*;
import transacoes_distribuidas.presenters.PresenterOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Esta classe representa o coordenador de uma transação com o 2PC, porém também faz o papel de intermediário da uma transação
 * impondo a lógica do 2PC dentro do sistema. Em resumo, não faz apenas iniciar a transação, mas garantir que o sistema utilize o 2PC.
 *
 * Uma observação, os métodos de canCommit, doCommit e abort foram implementados de forma a serem usados ao se receber uma solicitação do controller
 * ou para o caso de iniciar uma transação.
 *
 */
@Component
public class TwoPhaseCommitCoordinator {
    @Autowired
    private Map<String, Consortium> consortium;
    @Autowired
    private HttpService httpService;
    @Autowired
    private Bank bank;

    private HashMap<String, String> preparedLocal = new HashMap<>();// Contas que estão em fase de prepare localmente
    private final ReentrantLock lockPrepared = new ReentrantLock();// É um Mutex para a lista de contas
    private static final Logger logger = LoggerFactory.getLogger(TwoPhaseCommitCoordinator.class);


    /** Método responsável por iniciar a transação atômica. Nesta implementação, a transação distribuída será realizada de forma síncrona.
     *
     * @param transaction
     */
    public void openTransaction(Transaction transaction){
        logger.info(String.format("2PC OPEN - Iniciando a transação{%s}", transaction.getTid()));
        //boolean isAvaliableToCommit = true; // Variavel auxiliar que vai me auxiliar a entender se o nó votou SIM para a operação
        boolean accountWaiting = false; // Para informar se estou esperando uma conta em espera
        Operation operationInTime = null; // É a operação que estou processando
        int i=0, j;
        _2PCResponse aux;
        State2PC nextState2PC = State2PC.PREPARE;


        /**  ======================== FASE DE PREPARE ======================== */
        logger.info(String.format("2PC OPEN - Entrando na fase de PREPARE transação{%s}", transaction.getTid()));
        for (i = 0; i < transaction.getOperations().size(); i++) {
            // Obtem a operação que tem a vez
            operationInTime = transaction.getOperations().get(i);
            // Verifica se o nó vai poder realizar ela
            aux = requestCanCommit(operationInTime); /** 2PC */
            if (aux.responseNode == ResponseNode.ACCOUNT_IN_USE){
                accountWaiting = true; // por estar esperando
                nextState2PC = State2PC.ABORT;
                break;
            }
            // Se deu um erro em algum aqui, não envia o pedido para os demais e já começa a abortar os que já receberam
            if (aux.responseNode != ResponseNode.YES_CAN_COMMIT){
                nextState2PC = State2PC.ABORT;
                break;
            }
            logger.info(String.format("2PC OPEN - Realizei o PREPARE de operação{%s} com sucesso!", operationInTime.getOid() ));
            nextState2PC = State2PC.COMMIT;
        }


        /** ======================== FASE DE ABORT ======================== */
        if (nextState2PC == State2PC.ABORT){
            logger.info(String.format("2PC OPEN - Entrando na fase de ABORT transação{%s}", transaction.getTid()));
            // Começo da anterior a dar falha pq, se tiver dado falha, o banco deverá liberar o lock antes de reportar a falha
            for (j = i-1; j >= 0; j--) {
                // Obtem a operação que tem a vez
                operationInTime = transaction.getOperations().get(j);
                logger.info(String.format("2PC OPEN - Realizando o ABORT de operação{%s}", operationInTime.getOid()));
                boolean inRetries = false;
                // Se eu não conseguir fazer o abort, devo colocar uma fila de retries
                try {
                    requestDoAbort(operationInTime); /** 2PC */
                } catch (Exception e){
                    // Fico tentando colocar o retries na fila ????????????????????????????????????????????????????????????????????????
                    while (inRetries == false){
                        try{
                            /** COLOCAR NO RETRIES AGR **/
                            inRetries = true;
                        } catch (Exception ex){

                        }
                    }
                }
            }
            logger.info(String.format("2PC OPEN - Operação de ABORT transação{%s} com sucesso", transaction.getTid()));
            // SE TIVE DE FAZER UM ABORT POR CONTA DE UMA ESPERA
            if(accountWaiting == true){
                // Adiciona na lista de historico como falha
                transaction.setTransactionStatus(TransactionStatus.WAITING_COMMIT);
                this.bank.addHistoryTransactions(transaction);
                logger.info(String.format("2PC OPEN - Colocando a transação{%s} de volta na fila", transaction.getTid()));
                throw new AccountInUse("");
            }
            // SE NÃO ESTIVER EM UMA ESPERA, SIGNIFICA QUE FOI UM ERRO MESMO
            else{
                // Adiciona na lista de historico como falha
                transaction.setTransactionStatus(TransactionStatus.FAILED);
                this.bank.addHistoryTransactions(transaction);
                logger.info(String.format("2PC OPEN - A transação{%s} foi falhou", transaction.getTid()));
                //return; // Para encerrar a execução e não fazer a parte do commit
            }
        }


        /** ======================== FASE DE COMMIT ======================== */
        if (nextState2PC == State2PC.COMMIT){
            logger.info(String.format("2PC OPEN - Entrando na fase de COMMIT transação{%s}", transaction.getTid()));
            for (i = 0; i < transaction.getOperations().size(); i++) {
                // Obtem a operação
                operationInTime = transaction.getOperations().get(i);
                logger.info(String.format("2PC OPEN - Realizando o COMMIT de operação{%s}", operationInTime.getOid()));
                // VERIFICO SE CONSEGUI DAR COMMIT??????????????????????????????????????????????????????????????????????????????????
                requestDoCommit(operationInTime); /** 2PC */

            }
            // Adiciona na lista de historico como concluida
            transaction.setTransactionStatus(TransactionStatus.CONCLUDED);
            this.bank.addHistoryTransactions(transaction);
            logger.info(String.format("2PC OPEN - A transação{%s} foi conluída com sucesso", transaction.getTid()));
        }


    }


    /** Reliza a fase de PREPARE únicamente, ou seja, manda o canCommit(O) para o banco e conta especifico.
     *
     * @param operation
     * @return Em caso afirmativo deve retornar um responseNode = ResponseNode.YES_CAN_COMMIT
     */
    public _2PCResponse requestCanCommit(Operation operation){
        String uri;
        _2PCResponse res = new _2PCResponse();
        boolean aux = false;
        res.oid = operation.getOid();

        /** Se a operação for no banco atual/local, eu faço as chamadas */
        if(operation.getBankCode().equals(Bank.getBankCode())){
            lockPrepared.lock();// Trava tudo para só um processo fazer isso por vez
            // 1. Verifica se a conta já está sendo usada (se já tem a trava nela)
            if (this.preparedLocal.get(operation.getAccountCode()) != null){
                logger.info(String.format("2PC PREPARE - A conta está esperando por outra operação"));
                //throw new AccountInUse("Não será possível concluir a transação, pois a conta já está sendo utilizada para uma transação");
                res.responseNode = ResponseNode.ACCOUNT_IN_USE;
            }
            // 2. Se a conta não está sendo usada, faz a solicitação de prepare
            else {
                try{
                    aux = this.bank.canDoIt(operation, operation.getAccountCode());
                    logger.info(String.format("2PC PREPARE - permissão da operação{%s} para ser realizada é %b", operation.getOid(), aux));
                } catch (ResourceNotFoundException r){
                    logger.info(String.format("2PC PREPARE - Conta não encontrada para operação{%s}", operation.getOid()));
                    aux = false;
                }
                // Se for possível fazer a operação
                if (aux == true){
                    res.responseNode = ResponseNode.YES_CAN_COMMIT;
                    // Faz a trava da conta
                    this.preparedLocal.put(operation.getAccountCode(), operation.getOid());
                }
                // Se não for possível fazer a operação
                else {
                    res.responseNode = ResponseNode.NOT_CAN_COMMIT;
                }
            }
            lockPrepared.unlock();// Retiro a trava
        }
        /** Se a operação for para um banco externo, eu faço as chamadas */
        else{
            try{
                logger.info(String.format("2PC PREPARE - Encaminhando operação{%s} para Banco{%s}", operation.getOid(), operation.getBankCode()));
                uri = consortium.get(operation.getBankCode()).getBankUrl() + "bank/prepare"; // Pega o uri do banco
                logger.info(String.format("2PC PREPARE - Realizando PREPARE externo para operação{%s} - uri{%s}", operation.getOid(), uri));
                res =  this.httpService.post2PC( uri, PresenterOperation.modelToDto(operation));
                logger.info(String.format("2PC PREPARE - Recebi para operação{%s} do Banco{%s} -> %s", operation.getOid(), operation.getBankCode(), res.responseNode.name()));
            } catch (Exception e){ // Se não consegui contatar o outro banco
                res.responseNode = ResponseNode.NOT_CAN_COMMIT;
            }
        }
        return res;
    }

    /**
     * Se a operação que está com a tentativa do COMMIT for a mesma que fez um prepare anteriormente, realizo a operação e informo
     * que a conta está liberada e a operação já foi executada.
     *
     * @param operation
     * @return Em caso afirmativo deve retornar um responseNode = ResponseNode.COMMITTED
     */
    public _2PCResponse requestDoCommit(Operation operation){
        _2PCResponse res = new _2PCResponse();
        res.oid = operation.getOid();
        String uri;

        // Se for no banco atual/local
        if(operation.getBankCode().equals(Bank.getBankCode())){
            logger.info(String.format("2PC COMMIT - Realizando COMMIT local para operação{%s}", operation.getOid()));
            // Verifico se a conta não está na fase de PREPARE
            if (this.preparedLocal.get(operation.getAccountCode()) == null){
                res.responseNode = ResponseNode.ACCOUNT_NOT_IN_LOCK;
            }
            // Verifico se a operação que quer dar commit  foi a que fez o prepare
            else if ( this.preparedLocal.get(operation.getAccountCode()).equals(operation.getOid()) ){
                // FAZ A OPERAÇÃO///////////////////////////////////////////////////////////////////////////////////////
                this.bank.makeOperation(operation, operation.getAccountCode());
                // Retira a trava da conta
                this.preparedLocal.remove(operation.getAccountCode());
                res.responseNode = ResponseNode.COMMITTED;
            }
            else {
                logger.info(String.format("2PC COMMIT - Só quem pode realizar o COMMIT é quem fez o PREPARE"));
                // Se a operação que está tentando destravar não for a mesma que fez a trava
                res.responseNode = ResponseNode.WITHOUT_AUTHORIZATION_TO_COMMIT;
                //throw new InvalidOperation("Só quem pode retirar a trava é a operação que fez a trava");
            }
        }
        // Se for em um banco externo
        else {
            try{
                uri = consortium.get(operation.getBankCode()).getBankUrl() + "bank/commit"; // Pega o uri do banco
                logger.info(String.format("2PC COMMIT - Realizando COMMIT externo para operação{%s} - uri{%s}", operation.getOid(), uri));
                _2PCResponse resNode =  this.httpService.post2PC( uri, PresenterOperation.modelToDto(operation));
            } catch (Exception e){ // Se não consegui contatar o outro banco
                res.responseNode = ResponseNode.NOT_CAN_COMMIT;
            }
        }
        return res;
    }


    public _2PCResponse requestDoAbort(Operation operation){
        String uri;
        _2PCResponse res = new _2PCResponse();

        // Se for no banco atual/local
        if(operation.getBankCode().equals(Bank.getBankCode())){
            // Verifico se a operação que quer remover a trava é a mesma que fez a trava
            String aux = this.preparedLocal.get(operation.getAccountCode());
            if ( aux.equals(operation.getOid()) ){
                // Retira a trava da conta
                this.preparedLocal.remove(operation.getAccountCode());
            }
            else {
                // Se a operação que está tentando destravar não for a mesma que fez a trava
                throw new InvalidOperation("Só quem pode retirar a trava é a operação que fez a trava");
            }
        }
        // Se for em um banco externo
        else {
            uri = consortium.get(operation.getBankCode()).getBankUrl() + "bank/abort"; // Pega o uri do banco
            _2PCResponse aux = this.httpService.post2PC( uri, PresenterOperation.modelToDto(operation));
        }
        res.oid = operation.getOid();
        res.accountCode = operation.getAccountCode();
        res.responseNode = ResponseNode.ABORTTED;

        return res;
    }
}

/**
 * canCommit?(trans) → Sim / Não
 * Chamada do coordenador ao participante para perguntar se ele pode confirmar uma transação.
 * O participante responde com seu voto.
 *
 *
 * doCommit(trans)
 * Chamada do coordenador ao participante para dizer a ele para que confirme sua parte de
 * uma transação.
 *
 *
 * doAbort(trans)
 * Chamada do coordenador ao participante para dizer a ele para que cancele sua parte de uma transação.
 * haveCommitted(trans, participante)
 * Chamada do participante ao coordenador para confirmar que efetivou a transação.
 *
 *
 * getDecision(trans) → Sim/ Não
 * Chamada do participante ao coordenador para solicitar a decisão em uma transação, após ter votado
 * em Sim, mas ainda não ter recebido resposta devido a algum atraso. Usada para se recuperar de uma
 * falha de servidor ou de mensagens retardadas.
 */