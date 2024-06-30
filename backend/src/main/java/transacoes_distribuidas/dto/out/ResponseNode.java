package transacoes_distribuidas.dto.out;

public enum ResponseNode {
    YES_CAN_COMMIT,
    NOT_CAN_COMMIT,
    ACCOUNT_IN_USE,
    WITHOUT_AUTHORIZATION_TO_COMMIT,
    COMMITTED,
    ACCOUNT_NOT_IN_LOCK,
    ABORTTED,

}
