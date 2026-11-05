package org.example.projetofinalfruki.domain.exception;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(String idCliente) {
        super("Cliente com ID '" + idCliente + "' não foi encontrado na base de dados. Verifique o cadastro.");
    }
}