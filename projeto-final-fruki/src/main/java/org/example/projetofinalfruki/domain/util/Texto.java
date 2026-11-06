package org.example.projetofinalfruki.domain.util;

import java.text.Normalizer;
import java.util.Locale;

/** Utilitário para comparar textos sem acento e sem caixa. */
public final class Texto {

    private Texto() {
    }

    public static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }
}
