package org.example.projetofinalfruki.domain.model;

import java.time.LocalDate;

/**
 * Estações do ano no Hemisfério Sul (datas astronômicas aproximadas).
 * Usadas como sinal de sazonalidade no ranking.
 */
public enum Estacao {

    VERAO("Verão"),
    OUTONO("Outono"),
    INVERNO("Inverno"),
    PRIMAVERA("Primavera");

    private final String descricao;

    Estacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Estacao de(LocalDate data) {
        int mes = data.getMonthValue();
        int dia = data.getDayOfMonth();

        if ((mes == 12 && dia >= 21) || mes == 1 || mes == 2 || (mes == 3 && dia < 20)) {
            return VERAO;
        }
        if ((mes == 3 && dia >= 20) || mes == 4 || mes == 5 || (mes == 6 && dia < 20)) {
            return OUTONO;
        }
        if ((mes == 6 && dia >= 20) || mes == 7 || mes == 8 || (mes == 9 && dia < 22)) {
            return INVERNO;
        }
        return PRIMAVERA;
    }

    public double fator(CategoriaProduto categoria) {
        return switch (this) {
            case VERAO -> switch (categoria) {
                case AGUAS -> 0.95;
                case REFRIGERANTES -> 0.85;
                case CERVEJAS -> 0.85;
                case ENERGETICOS -> 0.75;
                case REFRESCOS -> 0.70;
                case SUCOS -> 0.60;
                case TONICAS -> 0.70;
                case OUTROS -> 0.50;
            };
            case OUTONO -> switch (categoria) {
                case SUCOS -> 0.75;
                case REFRIGERANTES -> 0.70;
                case CERVEJAS -> 0.65;
                case AGUAS -> 0.60;
                case ENERGETICOS -> 0.60;
                case REFRESCOS -> 0.65;
                case TONICAS -> 0.55;
                case OUTROS -> 0.50;
            };
            case INVERNO -> switch (categoria) {
                case SUCOS -> 0.85;
                case REFRIGERANTES -> 0.60;
                case CERVEJAS -> 0.55;
                case AGUAS -> 0.50;
                case ENERGETICOS -> 0.55;
                case REFRESCOS -> 0.60;
                case TONICAS -> 0.50;
                case OUTROS -> 0.50;
            };
            case PRIMAVERA -> switch (categoria) {
                case REFRIGERANTES -> 0.80;
                case ENERGETICOS -> 0.75;
                case CERVEJAS -> 0.70;
                case AGUAS -> 0.70;
                case SUCOS -> 0.65;
                case REFRESCOS -> 0.70;
                case TONICAS -> 0.60;
                case OUTROS -> 0.50;
            };
        };
    }
}
