package transacoes_distribuidas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import transacoes_distribuidas.exceptions.AccountInUse;
import transacoes_distribuidas.model.Transaction;

import java.util.concurrent.BlockingQueue;

public class ThreadTransactionProcessor extends Thread{

    private final BlockingQueue<Transaction> queue;
    @Autowired
    private BankService bankService;

    @Autowired
    private TwoPhaseCommitCoordinator executor;

    public ThreadTransactionProcessor(BlockingQueue<Transaction> queue) {
        this.queue = queue;
    }

    private static final Logger logger = LoggerFactory.getLogger(ThreadTransactionProcessor.class);

    @Override
    public void run() {
        logger.info("ASYNC - Thread consumidora de transações iniciada");
        try {
            while (true) {
                Transaction transacao = queue.take(); // Remove um elemento da fila, esperando se necessário
                logger.info(String.format("ASYNC - Iniciei o processo da transação{%s}", transacao.getTid()));

                try{
                    this.executor.openTransaction(transacao);
                }catch (AccountInUse e){// Se recebi a exceção de conta já usada, coloco na fila novamente
                    this.queue.put(transacao);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
