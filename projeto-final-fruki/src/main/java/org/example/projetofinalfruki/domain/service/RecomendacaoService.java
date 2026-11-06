package org.example.projetofinalfruki.domain.service;

import org.example.projetofinalfruki.application.dtos.ContextoDTO;
import org.example.projetofinalfruki.application.dtos.ItemRecomendadoDTO;
import org.example.projetofinalfruki.application.dtos.RecomendacaoResponseDTO;
import org.example.projetofinalfruki.config.RecomendacaoProperties;
import org.example.projetofinalfruki.domain.exception.ClienteNaoEncontradoException;
import org.example.projetofinalfruki.domain.model.PerfilRegional;
import org.example.projetofinalfruki.domain.model.RecomendacaoB2B;
import org.example.projetofinalfruki.infrastructure.db.supabase.databricks.RecomendacaoRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orquestra a consulta: Gold (Databricks) + cliente (Supabase) + contexto + ranking V2.
 * O controller não conhece repositório nem regra de negócio.
 */
@Service
public class RecomendacaoService {

    private final RecomendacaoRepository recomendacaoRepository;
    private final ClienteJpaRepository clienteRepository;
    private final RecomendadorV2Service recomendador;
    private final ContextoService contextoService;
    private final RecomendacaoProperties properties;

    public RecomendacaoService(RecomendacaoRepository recomendacaoRepository,
                               ClienteJpaRepository clienteRepository,
                               RecomendadorV2Service recomendador,
                               ContextoService contextoService,
                               RecomendacaoProperties properties) {
        this.recomendacaoRepository = recomendacaoRepository;
        this.clienteRepository = clienteRepository;
        this.recomendador = recomendador;
        this.contextoService = contextoService;
        this.properties = properties;
    }

    public RecomendacaoResponseDTO recomendar(Long idCliente, Integer limite) {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("idCliente deve ser um número positivo.");
        }
        int limiteSeguro = limite == null
                ? properties.getLimitePadrao()
                : Math.max(1, Math.min(limite, 50));

        // A Gold é a fonte principal; se o Databricks cair, sobe DatabricksIndisponivelException (503).
        List<RecomendacaoB2B> historico =
                recomendacaoRepository.findByIdClienteOrderByScoreRecomendacaoDesc(idCliente);

        // O código do cliente no Supabase é String; tentamos o mesmo número da Gold.
        ClienteEntity cliente = clienteRepository.findById(String.valueOf(idCliente)).orElse(null);
        if ((historico == null || historico.isEmpty()) && cliente == null) {
            throw new ClienteNaoEncontradoException(String.valueOf(idCliente));
        }

        ContextoDTO contexto = contextoService.obterContexto(
                cliente != null ? cliente.getCidade() : null,
                cliente != null ? cliente.getUf() : null);

        List<ItemRecomendadoDTO> itens = recomendador.recomendar(historico, cliente, contexto, limiteSeguro);
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
