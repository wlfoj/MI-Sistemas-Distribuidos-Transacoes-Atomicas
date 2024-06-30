package transacoes_distribuidas.infra;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5)) // Timeout de resposta
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Timeout de conexão
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5)) // Timeout de leitura
                                .addHandlerLast(new WriteTimeoutHandler(5)) // Timeout de escrita
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}