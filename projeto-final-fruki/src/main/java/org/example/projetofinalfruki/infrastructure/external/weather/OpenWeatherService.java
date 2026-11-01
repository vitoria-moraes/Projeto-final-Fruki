package org.example.projetofinalfruki.infrastructure.external.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class OpenWeatherService {

    @Value("${api.weather.key:sua_chave_aqui}")
    private String apiKey;

    @Value("${api.weather.url:https://api.openweathermap.org/data/2.5}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public double obterTemperaturaAtual(String cidade) {
        try {
            // UriComponentsBuilder trata acentos e espaços automaticamente (ex: Capão da Canoa -> Cap%C3%A3o)
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl + "/weather")
                    .queryParam("q", cidade + ",BR")
                    .queryParam("units", "metric")
                    .queryParam("appid", apiKey)
                    .toUriString();

            Map<String, Object> resposta = restTemplate.getForObject(url, Map.class);

            if (resposta != null && resposta.containsKey("main")) {
                Map<String, Object> main = (Map<String, Object>) resposta.get("main");
                if (main.containsKey("temp")) {
                    return Double.parseDouble(main.get("temp").toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter clima para " + cidade + ": " + e.getMessage());
        }
        return 25.0; // Fallback seguro
    }
}