package transacoes_distribuidas.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import transacoes_distribuidas.model.Bank;
import transacoes_distribuidas.services.ThreadServiceDiscovery;


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




    /**
    @Bean
    public ThreadServiceDiscovery consumerServiceDiscovery() {
        ThreadServiceDiscovery consumer = new ThreadServiceDiscovery();
        new Thread(consumer).start(); // Inicia o consumidor
        return consumer;
    }
    */
}
