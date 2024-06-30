package transacoes_distribuidas.dto.out;

public class BalanceResponse {
    public String bankCode;
    public String accountCode;
    public float value;

    public BalanceResponse() {
    }

    public BalanceResponse(String bankCode, String accountCode, float value) {
        this.bankCode = bankCode;
        this.accountCode = accountCode;
        this.value = value;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

}
