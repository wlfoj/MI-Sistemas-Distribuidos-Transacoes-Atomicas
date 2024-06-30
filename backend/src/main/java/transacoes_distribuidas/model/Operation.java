package transacoes_distribuidas.model;

/**
Unidade de transação, é isto que será enviado para os outros bancos
 */
public class Operation {
    private static long nextId = 1; // Atributo estático para armazenar o próximo ID disponível
    private String oid;// bankId.tid.id   -> EX: 2.44.4
    private OperationType operationType;
    private String bankCode;
    private String accountCode;
    private float value;

    public Operation(OperationType operationType, String bankCode, String accountCode, float value) {
        //this.creat_at = creat_at;
        this.operationType = operationType;
        this.bankCode = bankCode;
        this.accountCode = accountCode;
        this.value = value;
    }

    public String getOid() {
        return oid;
    }
    public void setOid(String oid){ this.oid = oid;}

    public OperationType getOperationType() {
        return operationType;
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

    /*
        public Operation(TransferType transferType, String bankCodeDestiny, String accountCodeDestiny, float value, String tid) {
        this.oid = String.format("%s.%s", tid,String.valueOf(nextId++));//prefixOid+"."+String.valueOf(nextId++);
        //this.creat_at = creat_at;
        this.transferType = transferType;
        this.bankCode = bankCodeDestiny;
        this.accountCode = accountCodeDestiny;
        this.value = value;
    }

    public Operation(String oid, TransferType transferType, String bankCode, String accountCode, float value) {
        this.oid = oid; // Já recebe no formato certo. EX -> "2.10.0"
        //this.creat_at = creat_at;
        this.transferType = transferType;
        this.bankCode = bankCode;
        this.accountCode = accountCode;
        this.value = value;
    }
     */
}
