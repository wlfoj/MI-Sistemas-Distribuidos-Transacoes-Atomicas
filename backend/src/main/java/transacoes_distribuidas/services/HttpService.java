package transacoes_distribuidas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import transacoes_distribuidas.dto.out.AccountsResponse;
import transacoes_distribuidas.dto.out._2PCResponse;

@Service
public class HttpService {


    private final WebClient webClient;

    @Autowired
    public HttpService(WebClient webClientBuilder) {
        this.webClient = webClientBuilder;
    }

    /** Realiza uma requisição do tipo GET
     *
     * @param uri -> o endereço completo da requisição. Se houver paramtros, deve estar na string.
     * @return
     */
    public AccountsResponse getAccountsResponseData(String uri) {
        return this.webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(AccountsResponse.class).block();
    }

    /** Realiza uma requisição do tipo POST
     *
     * @param uri  -> O endereço completo da requisição. Se houver paramtros, deve estar na string.
     * @param requestData -> O body a ser enviado na requisição
     * @return
     */
    public _2PCResponse post2PC(String uri, Object requestData) {
        return this.webClient.post()
                .uri(uri)
                .body(Mono.just(requestData), Object.class)
                .retrieve()
                .bodyToMono(_2PCResponse.class).block();
    }




}
