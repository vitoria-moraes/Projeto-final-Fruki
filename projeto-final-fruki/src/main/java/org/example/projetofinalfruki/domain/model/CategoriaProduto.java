package org.example.projetofinalfruki.domain.model;

import org.example.projetofinalfruki.domain.util.Texto;

/**
 * Categoria comercial inferida a partir do nome do produto cadastrado em tabela_preco.
 * O schema atual não tem coluna de categoria; por isso usamos palavras-chave.
 * O "margemProxy" é um substituto da margem real (que ainda não existe na base):
 * serve apenas para priorizar produtos de maior valor percebido no mix.
 */
public enum CategoriaProduto {

    REFRIGERANTES("Refrigerantes", 0.60),
    ENERGETICOS("Energéticos", 0.90),
    AGUAS("Águas", 0.50),
    CERVEJAS("Cervejas", 0.80),
    REFRESCOS("Refrescos", 0.55),
    SUCOS("Sucos", 0.65),
    TONICAS("Tônicas", 0.50),
    OUTROS("Outros", 0.40);

    private final String descricao;
    private final double margemProxy;

    CategoriaProduto(String descricao, double margemProxy) {
        this.descricao = descricao;
        this.margemProxy = margemProxy;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getMargemProxy() {
        return margemProxy;
    }

    /** Classifica pelo nome do produto. Nunca devolve null. */
    public static CategoriaProduto fromNome(String nome) {
        String n = Texto.normalizar(nome);
        if (n.isEmpty()) {
            return OUTROS;
        }
        if (contem(n, "agua")) {
            return AGUAS;
        }
        if (contem(n, "elev") || contem(n, "energet") || contem(n, "energy")) {
            return ENERGETICOS;
        }
        if (contem(n, "cerveja") || contem(n, "bellavista") || contem(n, "lager")
                || contem(n, "pilsen") || contem(n, "ipa")) {
            return CERVEJAS;
        }
        if (contem(n, "tonica")) {
            return TONICAS;
        }
        if (contem(n, "frukito") || contem(n, "refresco")) {
            return REFRESCOS;
        }
        if (contem(n, "suco") || contem(n, "comtem") || contem(n, "com tem")) {
            return SUCOS;
        }
        if (contem(n, "guarana") || contem(n, "cola") || contem(n, "laranja")
                || contem(n, "limao") || contem(n, "uva") || contem(n, "bergamota")
                || contem(n, "abacaxi") || contem(n, "morango") || contem(n, "frutas")
                || contem(n, "pessego") || contem(n, "citrus") || contem(n, "limonade")) {
            return REFRIGERANTES;
        }
        return OUTROS;
    }

    private static boolean contem(String texto, String termo) {
        return texto.contains(termo);
    }
}
