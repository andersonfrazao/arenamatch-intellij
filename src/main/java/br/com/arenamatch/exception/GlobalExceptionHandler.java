package br.com.arenamatch.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleRecursoNaoEncontrado(NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason();
        if (mensagem == null || mensagem.trim().isEmpty()) {
            mensagem = "Erro na requisicao.";
        }

        return ResponseEntity.status(ex.getStatusCode()).body(mensagem);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleConcorrencia(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(409)
                .body("Os dados foram alterados em outra sessao. Recarregue antes de salvar novamente.");
    }

    // Erros de regra de negocio.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRegraNegocio(RuntimeException ex) {
        System.err.println("[REGRA DE NEGOCIO BARRADA] " + ex.getMessage());

        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Erros inesperados de codigo.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleErrosInesperados(Exception ex) {
        System.err.println("[ERRO CRITICO INESPERADO]");
        ex.printStackTrace();

        return ResponseEntity.internalServerError()
                .body("Ocorreu um erro interno no servidor. Nossa equipe ja foi notificada.");
    }
}
