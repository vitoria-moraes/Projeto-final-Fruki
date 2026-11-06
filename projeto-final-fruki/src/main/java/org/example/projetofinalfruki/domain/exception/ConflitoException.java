package org.example.projetofinalfruki.domain.exception;

/** Conflito de unicidade (ex.: CNPJ já cadastrado). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
