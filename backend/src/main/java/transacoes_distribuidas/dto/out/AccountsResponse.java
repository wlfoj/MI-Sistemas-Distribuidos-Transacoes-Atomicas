package transacoes_distribuidas.dto.out;

import java.util.List;

public class AccountsResponse {
    public String cpf;// O cpf da pessoa que está verificando quais contas ela pode acessar
    public List<BalanceResponse> accountsToUse; // Todas as contas que ela pode movimentar dinheiro
}
