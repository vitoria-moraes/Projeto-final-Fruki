package org.example.projetofinalfruki.infrastructure.messaging.whatsapp;

import org.example.projetofinalfruki.domain.service.MotorSazonalidadeService;
import org.example.projetofinalfruki.infrastructure.db.supabase.entity.ClienteEntity;
import org.example.projetofinalfruki.infrastructure.db.supabase.repository.ClienteJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

enum EstadoSessao {
    INICIO,
    AGUARDANDO_CNPJ,
    MENU_PRINCIPAL,
    MONTANDO_CARRINHO,
    PEDIDO_FINALIZADO
}

@Service
public class WhatsAppBotService {

    private final ClienteJpaRepository clienteRepository;
    private final MotorSazonalidadeService motorSazonalidadeService;

    private final Map<String, EstadoSessao> sessoesAtivas = new ConcurrentHashMap<>();
    private final Map<String, ClienteEntity> clientesAutenticados = new ConcurrentHashMap<>();

    public WhatsAppBotService(ClienteJpaRepository clienteRepository, MotorSazonalidadeService motorSazonalidadeService) {
        this.clienteRepository = clienteRepository;
        this.motorSazonalidadeService = motorSazonalidadeService;
    }

    public String processarMensagemRecebida(String telefone, String mensagemTexto) {
        if (telefone == null || mensagemTexto == null) {
            return "Mensagem ou telefone inválido.";
        }

        String textoFormatado = mensagemTexto.trim().toLowerCase();
        EstadoSessao estadoAtual = sessoesAtivas.getOrDefault(telefone, EstadoSessao.INICIO);

        switch (estadoAtual) {
            case INICIO:
                sessoesAtivas.put(telefone, EstadoSessao.AGUARDANDO_CNPJ);
                return "🥤 *Olá! Bem-vindo ao Pede Fruki (Atendimento B2B)!*\nPara iniciar, por favor digite o **CNPJ** do seu estabelecimento:";

            case AGUARDANDO_CNPJ:
                String cnpjLimpo = textoFormatado.replaceAll("\\D", "");
                Optional<ClienteEntity> clienteOpt = clienteRepository.findByCnpj(cnpjLimpo);

                if (clienteOpt.isPresent()) {
                    ClienteEntity cliente = clienteOpt.get();
                    clientesAutenticados.put(telefone, cliente);
                    sessoesAtivas.put(telefone, EstadoSessao.MENU_PRINCIPAL);

                    return "✅ *Cliente Identificado:* " + cliente.getNomeFantasia() + "\n\n" +
                            "Como posso te ajudar hoje?\n" +
                            "1️⃣ Fazer um novo pedido\n" +
                            "2️⃣ Ver recomendação do dia (Promoções & Clima)\n" +
                            "3️⃣ Consultar catálogo";
                } else {
                    return "❌ *CNPJ não encontrado na base Fruki.* Digite um CNPJ válido:";
                }

            case MENU_PRINCIPAL:
                if (textoFormatado.equals("1")) {
                    sessoesAtivas.put(telefone, EstadoSessao.MONTANDO_CARRINHO);
                    return "📦 *Iniciando Pedido:* Digite o produto e a quantidade desejada:";
                } else if (textoFormatado.equals("2")) {
                    ClienteEntity cliente = clientesAutenticados.get(telefone);
                    String cidade = "Porto Alegre";
                    if (cliente != null && cliente.getCidade() != null) {
                        cidade = cliente.getCidade();
                    }

                    String recomendacao = motorSazonalidadeService.gerarRecomendacaoParaCidade(cidade);
                    return recomendacao + "\n\nDigite *0* para voltar ao menu principal.";
                } else if (textoFormatado.equals("0")) {
                    return "📌 *Menu Principal:*\n1️⃣ Fazer um novo pedido\n2️⃣ Ver recomendação do dia\n3️⃣ Consultar catálogo";
                } else {
                    return "Opção inválida. Digite *1* para Pedidos, *2* para Recomendação do dia ou *0* para ver o menu.";
                }

            case MONTANDO_CARRINHO:
                sessoesAtivas.put(telefone, EstadoSessao.PEDIDO_FINALIZADO);
                return "🎉 *Pedido registrado com sucesso!* Código de acompanhamento: #FRK-" + (System.currentTimeMillis() % 10000);

            default:
                sessoesAtivas.put(telefone, EstadoSessao.INICIO);
                return "Sessão reiniciada. Envie qualquer mensagem para começar.";
        }
    }
}