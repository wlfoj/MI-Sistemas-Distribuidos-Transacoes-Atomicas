package transacoes_distribuidas.exceptions;

public class InvalidOperation extends RuntimeException{
    public InvalidOperation(String message) {
        super(message);
    }

    public InvalidOperation(String message, Throwable cause) {
        super(message, cause);
    }
}

