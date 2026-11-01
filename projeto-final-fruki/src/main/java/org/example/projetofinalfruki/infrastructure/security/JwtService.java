package org.example.projetofinalfruki.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    // Chave secreta robusta (em produção, o ideal é injetar via application.yml / variável de ambiente)
    private static final String SECRET_KEY_STRING = "fruking-secret-key-security-jwt-b2b-token-2027-super-safe";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    // Validade do token: 2 horas (tempo suficiente para o lojista montar o pedido no catálogo)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 2;

    /**
     * Gera o token JWT contendo os dados do cliente identificado no WhatsApp.
     */
    public String gerarTokenCatalogo(String clienteId, String nomeCliente, String regiao, String tabelaPreco) {
        return Jwts.builder()
                .subject(clienteId)
                .claim("nome", nomeCliente)
                .claim("regiao", regiao)
                .claim("tabelaPreco", tabelaPreco)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Valida o token e extrai todas as informações (claims) guardadas nele.
     */
    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica se o token é válido e não expirou.
     */
    public boolean validarToken(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("Token JWT inválido ou expirado: " + e.getMessage());
            return false;
        }
    }
}