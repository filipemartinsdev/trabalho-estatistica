package estatistica;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;

public class TabelaNominal extends JFrame {
    // Split panes para controle dinâmico dos painéis
    private JSplitPane splitHorizontal;
    private JSplitPane splitVertical;

    // Cores do tema claro
    private static final Color COR_FUNDO = new Color(248, 249, 250);
    private static final Color COR_PAINEL = new Color(255, 255, 255);
    private static final Color COR_BORDA = new Color(222, 226, 230);
    private static final Color COR_TEXTO = new Color(33, 37, 41);
    private static final Color COR_BOTAO = new Color(0, 123, 255);
    private static final Color COR_BOTAO_HOVER = new Color(0, 105, 217);
    private static final Color COR_BOTAO_TEXTO = new Color(255, 255, 255);
    private static final Color COR_VERMELHO = new Color(220, 53, 69);
    private static final Color COR_SUCESSO = new Color(40, 167, 69);
    private static final Color COR_CINZA = new Color(108, 117, 125);

    // Novas cores para a tabela
    private static final Color COR_CABECALHO_TABELA = new Color(52, 58, 64);
    private static final Color COR_TEXTO_CABECALHO = Color.WHITE;
    private static final Color COR_LINHA_PAR = new Color(248, 249, 250);
    private static final Color COR_LINHA_IMPAR = Color.WHITE;
    private static final Color COR_TOTAL = new Color(220, 53, 69);
    private static final Color COR_TEXTO_TOTAL = Color.WHITE;

    public static void main(String[] args) {
        System.out.println("[OK] Calculadora com.estatistica - v2.0");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Forçar tema claro mesmo no sistema
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    aplicarTemaClaro();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new TabelaNominal().setVisible(true);
            }
        });
    }

    // Método para aplicar tema claro em todos os componentes
    private static void aplicarTemaClaro() {
        UIManager.put("Panel.background", COR_FUNDO);
        UIManager.put("Frame.background", COR_FUNDO);
        UIManager.put("TextArea.background", COR_PAINEL);
        UIManager.put("TextArea.foreground", COR_TEXTO);
        UIManager.put("TextArea.selectionBackground", COR_BOTAO);
        UIManager.put("TextArea.selectionForeground", COR_BOTAO_TEXTO);
        UIManager.put("TextField.background", COR_PAINEL);
        UIManager.put("TextField.foreground", COR_TEXTO);
        UIManager.put("Button.background", COR_BOTAO);
        UIManager.put("Button.foreground", COR_BOTAO_TEXTO);
        UIManager.put("Button.select", COR_BOTAO_HOVER);
        UIManager.put("CheckBox.background", COR_FUNDO);
        UIManager.put("CheckBox.foreground", COR_TEXTO);
        UIManager.put("Label.foreground", COR_TEXTO);
        UIManager.put("TitledBorder.titleColor", COR_TEXTO);
        UIManager.put("ScrollPane.background", COR_FUNDO);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(COR_BORDA));
        UIManager.put("Viewport.background", COR_PAINEL);
    }

    // Componentes da interface
    private JTextArea inputDados;
    private JPanel painelTabelaContainer; // Novo: Container da tabela customizada
    private JTextArea outputEstatisticas; // Novo: Área separada para estatísticas
    private PainelGrafico painelGrafico;
    private JScrollPane scrollGrafico;
    // Campos de configuração do gráfico (painel de configurações)
    private JTextField tituloGraficoField;
    private JTextField descricaoYField;
    private JTextField descricaoXField;
    private JButton btnCalcular, btnLimpar, btnExemplo, btnGraficoFi, btnGraficoFr, btnCopiarTabela, btnExportarGrafico;
    private JCheckBox checkOrdenar;

    // Controle de estado
    private boolean tabelaCalculada = false;

    // Dados estatísticos
    private ArrayList<String> dadosOriginais;
    private Map<String, Integer> frequencias;
    private String[] categorias;
    private int[] valores;
    private Color[] cores;

    private void configurarScrollPane(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(COR_PAINEL);
    }

    public TabelaNominal() {
        aplicarTemaComponentes();
        inicializarInterface();
        dadosOriginais = new ArrayList<>();
        frequencias = new LinkedHashMap<>();
    }

    private void aplicarTemaComponentes() {
        setBackground(COR_FUNDO);
        getContentPane().setBackground(COR_FUNDO);
    }

    private void inicializarInterface() {
        setTitle("Sistema de Análise Estatística - Tabela de Distribuição Nominal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel superior - Entrada de dados
        JPanel painelEntrada = criarPainelEntrada();

        // Área da tabela customizada
        painelTabelaContainer = new JPanel(new BorderLayout());
        painelTabelaContainer.setBackground(COR_PAINEL);

        JScrollPane scrollTabela = new JScrollPane(painelTabelaContainer);
        scrollTabela.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                "Tabela de Frequências",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        configurarScrollPane(scrollTabela);

        // Área de estatísticas (nova)
        outputEstatisticas = new JTextArea(10, 40);
        outputEstatisticas.setFont(new Font("Arial", Font.PLAIN, 12));
        outputEstatisticas.setEditable(false);
        outputEstatisticas.setBackground(COR_PAINEL);
        outputEstatisticas.setForeground(COR_TEXTO);
        outputEstatisticas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                "Estatísticas Descritivas",
                0, 0, new Font("Arial", Font.BOLD, 12), COR_TEXTO
        ));

        JScrollPane scrollEstatisticas = new JScrollPane(outputEstatisticas);
        scrollEstatisticas.setPreferredSize(new Dimension(400, 260));
        configurarScrollPane(scrollEstatisticas); // Altura inicial suficiente para leitura

        // Painel para tabela e estatísticas
        JPanel painelTabelaEstatisticas = new JPanel(new BorderLayout());
        JSplitPane splitTabelaEstatisticas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTabela, scrollEstatisticas);
        splitTabelaEstatisticas.setDividerLocation(265);
        splitTabelaEstatisticas.setResizeWeight(0.7);
        painelTabelaEstatisticas.add(splitTabelaEstatisticas, BorderLayout.CENTER);

        // Área do gráfico com scroll horizontal
        painelGrafico = new PainelGrafico();
        painelGrafico.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                "", // Grafico de barras
                0, 0, new Font("Arial", Font.BOLD, 12), COR_TEXTO
        ));
        scrollGrafico = new JScrollPane(painelGrafico);
        scrollGrafico.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollGrafico.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        configurarScrollPane(scrollGrafico);    // JSplitPane horizontal para tabela/estatísticas (sem gráfico inicialmente)
        splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelTabelaEstatisticas, null);
        splitHorizontal.setDividerLocation(900);
        splitHorizontal.setResizeWeight(1.0);
        splitHorizontal.setOneTouchExpandable(true);
        splitHorizontal.setDividerSize(8);
        splitHorizontal.setBackground(COR_FUNDO);

        // JSplitPane vertical para entrada de dados e conteúdo principal
        splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelEntrada, splitHorizontal);
        splitVertical.setDividerLocation(180);
        splitVertical.setResizeWeight(0.0);
        splitVertical.setOneTouchExpandable(true);
        splitVertical.setDividerSize(8);
        splitVertical.setBackground(COR_FUNDO);

        // Adicionar o JSplitPane principal à janela
        add(splitVertical, BorderLayout.CENTER);

        // Configurações da janela
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private JPanel criarPainelEntrada() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_FUNDO);
        painel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                "Entrada de Dados Categóricos (Separados por vírgula)",
                0, 0, new Font("Arial", Font.BOLD, 12), COR_TEXTO
        ));

        // Área de texto para dados
        inputDados = new JTextArea(5, 50);
        inputDados.setFont(new Font("Arial", Font.PLAIN, 12));
        inputDados.setBackground(COR_PAINEL);
        inputDados.setForeground(COR_TEXTO);
        inputDados.setCaretColor(COR_TEXTO);
        inputDados.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

    JScrollPane scrollInput = new JScrollPane(inputDados);
    scrollInput.getViewport().setBackground(COR_PAINEL);

    // Painel esquerdo (input + controles)
    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setBackground(COR_FUNDO);
    leftPanel.add(scrollInput, BorderLayout.CENTER);

    // Painel de controles
    JPanel painelControles = new JPanel(new FlowLayout());
    painelControles.setBackground(COR_FUNDO);

    checkOrdenar = new JCheckBox("Ordenar por Frequência", false);
    estilizarCheckbox(checkOrdenar);
    painelControles.add(checkOrdenar);

        // Botão: Copiar Tabela para Excel
        btnCopiarTabela = new JButton("📋 Copiar Tabela");
        estilizarBotaoCopiar(btnCopiarTabela);
        btnCopiarTabela.setEnabled(false);
        btnCopiarTabela.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copiarTabelaParaExcel();
            }
        });
        painelControles.add(btnCopiarTabela);

        // Botão para calcular apenas a tabela
        btnCalcular = new JButton("📊 Calcular Tabela");
        estilizarBotao(btnCalcular);
        btnCalcular.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcularTabela();
            }
        });
        painelControles.add(btnCalcular);

        // Botão para gráfico Fi (Frequência Absoluta)
        btnGraficoFi = new JButton("📈 Gráfico Fi");
        estilizarBotao(btnGraficoFi);
        btnGraficoFi.setEnabled(false);
        btnGraficoFi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gerarGraficoFi();
            }
        });
        painelControles.add(btnGraficoFi);

        // Botão para gráfico Fr (Frequência Relativa)
        btnGraficoFr = new JButton("📉 Gráfico Fr");
        estilizarBotao(btnGraficoFr);
        btnGraficoFr.setEnabled(false);
        btnGraficoFr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gerarGraficoFr();
            }
        });
        painelControles.add(btnGraficoFr);

        // Botão para exportar gráfico
        btnExportarGrafico = new JButton("💾 Exportar Gráfico");
        estilizarBotao(btnExportarGrafico);
        btnExportarGrafico.setEnabled(false);
        btnExportarGrafico.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                painelGrafico.exportarGrafico();
            }
        });
        painelControles.add(btnExportarGrafico);

        // Botao para carregar exemplo
        btnExemplo = new JButton("📝 Carregar Exemplo");
        estilizarBotao(btnExemplo, COR_CINZA);
        btnExemplo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                carregarExemplo();
            }
        });
        painelControles.add(btnExemplo);

        // Botao para limpar
        btnLimpar = new JButton("🗑️ Limpar");
        estilizarBotao(btnLimpar, COR_VERMELHO);
        btnLimpar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limparTudo();
            }
        });
        painelControles.add(btnLimpar);

    leftPanel.add(painelControles, BorderLayout.SOUTH);

    // Painel de configurações (lado direito da entrada)
    JPanel painelConfiguracoes = new JPanel();
    painelConfiguracoes.setLayout(new GridBagLayout());
    painelConfiguracoes.setBackground(COR_PAINEL);
    painelConfiguracoes.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(COR_BORDA),
        "Configurações do Gráfico",
        TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 12), COR_TEXTO
    ));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0; gbc.gridy = 0;
    painelConfiguracoes.add(new JLabel("Título do gráfico"), gbc);

    tituloGraficoField = new JTextField();
    tituloGraficoField.setColumns(20);
    tituloGraficoField.setBackground(COR_PAINEL);
    gbc.gridx = 0; gbc.gridy = 1;
    painelConfiguracoes.add(tituloGraficoField, gbc);

    gbc.gridx = 0; gbc.gridy = 2;
    painelConfiguracoes.add(new JLabel("Descrição eixo Y"), gbc);

    descricaoYField = new JTextField();
    descricaoYField.setColumns(20);
    descricaoYField.setBackground(COR_PAINEL);
    gbc.gridx = 0; gbc.gridy = 3;
    painelConfiguracoes.add(descricaoYField, gbc);

    gbc.gridx = 0; gbc.gridy = 4;
    painelConfiguracoes.add(new JLabel("Descrição eixo X"), gbc);

    descricaoXField = new JTextField();
    descricaoXField.setColumns(20);
    descricaoXField.setBackground(COR_PAINEL);
    gbc.gridx = 0; gbc.gridy = 5;
    painelConfiguracoes.add(descricaoXField, gbc);

    // Divisor entre input e configurações
    JSplitPane splitTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, painelConfiguracoes);
    splitTop.setDividerLocation(840);
    splitTop.setResizeWeight(1.0);
    splitTop.setOneTouchExpandable(true);
    splitTop.setDividerSize(8);

    painel.add(splitTop, BorderLayout.CENTER);
    return painel;
    }

    // Métodos de estilização (mantidos iguais)
    private void estilizarBotao(JButton botao) {
        estilizarBotao(botao, COR_BOTAO);
    }

    private void estilizarBotao(JButton botao, Color corFundo) {
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setBackground(corFundo);
        botao.setForeground(COR_BOTAO_TEXTO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corFundo.darker()),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(corFundo.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(corFundo);
            }
        });
    }

    private void estilizarBotaoCopiar(JButton botao) {
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setBackground(COR_CINZA);
        botao.setForeground(COR_BOTAO_TEXTO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_CINZA.darker()),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(COR_CINZA.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(COR_CINZA);
            }
        });
    }

    private void estilizarCheckbox(JCheckBox checkbox) {
        checkbox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkbox.setBackground(COR_FUNDO);
        checkbox.setForeground(COR_TEXTO);
        checkbox.setFocusPainted(false);
    }

    // MÉTODO PRINCIPAL MODIFICADO - Agora cria tabela customizada
    private void calcularTabela() {
        try {
            // 1. CAPTURAR E VALIDAR DADOS
            String textoInput = inputDados.getText().trim();
            if (textoInput.isEmpty()) {
                mostrarErro("Digite os dados categóricos separados por vírgula ou quebra de linha!");
                return;
            }

            // 2. PROCESSAR ENTRADA
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

            if (dadosOriginais.isEmpty()) {
                mostrarErro("Nenhum dado válido encontrado!");
                return;
            }

            // 3. PREPARAR DADOS PARA VISUALIZAÇÃO
            prepararDadosVisualizacao();

            // 4. GERAR TABELA CUSTOMIZADA (NOVO)
            criarTabelaCustomizada();

            // 5. GERAR ESTATÍSTICAS (NOVO)
            gerarEstatisticas();

            // 6. HABILITAR BOTÕES DE GRÁFICO E COPIAR
            tabelaCalculada = true;
            btnGraficoFi.setEnabled(true);
            btnGraficoFr.setEnabled(true);
            btnCopiarTabela.setEnabled(true);

            // 7. Atualizar ou limpar gráfico: se o gráfico já estiver visível, apenas repinta (para aplicar novos rótulos),
            // caso contrário, limpa para mostrar mensagem de aguardo.
            if (splitHorizontal.getRightComponent() == scrollGrafico && painelGrafico.temTipoGrafico()) {
                // Mantém o tipo de gráfico atual e repinta para aplicar novos títulos/labels
                painelGrafico.repaint();
            } else {
                // Limpa o gráfico (nenhum tipo definido)
                painelGrafico.limparGrafico();
                painelGrafico.repaint();
            }

        } catch (Exception ex) {
            mostrarErro("Erro no processamento: " + ex.getMessage());
        }
    }

    // NOVO MÉTODO - Criar tabela customizada
    private void criarTabelaCustomizada() {
        painelTabelaContainer.removeAll();
        JPanel painelTabela = new JPanel();
        painelTabela.setLayout(new GridBagLayout());
        painelTabela.setBackground(COR_PAINEL);

        // Alinhar ao topo
        painelTabelaContainer.setLayout(new BorderLayout());
        painelTabelaContainer.add(painelTabela, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.25; // Permite encolher um pouco mais horizontalmente
        gbc.insets = new Insets(1, 1, 1, 1);

        int totalDados = dadosOriginais.size();
        int freqAbsAcumulada = 0;
        double freqRelDecimalAcum = 0.0;

        // CABEÇALHO
        gbc.gridy = 0;
        gbc.gridwidth = 7;
        gbc.insets = new Insets(0, 0, 2, 0);

        JPanel cabecalho = criarLinhaTabela(
                new String[]{"Categoria", "Freq. Abs.", "Freq. Abs. Acm.",
                        "Freq. Rel.", "Freq. Rel. Acm.",
                        "Freq. Rel. %", "Freq. Rel. % Acm."},
                COR_CABECALHO_TABELA, COR_TEXTO_CABECALHO, true
        );
        painelTabela.add(cabecalho, gbc);

        // LINHAS DE DADOS
        gbc.gridwidth = 1;
        gbc.insets = new Insets(1, 1, 1, 1);

        for (int i = 0; i < categorias.length; i++) {
            gbc.gridy = i + 1;

            String categoria = categorias[i];
            int freqAbs = valores[i];
            freqAbsAcumulada += freqAbs;

            double freqRelDecimal = (double) freqAbs / totalDados;
            freqRelDecimalAcum += freqRelDecimal;
            double freqRelPercentual = freqRelDecimal * 100.0;
            double freqRelPercentualAcum = freqRelDecimalAcum * 100.0;

            Color corFundo = (i % 2 == 0) ? COR_LINHA_PAR : COR_LINHA_IMPAR;

            JPanel linha = criarLinhaTabela(
                    new String[]{
                            categoria,
                            String.valueOf(freqAbs),
                            String.valueOf(freqAbsAcumulada),
                            String.format("%.4f", freqRelDecimal),
                            String.format("%.4f", freqRelDecimalAcum),
                            String.format("%.2f%%", freqRelPercentual),
                            String.format("%.2f%%", freqRelPercentualAcum)
                    },
                    corFundo, COR_TEXTO, false
            );
            painelTabela.add(linha, gbc);
        }

        // LINHA TOTAL (SEMPRE ÚLTIMA)
        gbc.gridy = categorias.length + 1;
        gbc.insets = new Insets(2, 1, 1, 1);

        JPanel linhaTotal = criarLinhaTabela(
                new String[]{
                        "TOTAL",
                        String.valueOf(totalDados),
                        String.valueOf(totalDados),
                        "1.0000",
                        "1.0000",
                        "100.00%",
                        "100.00%"
                },
                COR_TOTAL, COR_TEXTO_TOTAL, true
        );
        painelTabela.add(linhaTotal, gbc);

        painelTabelaContainer.revalidate();
        painelTabelaContainer.repaint();
    }

    // NOVO MÉTODO - Criar linha da tabela customizada
    private JPanel criarLinhaTabela(String[] textos, Color corFundo, Color corTexto, boolean isCabecalho) {
        JPanel linha = new JPanel(new GridLayout(1, textos.length));
        linha.setBackground(corFundo);
        linha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));

        for (int i = 0; i < textos.length; i++) {
            JLabel label = new JLabel(textos[i]);
            label.setForeground(corTexto);
            label.setFont(new Font("Arial", isCabecalho ? Font.BOLD : Font.PLAIN, 11));

            if (i == 0) {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }

            linha.add(label);
        }

        return linha;
    }

    // NOVO MÉTODO - Gerar estatísticas separadas
    private void gerarEstatisticas() {
        StringBuilder sb = new StringBuilder();
        int totalDados = dadosOriginais.size();

        sb.append("📊 ESTATÍSTICAS DESCRITIVAS:\n");
        sb.append("───────────────────────────────────────\n");
        sb.append(String.format("• Total de Observações: %d\n", totalDados));
        sb.append(String.format("• Número de Categorias: %d\n", categorias.length));
        sb.append(String.format("• Categoria Mais Frequente: %s (%d ocorrências)\n", categorias[0], valores[0]));

        int minFreq = Arrays.stream(valores).min().orElse(0);
        String categoriaMenosFreq = "";
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] == minFreq) {
                categoriaMenosFreq = categorias[i];
                break;
            }
        }
        sb.append(String.format("• Categoria Menos Frequente: %s (%d ocorrências)\n", categoriaMenosFreq, minFreq));

        sb.append("\n🎯 MEDIDA DE TENDÊNCIA CENTRAL:\n");
        sb.append("───────────────────────────────────────\n");

        int maxFreq = Arrays.stream(valores).max().orElse(0);
        ArrayList<String> modas = new ArrayList<>();
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] == maxFreq) {
                modas.add(categorias[i]);
            }
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

        outputEstatisticas.setText(sb.toString());
    }

    // MÉTODO COPIAR TABELA MODIFICADO - Agora copia da tabela customizada
    private void copiarTabelaParaExcel() {
        if (!tabelaCalculada) {
            mostrarErro("Primeiro calcule a tabela para poder copiar!");
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();

            // Cabeçalho
            sb.append("Categoria\tFreq. Abs.\tFreq. Abs. Acm.\tFreq. Rel.\tFreq. Rel. Acm.\tFreq. Rel. %\tFreq. Rel. % Acm.\n");

            int totalDados = dadosOriginais.size();
            int freqAbsAcumulada = 0;
            double freqRelDecimalAcum = 0.0;

            // Dados (na mesma ordem da tabela customizada)
            for (int i = 0; i < categorias.length; i++) {
                String categoria = categorias[i];
                int freqAbs = valores[i];
                freqAbsAcumulada += freqAbs;

                double freqRelDecimal = (double) freqAbs / totalDados;
                freqRelDecimalAcum += freqRelDecimal;
                double freqRelPercentual = freqRelDecimal * 100.0;
                double freqRelPercentualAcum = freqRelDecimalAcum * 100.0;

                sb.append(String.format("%s\t%d\t%d\t%.4f\t%.4f\t%.2f%%\t%.2f%%\n",
                        categoria, freqAbs, freqAbsAcumulada,
                        freqRelDecimal, freqRelDecimalAcum,
                        freqRelPercentual, freqRelPercentualAcum));
            }

            // Total (sempre último)
            sb.append(String.format("TOTAL\t%d\t%d\t%.4f\t%.4f\t%.2f%%\t%.2f%%\n",
                    totalDados, totalDados, 1.0, 1.0, 100.0, 100.0));

            // Copiar para área de transferência
            StringSelection selection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);

            JOptionPane.showMessageDialog(this,
                    "Tabela copiada para a área de transferência!\n\n",
                    "Tabela Copiada", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            mostrarErro("Erro ao copiar tabela: " + ex.getMessage());
        }
    }

    // MÉTODOS EXISTENTES (mantidos iguais)
    private void prepararDadosVisualizacao() {
        if (checkOrdenar.isSelected()) {
            frequencias = frequencias.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(LinkedHashMap::new,
                            (m, e) -> m.put(e.getKey(), e.getValue()),
                            Map::putAll);
        }

        categorias = frequencias.keySet().toArray(new String[0]);
        valores = frequencias.values().stream().mapToInt(Integer::intValue).toArray();
        gerarCores();
    }

    private void gerarCores() {
        cores = new Color[categorias.length];
        Color[] paletaCores = {
                new Color(255, 99, 132), new Color(54, 162, 235), new Color(243, 182, 12),
                new Color(75, 192, 192), new Color(153, 102, 255), new Color(255, 159, 64),
                new Color(199, 199, 199), new Color(83, 102, 255), new Color(255, 99, 255),
                new Color(99, 255, 132)
        };
        for (int i = 0; i < cores.length; i++) {
            cores[i] = paletaCores[i % paletaCores.length];
        }
    }

    // Métodos de gráfico (mantidos iguais)
    private void gerarGraficoFi() {
        if (!tabelaCalculada) {
            mostrarErro("Primeiro calcule a tabela!");
            return;
        }
        try {
            painelGrafico.setTipoGrafico("Fi");
            painelGrafico.repaint();
            // Força a exibição do gráfico e restaura se minimizado
            splitHorizontal.setRightComponent(scrollGrafico);
            splitHorizontal.setDividerLocation(600);
            splitHorizontal.setResizeWeight(0.5);
            if (splitHorizontal.getDividerLocation() < 10) {
                splitHorizontal.setDividerLocation(600);
            }
            btnExportarGrafico.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                    "Gráfico de Frequência Absoluta (Fi) gerado com sucesso!",
                    "Gráfico Fi", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            mostrarErro("Erro ao gerar gráfico Fi: " + ex.getMessage());
        }
    }

    private void gerarGraficoFr() {
        if (!tabelaCalculada) {
            mostrarErro("Primeiro calcule a tabela!");
            return;
        }
        try {
            painelGrafico.setTipoGrafico("Fr");
            painelGrafico.repaint();
            // Força a exibição do gráfico e restaura se minimizado
            splitHorizontal.setRightComponent(scrollGrafico);
            splitHorizontal.setDividerLocation(600);
            splitHorizontal.setResizeWeight(0.5);
            if (splitHorizontal.getDividerLocation() < 10) {
                splitHorizontal.setDividerLocation(600);
            }
            btnExportarGrafico.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                    "Gráfico de Frequência Relativa (Fr) gerado com sucesso!",
                    "Gráfico Fr", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            mostrarErro("Erro ao gerar gráfico Fr: " + ex.getMessage());
        }
    }

    private void carregarExemplo() {
        String exemplo = """
                Ensino Médio, Ensino Superior, Ensino Médio, Ensino Fundamental,
                Ensino Superior, Ensino Médio, Pós-Graduação, Ensino Superior,
                Ensino Médio, Ensino Fundamental, Ensino Superior, Ensino Médio,
                Pós-Graduação, Ensino Superior, Ensino Médio, Ensino Superior,
                Ensino Fundamental, Ensino Médio, Ensino Superior, Ensino Médio
                """;
        inputDados.setText(exemplo);
        JOptionPane pane = new JOptionPane(
                "Exemplo carregado!\n\nDados: Escolaridade de 20 funcionários\n" +
                        "• Ensino Fundamental\n• Ensino Médio\n• Ensino Superior\n• Pós-Graduação",
                JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Exemplo - Dados Nominais");
        dialog.getContentPane().setBackground(COR_FUNDO);
        dialog.setVisible(true);
    }

    private void limparTudo() {
        inputDados.setText("");
        outputEstatisticas.setText("");
        painelTabelaContainer.removeAll();
        painelTabelaContainer.revalidate();
        painelTabelaContainer.repaint();
        dadosOriginais.clear();
        frequencias.clear();
        checkOrdenar.setSelected(false);
        tabelaCalculada = false;
        btnGraficoFi.setEnabled(false);
        btnGraficoFr.setEnabled(false);
        btnCopiarTabela.setEnabled(false);
        btnExportarGrafico.setEnabled(false);
        painelGrafico.limparGrafico();
        painelGrafico.repaint();
    }

    private void mostrarErro(String mensagem) {
        JOptionPane pane = new JOptionPane(mensagem, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Erro");
        dialog.getContentPane().setBackground(COR_FUNDO);
        dialog.setVisible(true);
    }

    // CLASSE PainelGrafico (MANTIDA COMPLETAMENTE IGUAL)
    class PainelGrafico extends JPanel {
        private String tipoGrafico = "";
            // Retângulos das colunas para detectar cliques
            private java.util.List<Rectangle> colunasBounds = new ArrayList<>();

        public PainelGrafico() {
            setBackground(COR_PAINEL);
            // Listener para clique nas colunas
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (categorias == null || categorias.length == 0) return;
                    // verificar qual coluna foi clicada
                    for (int i = 0; i < colunasBounds.size(); i++) {
                        Rectangle r = colunasBounds.get(i);
                        if (r != null && r.contains(e.getPoint())) {
                            // abrir seletor de cor
                            Color nova = JColorChooser.showDialog(PainelGrafico.this, "Escolha a cor da categoria: " + categorias[i], cores[i]);
                            if (nova != null) {
                                cores[i] = nova;
                                repaint();
                            }
                            break;
                        }
                    }
                }
            });
        }

        public void setTipoGrafico(String tipo) {
            this.tipoGrafico = tipo;
            updatePreferredSize();
        }

        public boolean temTipoGrafico() {
            return this.tipoGrafico != null && !this.tipoGrafico.isEmpty();
        }

        public void limparGrafico() {
            this.tipoGrafico = "";
            setPreferredSize(null);
            revalidate();
        }

        private void updatePreferredSize() {
            if (categorias != null && categorias.length > 0) {
                int larguraMinima = Math.max(80, categorias.length * 100);
                int alturaMinima = 400;
                setPreferredSize(new Dimension(larguraMinima, alturaMinima));
                revalidate();
            }
        }

        // ... (todos os métodos de desenho do gráfico mantidos 100% iguais)
        // [TODO O RESTO DO CÓDIGO DO GRÁFICO PERMANECE EXATAMENTE IGUAL]




        // ... (resto dos métodos do gráfico permanecem iguais)
        // Método para calcular escala correta
        private double calcularEscalaMaxima(double[] valores) {
            double maxValor = Arrays.stream(valores).max().orElse(1.0);
            double magnitude = Math.pow(10, Math.floor(Math.log10(maxValor)));
            double normalizado = maxValor / magnitude;

            double escalaFinal;
            if (normalizado <= 1.0) {
                escalaFinal = 1.0 * magnitude;
            } else if (normalizado <= 2.0) {
                escalaFinal = 2.0 * magnitude;
            } else if (normalizado <= 5.0) {
                escalaFinal = 5.0 * magnitude;
            } else {
                escalaFinal = 10.0 * magnitude;
            }

            if (escalaFinal <= maxValor) {
                escalaFinal = maxValor * 1.1;
            }

            return escalaFinal;
        }

        private double[] gerarValoresEscala(double maxEscala, int numMarcas) {
            double[] valores = new double[numMarcas + 1];
            double incremento = maxEscala / numMarcas;

            for (int i = 0; i <= numMarcas; i++) {
                valores[i] = i * incremento;
            }

            return valores;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Fundo do gráfico
            g.setColor(COR_PAINEL);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Se não há dados calculados ou tipo não definido
            if (categorias == null || categorias.length == 0 || tipoGrafico.isEmpty()) {
                g.setColor(COR_TEXTO);
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                String mensagem = tipoGrafico.isEmpty() ?
                        "Calcule a tabela e selecione o tipo de gráfico" :
                        "Gráfico será exibido após o cálculo";
                g.drawString(mensagem, 50, getHeight()/2);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int margem = 60;
            int larguraGrafico = getWidth() - 2 * margem;
            int alturaGrafico = getHeight() - 2 * margem - 60;

            // Calcular valores conforme o tipo de gráfico
            double[] valoresGrafico = new double[categorias.length];
            int totalDados = dadosOriginais.size();

            if (tipoGrafico.equals("Fi")) {
                for (int i = 0; i < valores.length; i++) {
                    valoresGrafico[i] = valores[i];
                }
            } else if (tipoGrafico.equals("Fr")) {
                for (int i = 0; i < valores.length; i++) {
                    valoresGrafico[i] = (valores[i] * 100.0) / totalDados;
                }
            }

            double maxEscala = calcularEscalaMaxima(valoresGrafico);
            int numMarcas = 10;
            double[] valoresEscala = gerarValoresEscala(maxEscala, numMarcas);

            // Desenhar eixos
            g2d.setColor(COR_TEXTO);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(margem, margem, margem, getHeight() - margem - 40);
            g2d.drawLine(margem, getHeight() - margem - 40, getWidth() - margem, getHeight() - margem - 40);

            // Desenhar escala no eixo Y
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= numMarcas; i++) {
                double valor = valoresEscala[i];
                double proporcao = valor / maxEscala;
                int yPos = getHeight() - margem - 40 - (int)(proporcao * alturaGrafico);

                // Linha da grade
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, 0));
                if (i > 0) {
                    g2d.drawLine(margem, yPos, getWidth() - margem, yPos);
                }

                // Rótulo do eixo Y
                g2d.setColor(COR_TEXTO);
                g2d.setStroke(new BasicStroke(1));
                String label;
                if (tipoGrafico.equals("Fr")) {
                    label = String.format(valor == (int)valor ? "%.0f%%" : "%.1f%%", valor);
                } else {
                    label = String.format(valor == (int)valor ? "%.0f" : "%.1f", valor);
                    // label = String.format(valor == (int)valor ? "%d" : "%d", valor);
                }
                g2d.drawString(label, margem - 30, yPos + 4);
                g2d.drawLine(margem - 5, yPos, margem, yPos);
            }

            // Desenhar colunas
            int larguraColuna = Math.max(40, Math.min(80, (larguraGrafico / categorias.length) - 10));
            int espacamento = larguraGrafico / categorias.length;

            for (int i = 0; i < categorias.length; i++) {
                double proporcao = valoresGrafico[i] / maxEscala;
                int alturaColuna = (int) (proporcao * alturaGrafico);
                int x = margem + i * espacamento + (espacamento - larguraColuna) / 2;
                int y = getHeight() - margem - 40 - alturaColuna;

                if (valoresGrafico[i] > 0 && alturaColuna < 2) {
                    alturaColuna = 2;
                    y = getHeight() - margem - 40 - alturaColuna;
                }

                // Cor sólida para cada categoria
                g2d.setColor(cores[i]);
                g2d.fillRect(x, y, larguraColuna, alturaColuna);

                // Contorno das colunas
                g2d.setColor(COR_TEXTO);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x, y, larguraColuna, alturaColuna);

                // Valor no topo da coluna
                g2d.setColor(COR_TEXTO);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                String valorStr;
                if (tipoGrafico.equals("Fr")) {
                    valorStr = String.format(valoresGrafico[i] == (int)valoresGrafico[i] ? "%.0f%%" : "%.1f%%", valoresGrafico[i]);
                } else {
                    valorStr = String.format("%.0f", valoresGrafico[i]);
                }
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(valorStr);
                g2d.drawString(valorStr, x + (larguraColuna - textWidth) / 2, Math.max(y - 5, margem + 15));

                // Rótulos do eixo X (categorias)
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.setColor(COR_TEXTO);
                String categoria = categorias[i];
                if (categoria.length() > 12) {
                    categoria = categoria.substring(0, 10) + "..";
                }

                AffineTransform original = g2d.getTransform();
                int labelX = x + larguraColuna / 2;
                int labelY = getHeight() - margem - 24;

                int labelWidth = g2d.getFontMetrics().stringWidth(categoria);
                g2d.drawString(categoria, labelX - labelWidth/2, labelY);

                // Atualizar bounds da coluna para detecção de clique
                // Assegura que a lista tem posição para o índice
                while (colunasBounds.size() <= i) colunasBounds.add(null);
                colunasBounds.set(i, new Rectangle(x, y, larguraColuna, Math.max(alturaColuna, 2)));
            }

        // Título do gráfico (usar valor do campo se preenchido)
        g2d.setColor(COR_TEXTO);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String tituloDefault = tipoGrafico.equals("Fi") ?
            "Gráfico de Frequência Absoluta (Fi)" :
            "Gráfico de Frequência Relativa (Fr %)";
        String titulo = (tituloGraficoField != null && !tituloGraficoField.getText().trim().isEmpty()) ?
            tituloGraficoField.getText().trim() : tituloDefault;
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(titulo);
        g2d.drawString(titulo, (getWidth() - titleWidth) / 2, 25);

        // Labels dos eixos (usar campos se preenchidos)
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String labelY = (descricaoYField != null && !descricaoYField.getText().trim().isEmpty()) ?
            descricaoYField.getText().trim() : (tipoGrafico.equals("Fi") ? "Frequência Absoluta" : "Frequência Relativa (%)");

        AffineTransform original = g2d.getTransform();
        g2d.rotate(Math.toRadians(-90), 15, getHeight() / 2);
        g2d.drawString(labelY, 15, getHeight() / 2);
        g2d.setTransform(original);

        String labelX = (descricaoXField != null && !descricaoXField.getText().trim().isEmpty()) ?
            descricaoXField.getText().trim() : "Categorias";
        g2d.drawString(labelX, getWidth() / 2 - Math.max(40, g2d.getFontMetrics().stringWidth(labelX)/2), getHeight()-60);

            // Legenda de cores
            if (categorias.length <= 8) {
                desenharLegenda(g2d);
            }
        }

        public void exportarGrafico() {
            if (!temTipoGrafico() || categorias == null || categorias.length == 0) {
                JOptionPane.showMessageDialog(this,
                    "Primeiro gere um gráfico para poder exportá-lo!",
                    "Erro ao Exportar", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            FileFilter filtro = new FileNameExtensionFilter("Imagens PNG (*.png)", "png");
            fileChooser.setFileFilter(filtro);
            fileChooser.setSelectedFile(new File("grafico_estatistica.png"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                // Adicionar extensão .png se não foi especificada
                if (!arquivo.getName().toLowerCase().endsWith(".png")) {
                    arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".png");
                }

                try {
                    // Criar imagem com as dimensões atuais do painel
                    BufferedImage imagem = new BufferedImage(getWidth(), getHeight(), 
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = imagem.createGraphics();
                    
                    // Configurar rendering hints para melhor qualidade
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                            RenderingHints.VALUE_RENDER_QUALITY);

                    // Pintar fundo branco
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Pintar o gráfico
                    paint(g2d);
                    g2d.dispose();

                    // Salvar imagem
                    ImageIO.write(imagem, "png", arquivo);
                    JOptionPane.showMessageDialog(this,
                        "Gráfico exportado com sucesso para:\n" + arquivo.getPath(),
                        "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Erro ao exportar gráfico: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void desenharLegenda(Graphics2D g2d) {
            int legendaX = getWidth() - 160;
            int legendaY = 60;
            int alturaItem = 18;

            // Fundo da legenda
            g2d.setColor(new Color(255, 255, 255, 230));
            g2d.fillRoundRect(legendaX - 10, legendaY - 25, 150, categorias.length * alturaItem + 30, 5, 5);
            g2d.setColor(COR_BORDA);
            g2d.drawRoundRect(legendaX - 10, legendaY - 25, 150, categorias.length * alturaItem + 30, 5, 5);

            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.setColor(COR_TEXTO);
            g2d.drawString("Legenda", legendaX, legendaY - 10);

            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i < Math.min(categorias.length, 8); i++) {
                int y = legendaY + i * alturaItem;

                g2d.setColor(cores[i]);
                g2d.fillRect(legendaX, y, 14, 14);
                g2d.setColor(COR_TEXTO);
                g2d.drawRect(legendaX, y, 14, 14);

                String categoria = categorias[i];
                if (categoria.length() > 18) {
                    categoria = categoria.substring(0, 15) + "...";
                }

                String valorFreq = tipoGrafico.equals("Fr") ?
                        String.format(" (%.1f%%)", (valores[i] * 100.0) / dadosOriginais.size()) :
                        String.format(" (%d)", valores[i]);

                String textoLegenda = categoria + valorFreq;
                if (textoLegenda.length() > 22) {
                    textoLegenda = categoria.substring(0, Math.max(1, 18 - valorFreq.length())) + "..." + valorFreq;
                }

                g2d.drawString(textoLegenda, legendaX + 20, y + 11);
            }
        }
    }
}
