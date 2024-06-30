package transacoes_distribuidas.services;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import transacoes_distribuidas.dto.out.ServiceDiscoveryResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;


/** Esta thread é usada para a descoberta de serviços seguindo um método de verificação em todos os ip's um a um (veja o código para entender melhor).
 *
 */
public class ThreadServiceDiscovery extends Thread{

    @Autowired
    private Map<String, String> consortium;
    private static final Logger logger = LoggerFactory.getLogger(ThreadTransactionProcessor.class);

    private final WebClient webClient;

    public ThreadServiceDiscovery(){
        // Cria o webclient próprio, para não usar o outro
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(1)) // Timeout de resposta
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // Timeout de conexão
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(1)) // Timeout de leitura
                        .addHandlerLast(new WriteTimeoutHandler(1)) // Timeout de escrita
                );

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /** Realiza uma requisição do tipo GET
     *
     * @param uri -> o endereço completo da requisição. Se houver paramtros, deve estar na string.
     * @return
     */
    private ServiceDiscoveryResponse discoveryServices(String uri) {
        return this.webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ServiceDiscoveryResponse.class).block();
    }


    @Override
    public void run() {
        logger.info("SERVICE DISCOVERY - Thread descobridora de serviços foi iniciada");
        String ip = null; // ip da máquina local
        String ipToDiscovery = null; // ip para se descobrir
        String uri = null; // possível url do banco

        try {
            // Sleep por Delay de 15 segundos, para dar tempo de todos os serviços subirem
            Thread.sleep(15000);
            while (true) {
                logger.info("SERVICE DISCOVERY - Iniciando uma descoberta");

                /** Sempre estou pegando o ip atual da minha máquina, pois o ip pode mudar (supostamente) */
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                /** Busca na força bruta no HTTP por serviços dentro dos computadores do LARSID. Do computador 1 ao 9 */
                for (int i = 1; i < 10; i++) {
                    ipToDiscovery = "172.16.103."; // Endereço de rede do LARSID
                    ipToDiscovery = ipToDiscovery + String.valueOf(i); // Resulta em "172.16.103.1" por exemplo ...
                    // Se o ip for o mesmo da máquina que está executando este código
                    if (! ipToDiscovery.equals(ip)){
                        //uri = "http://"+ipToDiscovery+":8080/bank/serviceDiscovery"; // http://172.16.103.1:8080/bank/serviceDiscovery
                        uri = "http://"+"127.0.0.1"+":808"+ String.valueOf(i) +"/bank/serviceDiscovery";
                        try{
                            logger.info(String.format("SERVICE DISCOVERY - Iniciando a busca por %s", uri));
                            ServiceDiscoveryResponse res = this.discoveryServices(uri);
                            logger.info("SERVICE DISCOVERY - Encontrei um serviço, estou adicionando no consórcio");
                            uri = "http://"+res.ip+":"+res.port+"/";// http://172.16.103.1:8080
                            // Se fez tudo sem erro, adiciona no map do consórcio
                            this.consortium.put(res.bankCode, uri);
                            logger.info(String.format("SERVICE DISCOVERY - { %s }", this.consortium.toString()));
                        }catch (Exception e){
                            //Se deu erro, não faz nada
                        }
                    }
                }
                // Sleep por 60 segundos
                Thread.sleep(60000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
