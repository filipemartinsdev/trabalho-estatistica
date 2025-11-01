package estatistica;

import java.util.*;

public class TabelaFrequencia {
    private ArrayList<String> dadosOriginais;
    private Map<String, Integer> frequencias;
    private String[] categorias;
    private int[] valores;

    public TabelaFrequencia() {
        dadosOriginais = new ArrayList<>();
        frequencias = new LinkedHashMap<>();
    }

    public void processarEntrada(String textoInput, boolean ordenarPorFrequencia) {
        dadosOriginais.clear();
        frequencias.clear();
        String[] valoresArray = textoInput.split("[,\n]+");
        for (String valor : valoresArray) {
            String categoria = valor.trim();
            if (!categoria.isEmpty()) {
                dadosOriginais.add(categoria);
                frequencias.put(categoria, frequencias.getOrDefault(categoria, 0) + 1);
            }
        }
        if (ordenarPorFrequencia) {
            frequencias = frequencias.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(LinkedHashMap::new,
                            (m, e) -> m.put(e.getKey(), e.getValue()),
                            Map::putAll);
        }
        categorias = frequencias.keySet().toArray(new String[0]);
        valores = frequencias.values().stream().mapToInt(Integer::intValue).toArray();
    }

    public ArrayList<String> getDadosOriginais() { return dadosOriginais; }
    public Map<String, Integer> getFrequencias() { return frequencias; }
    public String[] getCategorias() { return categorias; }
    public int[] getValores() { return valores; }
    public int getTotalDados() { return dadosOriginais.size(); }
}
