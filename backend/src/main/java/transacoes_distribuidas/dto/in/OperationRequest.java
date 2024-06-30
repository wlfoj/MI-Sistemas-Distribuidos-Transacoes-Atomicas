package transacoes_distribuidas.dto.in;

import transacoes_distribuidas.model.Operation;
import transacoes_distribuidas.model.OperationType;

import java.util.ArrayList;
import java.util.List;

public class OperationRequest {
    public String oid; // Id da operação = bankId.tid.oid
    public OperationType operationType;

    public String bankCode;
    public String accountCode;
    public float value;

}
