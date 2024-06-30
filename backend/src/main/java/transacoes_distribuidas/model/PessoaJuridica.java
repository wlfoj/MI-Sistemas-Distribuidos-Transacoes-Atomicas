package transacoes_distribuidas.model;

public class PessoaJuridica implements Pessoa{
    private String cnpj;

    public PessoaJuridica(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCnpj() {
        return cnpj;
    }

    @Override
    public boolean containsId(String id) {
        if (this.cnpj.equals(id)){
            return true;
        }
        return false;
    }

    @Override
    public boolean containsId(String id1, String id2){
        if ((this.cnpj.equals(id1) || this.cnpj.equals(id2))){
            return true;
        }
        return false;
    }
}
