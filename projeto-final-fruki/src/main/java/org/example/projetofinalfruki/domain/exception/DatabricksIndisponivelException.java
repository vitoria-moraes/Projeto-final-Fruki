package org.example.projetofinalfruki.domain.exception;

public class DatabricksIndisponivelException extends RuntimeException {
    public DatabricksIndisponivelException(String message) {
        super("Falha de comunicação com o motor analítico (Databricks). Fallback ativado. Detalhe: " + message);
    }
}