package transacoes_distribuidas.exceptions;

public class NegativeValue extends RuntimeException{
    public NegativeValue(String message) {
        super(message);
    }

    public NegativeValue(String message, Throwable cause) {
        super(message, cause);
    }
}
