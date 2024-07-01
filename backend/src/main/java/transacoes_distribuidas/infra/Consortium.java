package transacoes_distribuidas.infra;

public enum Consortium {
    BANK1("http://172.16.103.5:8080/", "1"),
    BANK2("http://172.16.103.6:8080/", "2"),
    BANK3("http://172.16.103.7:8080/", "3"),
    ;

    // TEM QUE LEMBRAR DE COLOCAR O IP DOS COMPUTADORES ONDE VAI PODER RODAR CADA BANCO
    private final String bankUrl;
    private final String bankCode;

    Consortium(String bankUrl, String bankCode) {
        this.bankUrl = bankUrl;
        this.bankCode = bankCode;
    }

    public String getBankUrl() {
        return bankUrl;
    }

    public String getBankCode() {
        return bankCode;
    }

    // Método estático para obter todos os elementos do enum
    public static Consortium[] getAllConsortiums() {
        return values();
    }
}
