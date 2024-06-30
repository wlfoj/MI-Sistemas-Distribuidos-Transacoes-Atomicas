package transacoes_distribuidas.presenters;

import transacoes_distribuidas.dto.in.CreateTransactionRequest;
import transacoes_distribuidas.model.Transaction;

public class PresenterTransaction {
    /** Método para converter uma transação dto em objeto do model
     *
     * @param t
     * @return
     */
    public static Transaction dtoToModel(CreateTransactionRequest t){
        Transaction model = new Transaction(
                PresenterOperation.listDtoToModel(t.operations), t.source, t.transactionType
        );
        return model;
    }
}
