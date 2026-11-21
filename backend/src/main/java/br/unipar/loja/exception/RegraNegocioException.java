package br.unipar.loja.exception;

import org.springframework.http.HttpStatus;

public class RegraNegocioException extends RuntimeException {
    private final HttpStatus status;
    public RegraNegocioException(HttpStatus status, String mensagem) { super(mensagem); this.status = status; }
    public HttpStatus getStatus() { return status; }
    public static RegraNegocioException naoEncontrado(String recurso) {
        return new RegraNegocioException(HttpStatus.NOT_FOUND, recurso + " não encontrado.");
    }
    public static RegraNegocioException conflito(String mensagem) {
        return new RegraNegocioException(HttpStatus.CONFLICT, mensagem);
    }
    public static RegraNegocioException invalido(String mensagem) {
        return new RegraNegocioException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
