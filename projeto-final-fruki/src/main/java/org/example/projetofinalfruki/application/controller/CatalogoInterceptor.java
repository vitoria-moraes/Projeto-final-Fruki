package org.example.projetofinalfruki.application.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CatalogoInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    public CatalogoInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("token");

        if (token != null && jwtService.validarToken(token)) {
            Claims claims = jwtService.extrairClaims(token);

            // Injeta os dados do cliente na requisição para a página web usar
            request.setAttribute("clienteId", claims.getSubject());
            request.setAttribute("nomeCliente", claims.get("nome", String.class));
            request.setAttribute("regiaoCliente", claims.get("regiao", String.class));
            request.setAttribute("tabelaPrecoCliente", claims.get("tabelaPreco", String.class));
            request.setAttribute("cnpjCliente", claims.get("cnpj", String.class));

            return true; // Deixa a requisição seguir para o Catálogo
        }

        // Se o token não existir ou for inválido, bloqueia o acesso e redireciona ou dá erro 401
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acesso negado: Token JWT ausente ou inválido. Identifique-se via WhatsApp.");
        return false;
    }
}