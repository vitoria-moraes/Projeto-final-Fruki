package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.config.RecomendacaoProperties;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.exception.DatabricksIndisponivelException;
import org.example.projetofinalfruki.domain.model.PerfilRegional;
import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.RecomendacaoRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Orquestra a consulta: Gold (Databricks) + cliente (Supabase) + contexto + ranking V2.
 * O controller não conhece repositório nem regra de negócio.
 */
@Service
public class RecomendacaoService {

    private static final Logger log = LoggerFactory.getLogger(RecomendacaoService.class);

    private final RecomendacaoRepository recomendacaoRepository;
    private final ClienteJpaRepository clienteRepository;
    private final RecomendadorV2Service recomendador;
    private final ContextoService contextoService;
    private final RecomendacaoProperties properties;
    private final CatalogoService catalogo;
    private final MockPedeFrukiService mockService;

    @Autowired
    public RecomendacaoService(RecomendacaoRepository recomendacaoRepository,
                               ClienteJpaRepository clienteRepository,
                               RecomendadorV2Service recomendador,
                               ContextoService contextoService,
                               RecomendacaoProperties properties,
                               CatalogoService catalogo,
                               MockPedeFrukiService mockService) {
        this.recomendacaoRepository = recomendacaoRepository;
        this.clienteRepository = clienteRepository;
        this.recomendador = recomendador;
        this.contextoService = contextoService;
        this.properties = properties;
        this.catalogo = catalogo;
        this.mockService = mockService;
    }

    /** Construtor legado mantido para testes e clientes internos já existentes. */
    public RecomendacaoService(RecomendacaoRepository recomendacaoRepository,
                               ClienteJpaRepository clienteRepository,
                               RecomendadorV2Service recomendador,
                               ContextoService contextoService,
                               RecomendacaoProperties properties) {
        this(recomendacaoRepository, clienteRepository, recomendador, contextoService,
                properties, null, new MockPedeFrukiService());
    }

    public RecomendacaoResponseDTO recomendar(Long idCliente, Integer limite) {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("idCliente deve ser um número positivo.");
        }
        int limiteSeguro = limite == null
                ? properties.getLimitePadrao()
                : Math.max(1, Math.min(limite, 50));

        // O ranking Gold melhora a personalização; indisponibilidade usa cold start local.
        List<RecomendacaoB2B> historico;
        boolean fonteIndisponivel = false;
        try {
            historico = recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(idCliente);
        } catch (DatabricksIndisponivelException e) {
            log.warn("Databricks indisponível para cliente {}; usando recomendações do catálogo local.", idCliente);
            historico = List.of();
            fonteIndisponivel = true;
        }

        // O código do cliente no Supabase é BIGINT (Long), igual ao id_cliente da Gold.
        ClienteEntity cliente = null;
        try {
            cliente = clienteRepository.findById(idCliente).orElse(null);
        } catch (DataAccessException e) {
            log.warn("Supabase indisponível ao buscar cliente {}; usando resposta demonstrativa.", idCliente);
            fonteIndisponivel = true;
        }
        if ((historico == null || historico.isEmpty()) && cliente == null && !fonteIndisponivel) {
            throw new ClienteNaoEncontradoException(String.valueOf(idCliente));
        }

        ContextoDTO contexto;
        try {
            contexto = contextoService.obterContexto(
                    cliente != null ? cliente.getCidade() : null,
                    cliente != null ? cliente.getUf() : null);
        } catch (RuntimeException e) {
            log.warn("Contexto externo indisponível; usando contexto demonstrativo.");
            contexto = mockService.contexto(cliente != null ? cliente.getCidade() : null,
                    cliente != null ? cliente.getUf() : null);
            fonteIndisponivel = true;
        }

        List<ItemRecomendadoDTO> itens;
        try {
            itens = recomendador.recomendar(historico, cliente, contexto, limiteSeguro);
        } catch (RuntimeException e) {
            log.warn("Ranking indisponível para cliente {}; usando recomendações demonstrativas.", idCliente);
            itens = List.of();
            fonteIndisponivel = true;
        }
        if (fonteIndisponivel || itens.size() < 4) {
            return mockService.recomendacoes(idCliente, cliente, itens, limiteSeguro, catalogo);
        }
        PerfilRegional perfil = PerfilRegional.fromCidade(
                cliente != null ? cliente.getCidade() : null,
                cliente != null ? cliente.getUf() : null);

        return new RecomendacaoResponseDTO(
                idCliente,
                cliente != null ? cliente.getNomeFantasia() : null,
                cliente != null ? cliente.getCidade() : null,
                cliente != null ? cliente.getUf() : null,
                perfil.getDescricao(),
                contexto,
                itens);
    }
}
