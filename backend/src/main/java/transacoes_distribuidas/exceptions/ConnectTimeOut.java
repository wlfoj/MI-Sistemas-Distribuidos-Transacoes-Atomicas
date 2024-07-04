package transacoes_distribuidas.exceptions;

public class ConnectTimeOut extends RuntimeException{
    public ConnectTimeOut(String message) {
        super(message);
    }

    public ConnectTimeOut(String message, Throwable cause) {
        super(message, cause);
    }

}
