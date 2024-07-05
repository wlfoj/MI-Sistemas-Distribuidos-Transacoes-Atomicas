package transacoes_distribuidas.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import transacoes_distribuidas.model.Bank;
import transacoes_distribuidas.model.Retries;
import transacoes_distribuidas.model.Transaction;
import transacoes_distribuidas.services.ThreadRetriesProcessor;
import transacoes_distribuidas.services.ThreadTransactionProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    /** Aqui eu obtenho a string com informaçoes sobre a inforção onde cada banco está, faço um parse e crio o meu hashMap
     *
     * @param bankStruct
     * @return
     */
    @Bean
    @Value("${bank.struct}")
    public Map<String, String> consortiumMap(String bankStruct){
        System.out.println(String.format("Recebi %s da variavel de ambiente", bankStruct));

        String aux = bankStruct;
        Map<String, String> map = new HashMap<String, String>();

        // Se n tem nada na var de ambiente, seto as configurações de localhost
        if (aux.equals("0")){
            aux = "[{\"bankCode\": \"1\", \"url\": \"http://127.0.0.1:8080/\"}, {\"bankCode\": \"9\", \"url\": \"http://127.0.0.1:8081/\"}, {\"bankCode\": \"8\", \"url\": \"http://127.0.0.1:8082/\"}]";
        }
        System.out.println(String.format("Estou usando %s da para o consorcio", aux));

        List<String> objs  = extractJsonObjects(aux);
        //System.out.println(String.format("Peguei os objetos: %s", objs.toString()));
        for (String json: objs) {
            parseJsonObject(json, map);
        }
        System.out.println(String.format("O bancos que tenho acesso: %s", map.toString()));
        return map;
    }


    /** Cria a lista com transações que deverão ser processadas
     *
     * @return
     */
    @Bean
    public BlockingQueue<Transaction> blockingQueue() {
        return new LinkedBlockingQueue<>(64);
    }

    /** Cria a lista para onde as requisições que falham irão para uma nova tentativa
     *
     * @return
     */
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


    /** Método auxiliar para obter os 'objetos' da string. Auxiliar para o parse
     *
     * @param jsonString
     * @return
     */
    private static List<String> extractJsonObjects(String jsonString) {
        List<String> jsonObjects = new ArrayList<>();
        int startIndex = 0;

        while ((startIndex = jsonString.indexOf('{', startIndex)) != -1) {
            int endIndex = jsonString.indexOf('}', startIndex);
            if (endIndex != -1) {
                jsonObjects.add(jsonString.substring(startIndex, endIndex + 1));
                startIndex = endIndex + 1;
            } else {
                break;
            }
        }

        return jsonObjects;
    }

    /** Método para realizar o parse nos 'objetos' que representam os dados de cada banco
     *
     * @param jsonObject -> Json em string, algo do tipo: {"bankCode": "9", "url": "http://172.16.103.9:8080/"}
     * @param consortiumMap
     */
    private static void parseJsonObject(String jsonObject, Map<String, String> consortiumMap) {
        String[] parts = jsonObject.replace("\":", "::").replace("{", "").replace("}", "").replace("\"", "").split(",");
        String bankCode = null;
        String url = null;

        for (String part : parts) {
            System.out.println(part);
            String[] keyValue = part.split("::");
            if (keyValue[0].trim().equals("bankCode")) {
                bankCode = keyValue[1].trim();
            } else if (keyValue[0].trim().equals("url")) {
                url = keyValue[1].trim();
            }
        }
        consortiumMap.put(bankCode, url);
    }
}
