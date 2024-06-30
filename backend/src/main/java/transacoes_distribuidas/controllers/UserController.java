package transacoes_distribuidas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import transacoes_distribuidas.dto.in.AuthRequest;
import transacoes_distribuidas.dto.in.CreateAccountRequest;
import transacoes_distribuidas.dto.in.CreateTransactionRequest;
import transacoes_distribuidas.dto.out.*;
import transacoes_distribuidas.model.Transaction;
import transacoes_distribuidas.services.BankService;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private BankService bankService;

/*=================================== =================================== =================================== ==========================
=================================== ENDPOINTS ACESSADOS PELO CLIENTE DE MANEIRA PUBLICA ===============================================
=================================== =================================== =================================== ===========================*/
    /** REALIZA O LOGIN DO USUARIO
     *
     * @param req
     * @return
     */
    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> auth(@RequestBody AuthRequest req){
        // Chama o serviço que retorna a conta ou null
        AuthResponse resp = this.bankService.getAccountToLogin(req);

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }


    /** CRIA UM CONTA, SE JÁ NÃO EXISTIR, SEJA: FISICA, JURIDICA, CONJUNTA
     *
     * @param req
     * @return
     */
    @PostMapping("/createAccount")
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest req){
        // Chamo o serviço que retorna null se não conseguir
        CreateAccountResponse resp = this.bankService.createAccount(req);
        // Monto a resposta
        if (resp == null){// Se já tiver usuário cadastrado
            resp = new CreateAccountResponse(null, null, null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }



    /*=================================== =================================== =================================== ==========================
     =================================== ENDPOINTS ACESSADOS PELO CLIENTE DE MANEIRA PRIVADA ===============================================
     =================================== =================================== =================================== ===========================*/
    /** OBTEM TODAS AS CONTAS EM QUE O USUARIO (O CPF) TEM ACESSO (EM TODOS OS BANCOS) JÁ COM O SALDO SENDO APRESENTADO.
     * Se um dos bancos estiver fora do ar, ele simplesmente não vai contribuir com a lista de contas que o usuário pode usar.
     *
     * @param cpf
     * @return
     */
    @GetMapping("/accounts/{cpf}")
    public ResponseEntity<AccountsResponse> getAccounts(@PathVariable("cpf") String cpf){
        AccountsResponse resp;

        resp = this.bankService.getAccounts(cpf);

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }


    /** Recebe a requisição de iniciar uma transação (ASYNC) plana em geral. Ao receber a solicitação e verificar se a mesma
     * está em conformidade, irá colocar em uma fila para processamento assincrono (liberando o canal para receber uma nova transação)
     *
     * @param req
     * @return
     * @throws InterruptedException
     */
    @PostMapping("/openTransaction")
    public ResponseEntity<CreateTransactionResponse> openTransaction(@RequestBody CreateTransactionRequest req) throws InterruptedException {
        CreateTransactionResponse res;
        // Registro a transferência e coloco em uma tarefa assincrona.
        res = this.bankService.openTransaction(req);

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }



    /** Obtem determinada transação, localmente, com base no id especiificado.
     * (UTILIZADO PARA FINS DE TESTE E DEBUG DA APLICAÇÃO APENAS)
     *
     * @param tid -> Id global da transação
     * @return
     */
    @GetMapping("transaction/{tid}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable("tid") String tid){
        Transaction resp;

        resp = this.bankService.getTransactionByTid(tid);

        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }
}
//openSubTransaction