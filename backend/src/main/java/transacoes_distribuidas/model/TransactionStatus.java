package transacoes_distribuidas.model;

public enum TransactionStatus {
    CONCLUDED,
    FAILED,
    WAITING_COMMIT, // ESPERANDO A CONFIRMAÇÃO DE QUE DEU TUDO CERTO
}
