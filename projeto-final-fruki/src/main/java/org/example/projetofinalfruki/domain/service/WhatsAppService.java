package org.example.projetofinalfruki.domain.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Configura aqui as tuas credenciais da Evolution API (podes mover depois para o application.yml)
    private final String evolutionApiUrl = "http://localhost:8080"; // URL da tua Evolution API
    private final String instanceName = "sua-instancia";             // Nome da instância no Evolution
    private final String apiKey = "sua-api-key-aqui";               // Global API Key da Evolution

    public void enviarMensagem(String numeroDestino, String textoResposta) {
        String url = evolutionApiUrl + "/message/sendText/" + instanceName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("number", numeroDestino);
        body.put("text", textoResposta);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("📤 Resposta enviada com sucesso para o WhatsApp: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar mensagem para a Evolution API: " + e.getMessage());
        }
    }
}