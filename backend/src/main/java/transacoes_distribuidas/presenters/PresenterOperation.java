package transacoes_distribuidas.presenters;

import transacoes_distribuidas.dto.in.OperationRequest;
import transacoes_distribuidas.model.Operation;

import java.util.ArrayList;
import java.util.List;

public class PresenterOperation {

    /** Método auxiliar que transforma um dto em um objeto do model.
     *
     * @param o -> Objeto operação recebido em uma solicitação no controller.
     * @return
     */
    public static Operation dtoToModel(OperationRequest o){
        Operation model = new Operation(o.operationType, o.bankCode, o.accountCode, o.value);
        model.setOid(o.oid);
        return model;
    }

    /** Converte uma lista de objetos dto em uma lista de objetos do model Operation
     *
     * @param listOperations
     * @return
     */
    public static List<Operation> listDtoToModel(List<OperationRequest> listOperations){
        List<Operation> list = new ArrayList<>();

        for (OperationRequest operation: listOperations) {
            list.add(dtoToModel(operation));
        }
        return list;
    }

    /** Método auxiliar que transforma um objeto do model em um objeto dto para envio pela rede.
     *
     * @param o -> Objeto operação utilizado na lógica de negócio.
     * @return
     */
    public static OperationRequest modelToDto(Operation o){
        OperationRequest dto = new OperationRequest();
        //
        dto.oid = o.getOid();
        dto.accountCode = o.getAccountCode();
        dto.bankCode = o.getBankCode();
        dto.operationType = o.getOperationType();
        dto.value = o.getValue();
        //
        return dto;
    }

    /** Método auxiliar que transforma uma lista de operações(model) em lista de operações(dto)
     *
     * @param listOperations
     * @return
     */
    public static List<OperationRequest> listModelToDto(List<Operation> listOperations){
        List<OperationRequest> list = new ArrayList<>();

        for (Operation operation: listOperations) {
            list.add(modelToDto(operation));
        }
        return list;
    }
}
