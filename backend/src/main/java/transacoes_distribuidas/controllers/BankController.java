package transacoes_distribuidas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import transacoes_distribuidas.dto.in.OperationRequest;
import transacoes_distribuidas.dto.out.*;

import transacoes_distribuidas.services.BankService;
import transacoes_distribuidas.services.TwoPhaseCommitCoordinator;


@RestController
@RequestMapping("/bank")
public class BankController {
    @Autowired
    private BankService bankService;

    @Autowired
    private TwoPhaseCommitCoordinator twoPhaseCommitCoordinator;


    // ============================================================================================================= //
    // ===================================== ENDPOINTS USADO ENTRE BANCOS ========================================== //
    // ============================================================================================================= //

    /** Obtem as contas, localmente, que o cpf tem acesso
     *
     * @param id -> É o cpf do usuário
     * @return
     */
    @GetMapping("/accountsIn/{id}")
    public ResponseEntity<AccountsResponse> getIternAccounts(@PathVariable("id") String id){
        AccountsResponse res = this.bankService.getAccountsIn(id);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }


    // ============================================================================================================= //
    // =============================== ENDPOINTS USADOS PARA O TWO PHASE COMMIT ==================================== //
    // ============================================================================================================= //
    /** Rota usada para sinalizar a fase de preparação.
     * Verifico se é possível realizar a operação e, caso seja, faço um bloqueio da conta em questão.
     *
     * @param req
     * @return
     */
    @PostMapping("/prepare")
    public ResponseEntity<_2PCResponse> prepareOperation(@RequestBody OperationRequest req){
        _2PCResponse res = this.bankService.canCommit(req);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    /** Realiza o commit de uma operação, a conta já deve estar bloqueada (passado pela fase de preparação)
     *  Faço a operação e desbloqueio a conta.
     *
     * @param req
     * @return
     */
    @PostMapping("/commit")
    public ResponseEntity<_2PCResponse> commitOperation(@RequestBody OperationRequest req){
        //BalanceResponse resp = null;
        _2PCResponse res = this.bankService.doCommit(req);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    /** Aborta a operação (só funciona caso esteja em fase de preparação)
     *
     * @param req
     * @return
     */
    @PostMapping("/abort")
    public ResponseEntity<_2PCResponse> abortOperation(@RequestBody OperationRequest req){
        //BalanceResponse resp = null;
        _2PCResponse res = this.bankService.abort(req);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }



    // ============================================================================================================= //
    // ============================ ENDPOINTS USADOS PARA A DESCOBERTA DE SERVIÇO ================================== //
    // ============================================================================================================= //
    // Ambos os métodos não puderam ser testados
    /** Usado para informar quem quiser saber sobre o meu serviço. Solicitado pelo Service Discovery.
     *
     * @return
     */
    @Deprecated
    @GetMapping("/serviceDiscovery")
    public ResponseEntity<ServiceDiscoveryResponse> serviceDiscovery(){
        ServiceDiscoveryResponse res = this.bankService.serviceDiscovery();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    /** Usado para informar os serviços que foram encontrados
     *
     * @return
     */
    @Deprecated
    @GetMapping("/showServices")
    public ResponseEntity<String> showServices(){
        String res = this.bankService.showConsortium();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
