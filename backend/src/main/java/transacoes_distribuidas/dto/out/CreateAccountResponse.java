package transacoes_distribuidas.dto.out;

public class CreateAccountResponse {
    public String accountId;
    public String bankCode;
    public String message;


    public CreateAccountResponse(String bankCode, String accountId, String message) {
        this.accountId = accountId;
        this.message = message;
        this.bankCode = bankCode;
    }


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String CPF) {
        this.accountId = accountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
