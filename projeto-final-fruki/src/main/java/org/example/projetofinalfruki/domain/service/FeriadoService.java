package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.FeriadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Feriados e eventos do Sul (RS/SC).
 * A BrasilAPI cobre os nacionais; estaduais, municipais e eventos regionais
 * entram por curadoria. Há cache de 12h e fallback para os nacionais fixos
 * caso a API esteja fora.
 */
@Service
public class FeriadoService {

    private static final Logger log = LoggerFactory.getLogger(FeriadoService.class);
    private static final String BRASIL_API_URL = "https://brasilapi.com.br/api/feriados/v1/";

    private final RestTemplate restTemplate = comTimeout();
    private final Map<String, CacheFeriados> cache = new ConcurrentHashMap<>();

    /** Evita que uma API externa lenta trave a recomendação. */
    private static RestTemplate comTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    public List<FeriadoDTO> buscarFeriadosCompletos(Integer anoEspecifico, String cidadeFiltro) {
        int ano = (anoEspecifico != null && anoEspecifico > 0) ? anoEspecifico : LocalDate.now().getYear();
        String chave = ano + "|" + (cidadeFiltro == null ? "" : cidadeFiltro.trim().toLowerCase());
        CacheFeriados emCache = cache.get(chave);
        if (emCache != null && emCache.expiraEm().isAfter(Instant.now())) {
            return filtrar(emCache.lista(), cidadeFiltro);
        }

        List<FeriadoDTO> todos = new ArrayList<>();
        if (anoEspecifico == null || anoEspecifico <= 0) {
            carregarEventosDoAno(ano, todos);
            carregarEventosDoAno(ano + 1, todos);
        } else {
            carregarEventosDoAno(ano, todos);
        }
        cache.put(chave, new CacheFeriados(List.copyOf(todos),
                Instant.now().plus(12, ChronoUnit.HOURS)));
        return filtrar(todos, cidadeFiltro);
    }

    private List<FeriadoDTO> filtrar(List<FeriadoDTO> lista, String cidadeFiltro) {
        if (cidadeFiltro == null || cidadeFiltro.isBlank()) {
            return lista;
        }
        return lista.stream()
                .filter(f -> f.cidade().equalsIgnoreCase("Todas") || f.cidade().equalsIgnoreCase(cidadeFiltro))
                .collect(Collectors.toList());
    }

    private void carregarEventosDoAno(int ano, List<FeriadoDTO> lista) {
        boolean apiOk = false;
        try {
            FeriadoDTO[] nacionais = restTemplate.getForObject(BRASIL_API_URL + ano, FeriadoDTO[].class);
            if (nacionais != null) {
                for (FeriadoDTO f : nacionais) {
                    lista.add(new FeriadoDTO(f.date(), f.name(), "NATIONAL", "BR", "Todas"));
                }
                apiOk = true;
            }
        } catch (Exception e) {
            log.warn("BrasilAPI indisponível para {} ({}). Usando feriados nacionais fixos.",
                    ano, e.getClass().getSimpleName());
        }
        if (!apiOk) {
            lista.addAll(feriadosNacionaisFixos(ano));
        }

        // Estaduais
        lista.add(new FeriadoDTO(LocalDate.of(ano, 9, 20), "Revolução Farroupilha (Dia do Gaúcho)", "STATE", "RS", "Todas"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 8, 11), "Dia de Santa Catarina", "STATE", "SC", "Todas"));

        // Municipais
        lista.add(new FeriadoDTO(LocalDate.of(ano, 2, 2), "Nossa Senhora dos Navegantes", "MUNICIPAL", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 3, 26), "Aniversário de Porto Alegre", "MUNICIPAL", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 26), "Aniversário de Lajeado (Matriz Fruki)", "MUNICIPAL", "RS", "Lajeado"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 7, 25), "São Leopoldo Fest / Dia do Colono", "MUNICIPAL", "RS", "São Leopoldo"));

        // Eventos regionais usados como sinal de demanda
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 20), "Semana Caldeira (Inovação & Tech)", "EVENTO_CORPORATIVO", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 5, 17), "Animextreme Edição Outono", "EVENTO_GEEK", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 18), "Animextreme Edição Primavera", "EVENTO_GEEK", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 4, 1), "South Summit Brazil", "EVENTO_CORPORATIVO", "RS", "Porto Alegre"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 8, 29), "Expointer", "EVENTO_AGRO", "RS", "Esteio"));

        // Praias / turismo
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Abertura da Alta Temporada de Verão", "TURISMO_PRAIA", "RS", "Capão da Canoa"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Réveillon & Festa da Iemanjá", "TURISMO_PRAIA", "RS", "Tramandaí"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 1, 1), "Réveillon nos Molhes", "TURISMO_PRAIA", "RS", "Torres"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 12, 31), "Super Réveillon à Beira-Mar (Pico de Vendas)", "TURISMO_PRAIA", "RS", "Capão da Canoa"));

        // Serra / festas típicas
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 24), "Abertura do Natal Luz de Gramado", "TURISMO_SERRA", "RS", "Gramado"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 30), "Sonho de Natal em Canela", "TURISMO_SERRA", "RS", "Canela"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 7), "Oktoberfest de Santa Cruz do Sul", "FESTA_TIPICA", "RS", "Santa Cruz do Sul"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 10, 6), "Oktoberfest de Blumenau", "FESTA_TIPICA", "SC", "Blumenau"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 2, 18), "Festa Nacional da Uva", "FESTA_TIPICA", "RS", "Caxias do Sul"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 9, 8), "Festa Nacional do Morango", "FESTA_TIPICA", "RS", "Bom Princípio"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 5, 15), "Festa da Bergamota / Citros", "FESTA_TIPICA", "RS", "Montenegro"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 5, 28), "Fenadoce", "FESTA_TIPICA", "RS", "Pelotas"));
        lista.add(new FeriadoDTO(LocalDate.of(ano, 7, 20), "São Leopoldo Fest", "FESTA_TIPICA", "RS", "São Leopoldo"));
    }

    private List<FeriadoDTO> feriadosNacionaisFixos(int ano) {
        return List.of(
                new FeriadoDTO(LocalDate.of(ano, 1, 1), "Confraternização Universal", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 4, 21), "Tiradentes", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 5, 1), "Dia do Trabalhador", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 9, 7), "Independência do Brasil", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 10, 12), "Nossa Senhora Aparecida", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 11, 2), "Finados", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 11, 15), "Proclamação da República", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 11, 20), "Dia da Consci\u00eancia Negra", "NATIONAL", "BR", "Todas"),
                new FeriadoDTO(LocalDate.of(ano, 12, 25), "Natal", "NATIONAL", "BR", "Todas")
        );
    }

    public boolean isProximoDeFeriado(String cidade) {
        return proximos(cidade, 10).stream()
                .anyMatch(f -> !f.date().isBefore(LocalDate.now()));
    }

    public List<FeriadoDTO> proximos(String cidade, int dias) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(Math.max(1, dias));
        return buscarFeriadosCompletos(hoje.getYear(), cidade).stream()
                .filter(f -> !f.date().isBefore(hoje) && f.date().isBefore(limite))
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .toList();
    }

    private record CacheFeriados(List<FeriadoDTO> lista, Instant expiraEm) {
    }
}
