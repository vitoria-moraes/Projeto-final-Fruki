package org.example.projetofinalfruki.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** HS256 exige no mínimo 256 bits (32 bytes). */
    private static final int TAMANHO_MINIMO_BYTES = 32;

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${jwt.secret:}") String secret,
                      @Value("${jwt.expiration-minutes:7200}") long expirationMinutes) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret ausente ou curto demais: defina a variável de ambiente JWT_SECRET com pelo menos 32 caracteres.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMinutes * 60_000L;
    }

    /**
     * Gera o token JWT contendo os dados do cliente identificado no WhatsApp.
     */
    public String gerarTokenCatalogo(String clienteId, String nomeCliente, String regiao, String tabelaPreco) {
        return gerarTokenCatalogo(clienteId, nomeCliente, regiao, tabelaPreco, null);
    }

    /** Variante com CNPJ, usada para o catálogo conseguir fechar pedido. */
    public String gerarTokenCatalogo(String clienteId, String nomeCliente, String regiao,
                                     String tabelaPreco, String cnpj) {
        long agora = System.currentTimeMillis();
        var builder = Jwts.builder()
                .subject(clienteId)
                .claim("nome", nomeCliente)
                .claim("regiao", regiao)
                .claim("tabelaPreco", tabelaPreco);
        if (cnpj != null && !cnpj.isBlank()) {
            builder.claim("cnpj", cnpj);
        }
        return builder
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + expirationMillis))
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
            // só o tipo da exceção no log: a mensagem pode ecoar trechos do token
            log.warn("Token JWT rejeitado: {}", e.getClass().getSimpleName());
            return false;
        }
    }
}