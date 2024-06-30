package transacoes_distribuidas.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConsortiumConfig {

    @Bean
    public Map<String, Consortium> consortiumMap(){
        Map<String, Consortium> map = new HashMap<String, Consortium>();
        map.put("1", Consortium.BANK1);
        map.put("2", Consortium.BANK2);
        map.put("3", Consortium.BANK3);
        return map;
    }

    @Bean
    public Map<String, String> consortiumMapString(){
        Map<String, String> map = new HashMap<String, String>();
        return map;
    }
}
