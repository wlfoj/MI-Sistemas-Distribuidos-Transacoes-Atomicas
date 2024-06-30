package transacoes_distribuidas.dto.in;

public class AuthRequest {
    public String cpf;
    public String password;

    public AuthRequest(String cpf, String password) {
        this.cpf = cpf;
        this.password = password;
    }
}
