package org.example.projetofinalfruki.domain.model;

import org.example.projetofinalfruki.domain.util.Texto;

import java.util.List;

/**
 * Perfil regional inspirado nos quatro lojistas descritos pelo grupo.
 * A aderência (0..1) indica o quanto cada categoria combina com o perfil.
 */
public enum PerfilRegional {

    MERCADO_CENTRAL("Mercado Central"),
    SUPER_LITORAL("Super Litoral"),
    CONVENIENCIA_SERRA("Conveniência Serra"),
    DISTRIBUIDORA_VALE("Distribuidora Vale");

    private final String descricao;

    PerfilRegional(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static PerfilRegional fromCidade(String cidade, String uf) {
        String c = Texto.normalizar(cidade);
        String estado = uf == null ? "" : uf.trim().toUpperCase();

        if (contemAlgum(c, List.of("capao", "tramandai", "torres", "balneario", "cassino",
                "cidreira", "xangri", "mostardas", "palmares", "florianopolis", "itapema",
                "bombinhas", "camboriu", "garopaba", "laguna", "imbituba"))) {
            return SUPER_LITORAL;
        }
        if (contemAlgum(c, List.of("gramado", "canela", "caxias", "bento", "garibaldi",
                "nova petropolis", "sao francisco de paula", "vacaria", "farroupilha"))) {
            return CONVENIENCIA_SERRA;
        }
        if (contemAlgum(c, List.of("lajeado", "estrela", "teutonia", "encantado", "taquari",
                "venancio", "arroio do meio", "bom retiro", "progresso"))) {
            return DISTRIBUIDORA_VALE;
        }
        if ("SC".equals(estado)) {
            return MERCADO_CENTRAL;
        }
        return MERCADO_CENTRAL;
    }

    public double aderencia(CategoriaProduto categoria) {
        return switch (this) {
            case MERCADO_CENTRAL -> switch (categoria) {
                case REFRIGERANTES -> 0.85;
                case SUCOS -> 0.75;
                case AGUAS -> 0.65;
                case ENERGETICOS -> 0.60;
                case CERVEJAS -> 0.55;
                case REFRESCOS -> 0.65;
                case TONICAS -> 0.55;
                case OUTROS -> 0.50;
            };
            case SUPER_LITORAL -> switch (categoria) {
                case AGUAS -> 0.95;
                case ENERGETICOS -> 0.80;
                case REFRIGERANTES -> 0.75;
                case CERVEJAS -> 0.75;
                case REFRESCOS -> 0.60;
                case SUCOS -> 0.55;
                case TONICAS -> 0.60;
                case OUTROS -> 0.50;
            };
            case CONVENIENCIA_SERRA -> switch (categoria) {
                case CERVEJAS -> 0.90;
                case SUCOS -> 0.80;
                case REFRIGERANTES -> 0.70;
                case AGUAS -> 0.60;
                case ENERGETICOS -> 0.65;
                case REFRESCOS -> 0.60;
                case TONICAS -> 0.55;
                case OUTROS -> 0.50;
            };
            case DISTRIBUIDORA_VALE -> switch (categoria) {
                case REFRIGERANTES -> 0.80;
                case SUCOS -> 0.75;
                case AGUAS -> 0.70;
                case CERVEJAS -> 0.65;
                case ENERGETICOS -> 0.60;
                case REFRESCOS -> 0.70;
                case TONICAS -> 0.50;
                case OUTROS -> 0.50;
            };
        };
    }

    private static boolean contemAlgum(String texto, List<String> termos) {
        return termos.stream().anyMatch(texto::contains);
    }
}
