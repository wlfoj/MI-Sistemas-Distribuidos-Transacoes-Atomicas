package transacoes_distribuidas.dto.in;

import transacoes_distribuidas.model.AccountType;

public class CreateAccountRequest {
    public String cpf1;
    public String cpf2;
    public String cnpj;
    public String password;
    public AccountType accountType;

    public CreateAccountRequest(String cpf1, String cpf2, String cnpj, String password, AccountType accountType) {
        this.cpf1 = cpf1;
        this.cpf2 = cpf2;
        this.cnpj = cnpj;
        this.password = password;
        this.accountType = accountType;
    }
}
