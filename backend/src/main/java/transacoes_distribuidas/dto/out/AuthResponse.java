package transacoes_distribuidas.dto.out;


public class AuthResponse {
    public String bankCode;
    public String accountId; // É o mesmo que CPF no caso (interprete como a chave pix)
    public String cpf;

    public AuthResponse(String bankCode, String accountId, String cpf) {
        this.bankCode = bankCode;
        this.accountId = accountId;
        this.cpf = cpf;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}
