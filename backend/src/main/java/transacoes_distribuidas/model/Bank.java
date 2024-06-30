package transacoes_distribuidas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;
import transacoes_distribuidas.exceptions.ResourceAlreadyExists;
import transacoes_distribuidas.exceptions.ResourceNotFoundException;

@Component
public class Bank {
    private String name;
    //@Value("${bank.code}")
    private static String bankCode; // Inserido de forma estática na hora que a aplicação é configurada
    private final List<Transaction> historyTransactions = new ArrayList<>(); // Só guarda as transações que foram iniciadas aqui
    private final List<Account> accounts = new ArrayList<>();
    private final ReentrantLock lockAccounts = new ReentrantLock();// É um Mutex para a lista de contas
    private final ReentrantLock lockTransactions = new ReentrantLock();// É um Mutex para a lista de transações

    public static String getBankCode() {
        return bankCode;
    }
    public static void setBankCode(String bankId){ bankCode = bankId;}

    public void addHistoryTransactions(Transaction transaction){
        this.lockTransactions.lock();
        this.historyTransactions.add(transaction);
        this.lockTransactions.unlock();
    }

    public void updateTransactionStatusInHistory(String tid, TransactionStatus status){
        this.lockTransactions.lock();
        for (Transaction transaction: this.historyTransactions) {
            if (transaction.getTid().equals(tid)){
                transaction.setTransactionStatus(status);
                break;
            }
        }
        this.lockTransactions.unlock();
    }

    public Transaction findTransactionInHistory(String tid){
        Transaction res = null;
        this.lockTransactions.lock();
        for (Transaction transaction: this.historyTransactions) {
            if (transaction.getTid().equals(tid)){
                res = transaction;
                break;
            }
        }
        this.lockTransactions.unlock();
        if (res == null){
            throw new ResourceNotFoundException("Não existe transação com o id especificado");
        }
        return res;
    }

    // ================================ ENCONTRA AS CONTAS
    public List<Account> findAllAccountsByCpf(String cpf) {
        List<Account> accounts_resp = new ArrayList<>();
        this.lockAccounts.lock();
        for (Account account : this.accounts) {
            if (account.getPessoa().containsId(cpf)) {
                accounts_resp.add(account);
            }
        }
        this.lockAccounts.unlock();
        // Se não tiver achado ninguém, retorne vazio
        if (accounts_resp.size() == 0){
            throw new ResourceNotFoundException("Não existe conta com este cpf cadastrada");
        }
        return accounts_resp; // Retorna null se não encontrar nenhuma conta com o CPF especificado
    }

    public Account findAccountByAccountId(String accountId) {
        Account res = null;
        this.lockAccounts.lock();
        for (Account account : this.accounts) {
            if (account.getAccountId().equals(accountId)) {
                res = account;
                break;
            }
        }
        this.lockAccounts.unlock();
        if (res == null){
            // Se não tiver achado ninguém, gere exceção
            throw new ResourceNotFoundException("Não existe conta com este cpf cadastrada");
        }
        return res;
    }

    // ================================ VERIFICA SE JÁ EXISTE
    public boolean accountIdAlreadyRegistered(String accountId) {
        boolean resp = false;
        this.lockAccounts.lock();
        for (Account account : accounts) {
            if (account.getAccountId().equals(accountId)) {
                resp = true;
                break;
            }
        }
        this.lockAccounts.unlock();
        return resp;
    }

    /** Verifica se o cpf ou cnpj em questão já possui conta. Utilizado para verificações em
     * contas fisicas e juridicas.
     *
     * @param cpf_cnpj
     * @return
     */
    public boolean cpf_cnpjAlreadyRegistered(String cpf_cnpj) {
        boolean aux = false;
        this.lockAccounts.lock();
        for (Account account : accounts) {
            // Se for uma conta conjunta, não faço a verificação
            if (account.getPessoa() instanceof PessoaConjunta ){
                continue;
            }
            if (account.getPessoa().containsId(cpf_cnpj)) {
                aux = true;
            }
        }
        this.lockAccounts.unlock();
        return aux;
    }

    /** Verifica se o cpf ou cnpj em questão já possui conta. Utilizado para verificações em
     * contas conjuntas.
     *
     * @param cpf1
     * @param cpf2
     * @return
     */
    public boolean cpf_cnpjAlreadyRegistered(String cpf1, String cpf2) {
        boolean aux = false;
        this.lockAccounts.lock();
        for (Account account : accounts) {
            if (!(account.getPessoa() instanceof PessoaConjunta)){
                continue;
            }
            if (account.getPessoa().containsId(cpf1, cpf2)) {
                aux = true;
            }
        }
        this.lockAccounts.unlock();
        return aux;
    }


    // ================================ CRIA AS CONTAS
    public Account createAccountFisica(String password, String cpf){
        boolean aux;
        // Se já não houver registrado
        if (!cpf_cnpjAlreadyRegistered(cpf)){
            PessoaFisica p1 = new PessoaFisica(cpf);
            Account account = new Account(p1, p1.getCpf(), password, 0, this.bankCode);

            this.lockAccounts.lock();
            aux = this.accounts.add(account);
            this.lockAccounts.unlock();

            if (aux == true){
                return account;
            }
        }
        throw new ResourceAlreadyExists("Já existe uma conta com este cpf registgrada");
        //return null;
    }
    public Account createAccountJuridica(String password,String cnpj){
        boolean aux;
        // Se já não houver registrado
        if (!cpf_cnpjAlreadyRegistered(cnpj)){
            PessoaJuridica p1 = new PessoaJuridica(cnpj);
            Account account = new Account(p1, p1.getCnpj(), password, 0, this.bankCode);
            // Verifico se consegui adicionar

            this.lockAccounts.lock();
            aux = this.accounts.add(account);
            this.lockAccounts.unlock();

            if (aux == true){
                return account;
            }
        }
        throw new ResourceAlreadyExists("Já existe uma conta com este cnpj registgrada");
        //return null;
    }
    public Account createAccountConjunta(String password, String cpf1, String cpf2){
        boolean aux;
        // Se já não houver registrado
        if (!cpf_cnpjAlreadyRegistered(cpf1, cpf2)){
            PessoaConjunta p1 = new PessoaConjunta(cpf1, cpf2);
            Account account = new Account(p1, p1.getConcatCpf(), password, 0, this.bankCode);
            // Verifico se consegui adicionar

            this.lockAccounts.lock();
            aux = this.accounts.add(account);
            this.lockAccounts.unlock();

            if (aux == true){
                return account;
            }
        }
        throw new ResourceAlreadyExists("Já existe uma conta com este par de cpf registrada");
        //return null;
    }







    /** Realiza a operação na conta, identificando se é operação de saque ou de deposito.
     *
     * @param op -> Operação que será processada.
     */
    public void makeOperation(Operation op, String accountId){
        Account account = this.findAccountByAccountId(accountId);// Se não encontrar, recebo uma exceção
        if (op.getOperationType() == OperationType.DEPOSIT){
            account.makeDeposit(op.getValue());
        }
        else {// WITHDRAW
            account.makeWithdraw(op.getValue());
        }
    }


    /** Utilizado para saber se uma conta será capaz de realizar determinada operação.
     * Para uso exclusivo do Two Phase Commit Coordinator
     *
     * @param op
     * @param accountId
     * @return
     */
    public boolean canDoIt(Operation op, String accountId){
        Account acc = findAccountByAccountId(accountId);
        return acc.canDoIt(op);
    }

}
