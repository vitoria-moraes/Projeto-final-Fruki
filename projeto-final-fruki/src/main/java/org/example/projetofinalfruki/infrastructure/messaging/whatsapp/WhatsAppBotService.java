package org.example.projetofinalfruki.infrastructure.messaging.whatsapp;

import org.example.projetofinalfruki.domain.service.RecomendacaoPerfilService;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.TabelaPrecoEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.TabelaPrecoJpaRepository;
import org.example.projetofinalfruki.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fluxo do bot: gatilhos de texto ("catalogo", "comprar", "tabela de precos") funcionam a qualquer momento.
 * Se o cliente ainda não foi identificado, o bot pede o CNPJ e executa o gatilho guardado em seguida.
 * Sessões em memória: em produção trocar por Redis/tabela com TTL (o mapa cresce sem limite).
 */
@Service
public class WhatsAppBotService {

    private enum Intencao { CATALOGO, COMPRAR, TABELA, RECOMENDACAO }

    private static final String MENU = """
            📌 *Menu Pede Fruki*
            1️⃣ Fazer pedido
            2️⃣ Recomendações para o seu perfil
            3️⃣ Catálogo online
            4️⃣ Tabela de preços

            Atalhos: *catalogo*, *comprar*, *tabela de precos*""";

    private final ClienteJpaRepository clienteRepository;
    private final TabelaPrecoJpaRepository tabelaPrecoRepository;
    private final RecomendacaoPerfilService recomendacaoService;
    private final JwtService jwtService;
    private final String baseUrl;

    private final Map<String, EstadoSessao> sessoes = new ConcurrentHashMap<>();
    private final Map<String, ClienteEntity> clientes = new ConcurrentHashMap<>();
    private final Map<String, Intencao> pendentes = new ConcurrentHashMap<>();

    public WhatsAppBotService(ClienteJpaRepository clienteRepository,
                              TabelaPrecoJpaRepository tabelaPrecoRepository,
                              RecomendacaoPerfilService recomendacaoService,
                              JwtService jwtService,
                              @Value("${app.catalogo.base-url:http://localhost:8080}") String baseUrl) {
        this.clienteRepository = clienteRepository;
        this.tabelaPrecoRepository = tabelaPrecoRepository;
        this.recomendacaoService = recomendacaoService;
        this.jwtService = jwtService;
        this.baseUrl = baseUrl;
    }

    public String processarMensagemRecebida(String telefone, String mensagem) {
        if (telefone == null || telefone.isBlank() || mensagem == null) {
            return "Mensagem ou telefone inválido.";
        }
        String texto = normalizar(mensagem);
        if (texto.isEmpty()) return "Não entendi 🤔 Digite *menu* para ver as opções.";

        if (texto.equals("sair") || texto.equals("reiniciar")) {
            sessoes.remove(telefone);
            clientes.remove(telefone);
            pendentes.remove(telefone);
            return "Sessão encerrada. Envie *oi* para começar de novo.";
        }

        ClienteEntity cliente = clientes.get(telefone);

        // Ainda sem identificação: CNPJ primeiro, gatilho guardado para depois
        if (cliente == null) {
            if (sessoes.getOrDefault(telefone, EstadoSessao.INICIO) == EstadoSessao.AGUARDANDO_CNPJ) {
                return validarCnpj(telefone, texto);
            }
            Intencao intencao = detectarIntencao(texto);
            if (intencao != null) pendentes.put(telefone, intencao);
            sessoes.put(telefone, EstadoSessao.AGUARDANDO_CNPJ);
            return "🥤 *Bem-vindo ao Pede Fruki (atendimento B2B)!*\nPara continuar, envie o *CNPJ* do seu estabelecimento (só números):";
        }

        Intencao intencao = detectarIntencao(texto);
        if (intencao != null) return executar(telefone, cliente, intencao);

        return switch (texto) {
            case "1" -> executar(telefone, cliente, Intencao.COMPRAR);
            case "2" -> executar(telefone, cliente, Intencao.RECOMENDACAO);
            case "3" -> executar(telefone, cliente, Intencao.CATALOGO);
            case "4" -> executar(telefone, cliente, Intencao.TABELA);
            case "0", "menu", "oi", "ola" -> {
                sessoes.put(telefone, EstadoSessao.MENU_PRINCIPAL);
                yield MENU;
            }
            default -> "Não entendi 🤔 Digite *menu* para ver as opções.";
        };
    }

    private String validarCnpj(String telefone, String texto) {
        String cnpj = texto.replaceAll("\\D", "");
        if (cnpj.length() != 14) {
            return "❌ O CNPJ precisa ter 14 dígitos. Tente novamente (ou envie *sair*):";
        }
        Optional<ClienteEntity> achado = clienteRepository.findByCnpj(cnpj);
        if (achado.isEmpty()) {
            return "❌ *CNPJ não encontrado na base Fruki.* Confira e envie novamente:";
        }
        ClienteEntity cliente = achado.get();
        clientes.put(telefone, cliente);
        sessoes.put(telefone, EstadoSessao.MENU_PRINCIPAL);

        String saudacao = "✅ *" + cliente.getNomeFantasia() + "* identificado!\n\n";
        Intencao pendente = pendentes.remove(telefone);
        return saudacao + (pendente != null ? executar(telefone, cliente, pendente) : MENU);
    }

    private String executar(String telefone, ClienteEntity cliente, Intencao intencao) {
        return switch (intencao) {
            case CATALOGO -> {
                sessoes.put(telefone, EstadoSessao.MENU_PRINCIPAL);
                yield "🛒 *Seu catálogo B2B* (link válido por 5 dias):\n" + linkCatalogo(cliente)
                        + "\n\nDigite *menu* para voltar.";
            }
            case COMPRAR -> {
                sessoes.put(telefone, EstadoSessao.MONTANDO_CARRINHO);
                yield "📦 *Novo pedido*\nMonte o pedido com os preços da sua região aqui:\n" + linkCatalogo(cliente)
                        + "\n\nDigite *menu* para voltar.";
            }
            case TABELA -> tabelaDePrecos(cliente);
            case RECOMENDACAO -> recomendacaoService.formatarWhatsApp(recomendacaoService.recomendar(cliente));
        };
    }

    private String tabelaDePrecos(ClienteEntity cliente) {
        List<TabelaPrecoEntity> itens = tabelaPrecoRepository.findAll(PageRequest.of(0, 10)).getContent();
        StringBuilder sb = new StringBuilder("💲 *Tabela de preços (prévia)*\n");
        for (TabelaPrecoEntity t : itens) {
            sb.append("• ").append(t.getNomeProduto()).append(" — R$ ").append(formatar(t.getPrecoBase())).append("\n");
        }
        if (itens.isEmpty()) sb.append("Prévia indisponível no momento.\n");
        return sb.append("\nTabela completa no catálogo:\n").append(linkCatalogo(cliente)).toString();
    }

    private String linkCatalogo(ClienteEntity cliente) {
        String uf = cliente.getUf() != null ? cliente.getUf().toUpperCase(Locale.ROOT) : "RS";
        String cidade = cliente.getCidade() != null ? cliente.getCidade() : "Porto Alegre";
        // TODO: trocar "TABELA_" + uf pela consulta real à dim_tabela_preco do cliente
        String token = jwtService.gerarTokenCatalogo(String.valueOf(cliente.getCodCliente()), cliente.getNomeFantasia(),
                cidade + " - " + uf, "TABELA_" + uf, cliente.getCnpj());
        return baseUrl + "/catalogo?token=" + token;
    }

    private static Intencao detectarIntencao(String texto) {
        if (texto.contains("catalog")) return Intencao.CATALOGO;
        if (texto.contains("tabela") || texto.contains("preco")) return Intencao.TABELA;
        if (texto.contains("comprar") || texto.contains("pedido")) return Intencao.COMPRAR;
        if (texto.contains("recomenda")) return Intencao.RECOMENDACAO;
        return null;
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private static String formatar(BigDecimal v) {
        return v == null ? "—" : String.format(Locale.of("pt", "BR"), "%.2f", v);
    }
}
