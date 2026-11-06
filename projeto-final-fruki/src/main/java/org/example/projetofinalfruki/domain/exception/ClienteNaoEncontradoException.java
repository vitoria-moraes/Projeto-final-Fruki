package org.example.projetofinalfruki.domain.exception;

/** Cliente não encontrado na base transacional (Supabase). Vira HTTP 404. */
public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String identificador) {
        super("Cliente não encontrado: " + identificador);
    }
}
