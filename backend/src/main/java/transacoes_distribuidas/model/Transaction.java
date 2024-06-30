package transacoes_distribuidas.model;
import transacoes_distribuidas.dto.SourceOrDestinyTransaction;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
É um conjunto de operações com uma lógica determinada
 */
public class Transaction {
    private static long nextId = 1; // Atributo estático para armazenar o próximo ID disponível (DA CLASSE)
    private String tid; // "44.4582182629.5"
    private TransactionType transactionType;
    private SourceOrDestinyTransaction source;
    private TransactionStatus transactionStatus; // Status da transação
    private LocalDateTime createAt; // Salvo a hora em que foi criado
    private List<Operation> operations; // Origens da transferẽncia

    /** Usado quando se cria a transação aqui
     *
     * @param operations
     * @param source
     */
    public Transaction(List<Operation> operations, SourceOrDestinyTransaction source, TransactionType transactionType) {
        // EX -> "4.578"
        this.tid = String.format("%s.%s", Bank.getBankCode(), String.valueOf(nextId++));//Bank.getBankCode()+"."+String.valueOf(nextId++);
        this.createAt = LocalDateTime.now();// Pega a data e hora atual
        //
        this.source = source;
        this.transactionType = transactionType;
        this.transactionStatus = TransactionStatus.WAITING_COMMIT;
        //this.operations = operations;
        // Seto as operações já com seu id global
        setOperations(operations);
    }

    public Transaction(String tid, List<Operation> operations, SourceOrDestinyTransaction source, TransactionType transactionType) {
        this.tid = tid; // Caso já tenha um id na transação
        this.transactionStatus = TransactionStatus.WAITING_COMMIT;// A transação é criada como pendente
        this.createAt = LocalDateTime.now();// Pega a data e hora atual
        //
        this.source = source;
        this.transactionType = transactionType;
        setOperations(operations);
    }

    public String getTid() {
        return tid;
    }

    public SourceOrDestinyTransaction getSource() {
        return source;
    }

    public void setSource(SourceOrDestinyTransaction source) {
        this.source = source;
    }


    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {

        this.operations = operations;
        // Inserindo os ids corretos para cada operação
        for (int i = 0; i < operations.size(); i++) {
            // Obtenho a operação e colocando o id completo. EX:   "2.45.7"
            this.operations.get(i).setOid( String.format("%s.%s",this.tid, String.valueOf(i)) );
        }
    }

    /** Método utilizado para saber já se passou 1min desde que a transação foi criada
     *
     * @return true se já passou 1min, false caso contrário.
     */
    public boolean hasExpired(){
        LocalDateTime agora = LocalDateTime.now();
        Duration duration = Duration.between(this.createAt, agora);
        return duration.toMinutes() > 1;
    }

}
