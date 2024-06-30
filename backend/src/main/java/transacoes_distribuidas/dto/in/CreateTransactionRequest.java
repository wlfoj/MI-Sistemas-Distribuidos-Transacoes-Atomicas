package transacoes_distribuidas.dto.in;

import transacoes_distribuidas.dto.SourceOrDestinyTransaction;
import transacoes_distribuidas.model.Transaction;
import transacoes_distribuidas.model.TransactionType;
import transacoes_distribuidas.presenters.PresenterOperation;

import java.util.List;

public class CreateTransactionRequest {
    public TransactionType transactionType;
    public SourceOrDestinyTransaction source; // Informando de onde veio a origem da transação
    public List<OperationRequest> operations; // Origens da transferẽncia

}
