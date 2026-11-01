package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeriadoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BRASIL_API_URL = "https://brasilapi.com.br/api/feriados/v1/";

    /**
     * Busca feriados e eventos. Se anoEspecifico for nulo/zero, traz a lista combinada de 2026 e 2027!
     */
    public List<FeriadoDTO> buscarFeriadosCompletos(Integer anoEspecifico, String cidadeFiltro) {
        List<FeriadoDTO> todosEventos = new ArrayList<>();

        if (anoEspecifico != null && anoEspecifico > 0) {
            carregarEventosDoAno(anoEspecifico, todosEventos);
        } else {
            // Se não especificou o ano, carrega a agenda completa de 2026 E 2027!
            carregarEventosDoAno(2026, todosEventos);
            carregarEventosDoAno(2027, todosEventos);
        }

        // Filtra por cidade se especificado
        if (cidadeFiltro != null && !cidadeFiltro.isBlank()) {
            return todosEventos.stream()
                    .filter(f -> f.cidade().equalsIgnoreCase("Todas") || f.cidade().equalsIgnoreCase(cidadeFiltro))
                    .collect(Collectors.toList());
        }

        return todosEventos;
    }

    private void carregarEventosDoAno(int ano, List<FeriadoDTO> lista) {
        // 1. Feriados Nacionais via BrasilAPI
        try {
            FeriadoDTO[] nacionais = restTemplate.getForObject(BRASIL_API_URL + ano, FeriadoDTO[].class);
            if (nacionais != null) {
                for (FeriadoDTO f : nacionais) {
                    lista.add(new FeriadoDTO(f.date(), f.name(), "NATIONAL", "BR", "Todas"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar BrasilAPI para " + ano + ": " + e.getMessage());
        }

        // 2. Feriados Estaduais RS e SC
        lista.add(new FeriadoDTO(LocalDate.of(ano, 9, 20), "Revolução Farroupilha (Dia do Gaúcho)", "STATE", "RS", "Todas"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 8, 11), "Dia de Santa Catarina", "STATE", "SC", "Todas"));

        // 3. Feriados Municipais Principais
        lista.add(new FeriadoDTO(LocalDate.of(ano, 2, 2), "Nossa Senhora dos Navegantes", "MUNICIPAL", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 3, 26), "Aniversário de Porto Alegre", "MUNICIPAL", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 26), "Aniversário de Lajeado (Matriz Fruki)", "MUNICIPAL", "RS", "Lajeado"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 7, 25), "São Leopoldo Fest / Dia do Colono", "MUNICIPAL", "RS", "São Leopoldo"));

        // 4. EVENTOS TECH, CULTURA POP E GRANDES FEIRAS 🚀🎮💡
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 20), "Semana Caldeira (Instituto Caldeira - Inovação & Tech)", "EVENTO_CORPORATIVO", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 5, 17), "Animextreme Edição Outono (FIERGS - Cultura Pop & Geek)", "EVENTO_GEEK", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 18), "Animextreme Edição Primavera (FIERGS - Cultura Pop & Geek)", "EVENTO_GEEK", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 4, 1), "South Summit Brazil (Cais Mauá)", "EVENTO_CORPORATIVO", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 8, 29), "Expointer (Parque Assis Brasil)", "EVENTO_AGRO", "RS", "Esteio"));

        // 5. PRAIAS, TURISMO E REVEILLON 🏖️🎆
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Abertura da Alta Temporada de Verão & Réveillon", "TURISMO_PRAIA", "RS", "Capão da Canoa"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Réveillon & Festa da Iemanjá", "TURISMO_PRAIA", "RS", "Tramandaí"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Réveillon nos Molhes & Festival Verão", "TURISMO_PRAIA", "RS", "Torres"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Réveillon Show na Orla", "TURISMO_PRAIA", "SC", "Balneário Camboriú"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Virada de Ano no Litoral Catarinense", "TURISMO_PRAIA", "SC", "Florianópolis"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 12, 31), "Super Réveillon à Beira-Mar (Pico de Vendas)", "TURISMO_PRAIA", "RS", "Capão da Canoa"));

        // 6. SERRA & FESTAS TÍPICAS REGIONAIS 🍇🍺🍓
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 24), "Abertura do Natal Luz de Gramado", "TURISMO_SERRA", "RS", "Gramado"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 30), "Sonho de Natal em Canela", "TURISMO_SERRA", "RS", "Canela"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 7), "Oktoberfest de Santa Cruz do Sul", "FESTA_TYPICA", "RS", "Santa Cruz do Sul"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 6), "Oktoberfest de Blumenau", "FESTA_TYPICA", "SC", "Blumenau"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 2, 18), "Festa Nacional da Uva", "FESTA_TYPICA", "RS", "Caxias do Sul"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 9, 8), "Festa Nacional do Morango", "FESTA_TYPICA", "RS", "Bom Princípio"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 5, 15), "Festa da Bergamota / Citros", "FESTA_TYPICA", "RS", "Montenegro"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 5, 28), "Fenadoce (Feira Nacional do Doce)", "FESTA_TYPICA", "RS", "Pelotas"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 7, 20), "São Leopoldo Fest", "FESTA_TYPICA", "RS", "São Leopoldo"));
    }

    public boolean isProximoDeFeriado(String cidade) {
        int anoAtual = LocalDate.now().getYear();
        LocalDate hoje = LocalDate.now();

        return buscarFeriadosCompletos(anoAtual, cidade).stream()
                .anyMatch(f -> !f.date().isBefore(hoje) && f.date().isBefore(hoje.plusDays(10)));
    }
}