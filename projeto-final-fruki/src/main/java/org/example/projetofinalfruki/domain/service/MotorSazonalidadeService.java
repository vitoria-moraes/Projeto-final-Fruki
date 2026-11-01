package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.infrastructure.external.weather.OpenWeatherService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MotorSazonalidadeService {

    private final OpenWeatherService weatherService;
    private final FeriadoService feriadoService;

    public MotorSazonalidadeService(OpenWeatherService weatherService, FeriadoService feriadoService) {
        this.weatherService = weatherService;
        this.feriadoService = feriadoService;
    }

    public List<FeriadoDTO> listarFeriadosDoAno(String cidade) {
        int anoAtual = LocalDate.now().getYear();
        return feriadoService.buscarFeriadosCompletos(anoAtual, cidade);
    }

    public String gerarRecomendacaoParaCidade(String cidade) {
        String cidadeFinal = (cidade != null && !cidade.isBlank()) ? cidade : "Porto Alegre";

        // 1. Busca a temperatura real do OpenWeather
        double temperaturaAtual = weatherService.obterTemperaturaAtual(cidadeFinal);

        // 2. Verifica se há feriado próximo
        boolean proximoFeriado = feriadoService.isProximoDeFeriado(cidadeFinal);

        LocalDate hoje = LocalDate.now();
        int mes = hoje.getMonthValue();

        // Se houver Feriado Próximo + Temperatura Real do OpenWeather
        if (proximoFeriado) {
            return String.format(
                    "🎉 *Feriado a chegar nos próximos dias em %s (Temperatura atual: %.1f°C)!*\nAntecipe o pedido da sua loja! Recomenda-se reforço de estoque em **Packs de Fruki Guaraná 2L**, **Água Mineral Pedra** e **Cervejas**.",
                    cidadeFinal, temperaturaAtual
            );
        }

        // Se o dia estiver quente (> 28°C)
        if (temperaturaAtual > 28.0) {
            return String.format(
                    "🔥 *Dia quente em %s (%.1f°C)!*\nQue tal reforçar o estoque com **Água Mineral Pedra** gelada e **Fruki Guaraná 2L**? Alta saída garantida!",
                    cidadeFinal, temperaturaAtual
            );
        }

        // Mês Farroupilha
        if (mes == 9) {
            return "🐎 *É tempo de Revolução Farroupilha no RS!*\nO churrasco dos seus clientes vai pedir os sabores tradicionais da **Fruki** e **H2OH! / Citrus**. Garanta o seu mix!";
        }

        // Recomendação padrão com clima
        return String.format(
                "💡 *Dica do Dia (%s - %.1f°C):*\nMantenha o mix equilibrado entre **Fruki Cola** e os sabores frutas para garantir giro rápido na prateleira.",
                cidadeFinal, temperaturaAtual
        );
    }
}