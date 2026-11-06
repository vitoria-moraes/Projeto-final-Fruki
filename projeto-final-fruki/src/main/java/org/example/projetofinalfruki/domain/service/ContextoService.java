package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.EstacaoDTO;
import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.example.projetofinalfruki.domain.model.Estacao;
import org.example.projetofinalfruki.infrastructure.external.weather.OpenWeatherService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Junta clima, estação do ano e calendário em um único contexto,
 * usado tanto pelo recomendador quanto pelos endpoints e pelo bot.
 */
@Service
public class ContextoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final OpenWeatherService clima;
    private final FeriadoService feriados;

    public ContextoService(OpenWeatherService clima, FeriadoService feriados) {
        this.clima = clima;
        this.feriados = feriados;
    }

    public ContextoDTO obterContexto(String cidade, String uf) {
        return obterContexto(cidade, uf, LocalDate.now());
    }

    public ContextoDTO obterContexto(String cidade, String uf, LocalDate data) {
        String cidadeFinal = (cidade == null || cidade.isBlank()) ? "Porto Alegre" : cidade.trim();
        String ufFinal = (uf == null || uf.isBlank()) ? "RS" : uf.trim().toUpperCase(Locale.ROOT);
        LocalDate dataFinal = data == null ? LocalDate.now() : data;

        OpenWeatherService.ClimaInfo info = clima.obterClima(cidadeFinal);
        Estacao estacao = Estacao.de(dataFinal);
        List<FeriadoDTO> lista = feriados.buscarFeriadosCompletos(dataFinal.getYear(), cidadeFinal);
        boolean feriadoProximo = lista.stream()
                .anyMatch(f -> !f.date().isBefore(dataFinal) && f.date().isBefore(dataFinal.plusDays(10)));

        String descricao = String.format(PT_BR,
                "%s em %s (%s), %.1f °C (%s).%s",
                estacao.getDescricao(), cidadeFinal, ufFinal,
                info.temperatura(), info.descricao(),
                feriadoProximo ? " Há feriado/evento nos próximos 10 dias." : "");

        return new ContextoDTO(cidadeFinal, ufFinal, dataFinal, estacao,
                estacao.getDescricao(), info.temperatura(), info.fonte(),
                feriadoProximo, descricao, lista);
    }

    public EstacaoDTO estacao(LocalDate data) {
        LocalDate dataFinal = data == null ? LocalDate.now() : data;
        Estacao estacao = Estacao.de(dataFinal);
        return new EstacaoDTO(dataFinal, estacao,
                estacao.getDescricao() + " (Hemisfério Sul) em " + dataFinal.format(DATA));
    }

    public List<FeriadoDTO> feriados(Integer ano, String cidade) {
        return feriados.buscarFeriadosCompletos(ano, cidade);
    }

    public List<FeriadoDTO> proximosFeriados(String cidade, int dias) {
        return feriados.proximos(cidade, dias);
    }
}
