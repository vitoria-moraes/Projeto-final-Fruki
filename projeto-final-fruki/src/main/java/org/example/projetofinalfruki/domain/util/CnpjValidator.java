package org.example.projetofinalfruki.domain.util;

/** Validação e normalização de CNPJ (14 dígitos, com dígitos verificadores). */
public final class CnpjValidator {

    private CnpjValidator() {
    }

    public static String normalizar(String cnpj) {
        return cnpj == null ? "" : cnpj.replaceAll("\\D", "");
    }

    public static boolean valido(String cnpj) {
        String n = normalizar(cnpj);
        if (n.length() != 14 || n.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int d1 = digito(n, 12);
            int d2 = digito(n, 13);
            return n.charAt(12) - '0' == d1 && n.charAt(13) - '0' == d2;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int digito(String n, int tamanho) {
        int[] pesos = tamanho == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (n.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
