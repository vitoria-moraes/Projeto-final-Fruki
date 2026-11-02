package org.example.projetofinalfruki.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/** Envio via Evolution API. Configuração por propriedades/variáveis de ambiente, nunca no código. */
@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final RestTemplate restTemplate;
    private final String url;
    private final String instance;
    private final String apiKey;

    public WhatsAppService(RestTemplateBuilder builder,
                           @Value("${evolution.url:http://localhost:8081}") String url,
                           @Value("${evolution.instance:}") String instance,
                           @Value("${evolution.api-key:}") String apiKey) {
        this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(3)).setReadTimeout(Duration.ofSeconds(5)).build();
        this.url = url;
        this.instance = instance;
        this.apiKey = apiKey;
    }

    public boolean enviarMensagem(String numeroDestino, String texto) {
        if (apiKey.isBlank() || instance.isBlank()) {
            log.warn("Evolution API não configurada (evolution.instance / evolution.api-key): resposta não enviada.");
            return false;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);
        try {
            restTemplate.postForEntity(url + "/message/sendText/" + instance,
                    new HttpEntity<>(Map.of("number", numeroDestino, "text", texto), headers), String.class);
            return true;
        } catch (Exception e) {
            log.error("Falha ao enviar para a Evolution API: {}", e.getMessage());
            return false;
        }
    }
}
