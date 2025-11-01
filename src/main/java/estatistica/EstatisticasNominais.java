package estatistica;

public class EstatisticasNominais {
    public static String gerarEstatisticas(String[] categorias, int[] valores, int totalDados) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uD83D\uDCCA ESTATÍSTICAS DESCRITIVAS:\n");
        sb.append("───────────────────────────────────────\n");
        sb.append(String.format("• Total de Observações: %d\n", totalDados));
        sb.append(String.format("• Número de Categorias: %d\n", categorias.length));
        sb.append(String.format("• Categoria Mais Frequente: %s (%d ocorrências)\n", categorias[0], valores[0]));
        int minFreq = Integer.MAX_VALUE;
        String categoriaMenosFreq = "";
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] < minFreq) {
                minFreq = valores[i];
                categoriaMenosFreq = categorias[i];
            }
        }
        sb.append(String.format("• Categoria Menos Frequente: %s (%d ocorrências)\n", categoriaMenosFreq, minFreq));
        sb.append("\n\uD83C\uDFC6 MEDIDA DE TENDÊNCIA CENTRAL:\n");
        sb.append("───────────────────────────────────────\n");
        int maxFreq = Integer.MIN_VALUE;
        for (int v : valores) if (v > maxFreq) maxFreq = v;
        java.util.ArrayList<String> modas = new java.util.ArrayList<>();
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] == maxFreq) modas.add(categorias[i]);
        }
        if (modas.size() == 1) {
            sb.append(String.format("• Moda: %s (unimodal)\n", modas.get(0)));
        } else if (modas.size() == categorias.length) {
            sb.append("• Distribuição: Amodal (todas categorias têm mesma frequência)\n");
        } else if (modas.size() == 2) {
            sb.append(String.format("• Modas: %s (bimodal)\n", String.join(", ", modas)));
        } else {
            sb.append(String.format("• Modas: %s (multimodal)\n", String.join(", ", modas)));
        }
        return sb.toString();
    }
}
