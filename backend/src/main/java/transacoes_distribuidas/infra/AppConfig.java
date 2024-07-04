package transacoes_distribuidas.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import transacoes_distribuidas.model.Bank;
import transacoes_distribuidas.model.Retries;
import transacoes_distribuidas.model.Transaction;
import transacoes_distribuidas.services.ThreadRetriesProcessor;
import transacoes_distribuidas.services.ThreadTransactionProcessor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Configuration
public class AppConfig {

    /** Forma de inserir, de maneira estática o valor do bankCode em Bank
     *
     * @param bankCode
     */
    @Value("${bank.code}")
    public void setBankCode(String bankCode) {
        Bank.setBankCode(bankCode);
    }


    @Bean
    public BlockingQueue<Transaction> blockingQueue() {
        return new LinkedBlockingQueue<>(64);
    }

    @Bean
    public BlockingQueue<Retries> blockingQueueRetries() {
        return new LinkedBlockingQueue<>(128);
    }

    @Bean
    public ThreadTransactionProcessor consumerTransaction(BlockingQueue<Transaction> queue) {
        ThreadTransactionProcessor consumer = new ThreadTransactionProcessor(queue);
        new Thread(consumer).start(); // Inicia o consumidor
        return consumer;
    }

    @Bean
    public ThreadRetriesProcessor consumerRetries(BlockingQueue<Retries> queue) {
        ThreadRetriesProcessor consumer = new ThreadRetriesProcessor(queue);
        new Thread(consumer).start(); // Inicia o consumidor
        return consumer;
    }
    
}
