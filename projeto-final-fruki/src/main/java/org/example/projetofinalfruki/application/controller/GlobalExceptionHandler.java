package org.example.projetofinalfruki.application.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.exception.ConflitoException;
import org.example.projetofinalfruki.domain.exception.DatabricksIndisponivelException;
import org.example.projetofinalfruki.domain.exception.ProdutoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tratamento único de erros no padrão ProblemDetail (RFC 7807).
 * Cobre: validação (400), cliente inexistente (404), conflito (409),
 * Databricks fora (503) e erro inesperado (500).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_ERROS = "https://pedefruki.local/erros/";

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ProblemDetail clienteNaoEncontrado(ClienteNaoEncontradoException e, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, "Cliente não encontrado", e.getMessage(),
                "cliente-nao-encontrado", request);
    }

    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ProblemDetail produtoNaoEncontrado(ProdutoNaoEncontradoException e, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, "Produto não encontrado", e.getMessage(),
                "produto-nao-encontrado", request);
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException e, HttpServletRequest request) {
        return problema(HttpStatus.CONFLICT, "Conflito de dados", e.getMessage(),
                "conflito", request);
    }

    @ExceptionHandler(DatabricksIndisponivelException.class)
    public ProblemDetail databricksIndisponivel(DatabricksIndisponivelException e, HttpServletRequest request) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "Camada analítica indisponível", e.getMessage(),
                "databricks-indisponivel", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail rotaNaoEncontrada(NoResourceFoundException e, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                "A rota " + request.getRequestURI() + " não existe.", "nao-encontrado", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail metodoNaoPermitido(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return problema(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "O método " + e.getMethod() + " não é suportado nesta rota.", "metodo-nao-permitido", request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail bancoIndisponivel(DataAccessException e, HttpServletRequest request) {
        log.error("Falha de acesso ao banco transacional em {}", request.getRequestURI(), e);
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível",
                "Não foi possível acessar o banco transacional agora.", "banco-indisponivel", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail dadosInvalidos(IllegalArgumentException e, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage(),
                "requisicao-invalida", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<Map<String, String>> erros = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(erro -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("campo", erro.getField());
            item.put("mensagem", erro.getDefaultMessage());
            erros.add(item);
        });
        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Um ou mais campos estão inválidos.", "requisicao-invalida", request);
        pd.setProperty("erros", erros);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail constraint(ConstraintViolationException e, HttpServletRequest request) {
        List<Map<String, String>> erros = new ArrayList<>();
        e.getConstraintViolations().forEach(v -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("campo", v.getPropertyPath().toString());
            item.put("mensagem", v.getMessage());
            erros.add(item);
        });
        ProblemDetail pd = problema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Parâmetro inválido.", "requisicao-invalida", request);
        pd.setProperty("erros", erros);
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "Valor inválido para '" + e.getName() + "': " + e.getValue(),
                "requisicao-invalida", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpoIlegivel(HttpMessageNotReadableException e, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "Corpo inválido",
                "O corpo da requisição não é um JSON válido para este endpoint.",
                "requisicao-invalida", request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail erroInesperado(Exception e, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                "erro-interno", request);
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe,
                                   String tipo, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalhe);
        pd.setTitle(titulo);
        pd.setType(URI.create(BASE_ERROS + tipo));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }
}
