package transacoes_distribuidas.exceptions;

public class AccountInUse extends RuntimeException{
    public AccountInUse(String message) {
        super(message);
    }

    public AccountInUse(String message, Throwable cause) {
        super(message, cause);
    }
}
