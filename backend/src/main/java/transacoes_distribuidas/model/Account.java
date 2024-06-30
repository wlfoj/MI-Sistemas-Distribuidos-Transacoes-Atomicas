package transacoes_distribuidas.model;

import org.springframework.beans.factory.annotation.Value;
import transacoes_distribuidas.exceptions.InsufficientFunds;
import transacoes_distribuidas.exceptions.NegativeValue;

import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private Pessoa pessoa;// Dados pessoais
    private String accountId;// Id da conta. É o mesmo cpf ou cnpj, em contas conjuntas, é a concatenação dos dois
    private String password;// Senha da conta
    private float balance;// Saldo
    //private AccountStatus status;
    private final ReentrantLock lock = new ReentrantLock();// É um Mutex

    @Value("${bank.code}")
    private String bankCode;

    public Account(Pessoa pessoa, String accountId, String password, float balance, String bankCode) {
        this.pessoa = pessoa;
        this.accountId = accountId;
        this.password = password;
        this.balance = balance;
        this.bankCode = bankCode;
    }

    public Account() {
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getBalance() {
        return balance;
    }


    /** Realiza a operação de Deposito
     *
     * @param value
     * @return
     */
    public float makeDeposit(float value){
        float balance;
        lock.lock(); // Adquire o bloqueio
        if (value < 0){// Se o valor de deposito for negativo
            lock.unlock(); // Libera o bloqueio
            throw new NegativeValue("O valor de saque deve ser positivo");
        }
        this.balance = this.balance + value;
        balance = this.balance;
        lock.unlock(); // Libera o bloqueio
        return balance;
    }

    /** Realiza a operação da Saque
     *
     * @param value
     * @return
     */
    public float makeWithdraw(float value){
        float balance;
        lock.lock(); // Adquire o bloqueio
        if (value > this.balance){// Se o valor de saque for maior que o saldo
            lock.unlock(); // Libera o bloqueio
            throw new InsufficientFunds("Você não tem saldo suficiente para esta operação");
        }
        if (value < 0){// Se o valor de saque for negativo
            lock.unlock(); // Libera o bloqueio
            throw new NegativeValue("O valor de saque deve ser positivo");
        }
        this.balance = this.balance - value;
        balance = this.balance;
        lock.unlock(); // Libera o bloqueio
        return balance;
    }

    public String getBankCode() {
        return bankCode;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    /** Utilizado para saber se uma conta será capaz de realizar determinada operação.
     * Para uso exclusivo do Two Phase Commit Coordinator
     *
     * @param op -> Operação a ser verificada a possibilidade de execução
     * @return True caso seja possível executar, e False caso contrário.
     */
    public boolean canDoIt(Operation op){
        boolean res = true;
        if(op.getValue() < 0){ // Operações com  valores negativos
            res = false;
        }
        // Qualque deposito, desde que não seja negativo, será permitido
        if (op.getOperationType() == OperationType.WITHDRAW) {
            if (this.balance - op.getValue() < 0){ // Saldo insuficiente
                res = false;
            }
        }
        return res;
    }

}
