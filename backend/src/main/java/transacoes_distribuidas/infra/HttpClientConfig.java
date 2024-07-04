package transacoes_distribuidas.infra;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import transacoes_distribuidas.exceptions.ConnectTimeOut;
import transacoes_distribuidas.exceptions.ReadTimeOut;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(2)) // Timeout de resposta
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000) // Timeout de conexão
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(2)) // Timeout de leitura
                                .addHandlerLast(new WriteTimeoutHandler(2)) // Timeout de escrita
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> next.exchange(request)
                        .doOnError(throwable -> {
                            if (throwable.getCause() instanceof io.netty.handler.timeout.ReadTimeoutException) {
                                throw new ReadTimeOut("TimeOut de leitura e escrita");
                            } else if (throwable.getCause() instanceof io.netty.channel.ConnectTimeoutException) {
                                throw new ConnectTimeOut("Falha ao conectar ao host");
                            } else {
                                throw new RuntimeException("Outro erro", throwable);
                            }
                        })
                )
                .build();
    }
}