package org.example.projetofinalfruki.domain.exception;

/** Produto não encontrado na tabela de preços. Vira HTTP 404. */
public class ProdutoNaoEncontradoException extends RuntimeException {
    public ProdutoNaoEncontradoException(String codItem) {
        super("Produto não encontrado: " + codItem);
    }
}
