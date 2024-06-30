package transacoes_distribuidas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import transacoes_distribuidas.dto.out.ErrorDetailsResponse;
import transacoes_distribuidas.exceptions.AccountInUse;
import transacoes_distribuidas.exceptions.ResourceAlreadyExists;
import transacoes_distribuidas.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetailsResponse errorDetails = new ErrorDetailsResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<?> handleInterruptedException(Exception ex, WebRequest request) {
        //ErrorDetailsResponse errorDetails = new ErrorDetailsResponse(ex.getMessage(), request.getDescription(false));
        ErrorDetailsResponse errorDetails = new ErrorDetailsResponse("Não foi possível realizar a transação, tente novamente", request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceAlreadyExists.class)
    public ResponseEntity<?> handleResourceAlreadyExists(Exception ex, WebRequest request) {
        ErrorDetailsResponse errorDetails = new ErrorDetailsResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(AccountInUse.class)
    public ResponseEntity<?> handleAccountInUse(Exception ex, WebRequest request) {
        ErrorDetailsResponse errorDetails = new ErrorDetailsResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }


    /** Tive que usar uma verificação com ifs, pois a mesma classe de exceção é levantada em dois cenários diferentes (O Spring faz isso e não consegui mudar)
     *
     */
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<?> handleRequestTimeOut(Exception ex, WebRequest request) {
        ErrorDetailsResponse errorDetails = new ErrorDetailsResponse(ex.getMessage(), request.getDescription(false));
        // Se recebi TimeOut, significa que ele está lá, mas demorou de me responder
        if (ex.getCause().getMessage() == null){
            return new ResponseEntity<>(errorDetails, HttpStatus.GATEWAY_TIMEOUT);
        }
        // Se recebi uma conexão recusada, significa que outro nó está fora de serviço
        else if (ex.getCause().getMessage().contains("Connection refused")) {
            return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
        }
        // Caso "geral" que não consigo especificar
        else {
            return new ResponseEntity<>(errorDetails, HttpStatus.BAD_GATEWAY);
        }
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetailsResponse errorDetails = new ErrorDetailsResponse(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Defina mais métodos de tratamento de exceções conforme necessário
}
