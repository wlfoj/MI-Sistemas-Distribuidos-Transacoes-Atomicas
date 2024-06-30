package transacoes_distribuidas.exceptions;

public class NullFields extends RuntimeException{
    public NullFields(String message) {
        super(message);
    }

    public NullFields(String message, Throwable cause) {
        super(message, cause);
    }
}
