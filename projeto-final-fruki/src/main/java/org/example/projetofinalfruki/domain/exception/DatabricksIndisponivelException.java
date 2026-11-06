package org.example.projetofinalfruki.domain.exception;

/**
 * Falha de comunicação/consulta na camada analítica (Databricks). Vira HTTP 503.
 * Não use para erro de SQL/coluna: nesse caso o log deve mostrar a causa real.
 */
public class DatabricksIndisponivelException extends RuntimeException {
    public DatabricksIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
