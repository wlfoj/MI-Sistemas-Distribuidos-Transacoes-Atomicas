package transacoes_distribuidas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import transacoes_distribuidas.dto.out._2PCResponse;
import transacoes_distribuidas.model.Retries;

import java.util.concurrent.BlockingQueue;

public class ThreadRetriesProcessor extends Thread{

    private final BlockingQueue<Retries> queue;
    @Autowired
    private BankService bankService;

    @Autowired
    private HttpService httpService;

    public ThreadRetriesProcessor(BlockingQueue<Retries> queue) {
        this.queue = queue;
    }

    private static final Logger logger = LoggerFactory.getLogger(ThreadTransactionProcessor.class);

    @Override
    public void run() {
        logger.info("ASYNC RETRIES - Thread consumidora de retries foi iniciada");
        try {
            while (true) {
                Retries retries = queue.take(); // Remove um elemento da fila, esperando se necessário
                logger.info(String.format("ASYNC RETRIES- Iniciei a nova tentativa de entregar oid{%s} para uri{%s}", retries.operation.getOid(), retries.uri));

                try{
                    _2PCResponse res = this.httpService.post2PC(retries.uri, retries.operation);
                }catch (Exception e){// Se deu algum erro, coloco novamente
                    logger.info(String.format("ASYNC RETRIES- Adicionei o retries na fila novamente, devido a uma erro na entrega. oid{%s} para uri{%s}", retries.operation.getOid(), retries.uri));
                    this.queue.put(retries);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
