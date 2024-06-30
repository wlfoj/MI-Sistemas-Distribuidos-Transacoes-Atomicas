package transacoes_distribuidas.model;

public class PessoaFisica implements Pessoa{
    private String cpf;

    public PessoaFisica(String cpf) {
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    @Override
    public boolean containsId(String id) {
        if (this.cpf.equals(id)){
            return true;
        }
        return false;
    }

    @Override
    public boolean containsId(String id1, String id2){
        if ((this.cpf.equals(id1) || this.cpf.equals(id2))){
            return true;
        }
        return false;
    }
}
