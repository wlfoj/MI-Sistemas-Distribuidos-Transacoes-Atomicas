package transacoes_distribuidas.model;

public interface Pessoa {
    // Verifico se contem cpf, cpnj na classe
    public boolean containsId(String id);
    public boolean containsId(String id1, String id2);
}
