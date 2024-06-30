package transacoes_distribuidas.dto.out;

public class ErrorDetailsResponse {
    public String message;
    public String description;

    public ErrorDetailsResponse(String message, String description) {
        this.message = message;
        this.description = description;
    }
}
