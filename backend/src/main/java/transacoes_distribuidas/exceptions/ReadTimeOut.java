package transacoes_distribuidas.exceptions;

public class ReadTimeOut extends RuntimeException{
    public ReadTimeOut(String message) {
        super(message);
    }

    public ReadTimeOut(String message, Throwable cause) {
        super(message, cause);
    }
}
