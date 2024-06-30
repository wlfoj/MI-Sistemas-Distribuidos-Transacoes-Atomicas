package transacoes_distribuidas.model;

public class PessoaConjunta implements Pessoa{

    private String cpf1;
    private String cpf2;

    public PessoaConjunta(String cpf1, String cpf2) {
        this.cpf1 = cpf1;
        this.cpf2 = cpf2;
    }

    public String getCpf1() {
        return cpf1;
    }

    public String getCpf2() {
        return cpf2;
    }

    @Override
    public boolean containsId(String id) {
        if (this.cpf1.equals(id) || this.cpf2.equals(id)){
            return true;
        }
        return false;
    }
    @Override
    public boolean containsId(String id1, String id2){
        if ((this.cpf1.equals(id1) && this.cpf2.equals(id2)) || (this.cpf1.equals(id2) && this.cpf2.equals(id1))){
            return true;
        }
        return false;
    }

    public String getConcatCpf(){
        return  this.cpf1 + this.cpf2;
    }
}
