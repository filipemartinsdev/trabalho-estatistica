    // Métodos auxiliares para o PainelGraficoNominal

package estatistica;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;

public class TabelaNominal extends JFrame {
    // Split panes para controle dinâmico dos painéis
    private JSplitPane splitHorizontal;
    private JSplitPane splitVertical;

    // Cores do tema claro (mantidas para compatibilidade)
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
    private static final Color COR_CABECALHO_TABELA = new Color(52, 58, 64);
    private static final Color COR_TEXTO_CABECALHO = Color.WHITE;
    private static final Color COR_LINHA_PAR = new Color(248, 249, 250);
    private static final Color COR_LINHA_IMPAR = Color.WHITE;
    private static final Color COR_TOTAL = new Color(220, 53, 69);
    private static final Color COR_TEXTO_TOTAL = Color.WHITE;

    // Componentes da interface
    private JTextArea inputDados;
    private JPanel painelTabelaContainer;
    private JTextArea outputEstatisticas;
    private PainelGraficoNominal painelGrafico;
    private JScrollPane scrollGrafico;
    private JTextField tituloGraficoField;
    private JTextField descricaoYField;
    private JTextField descricaoXField;
    private JButton btnCalcular, btnLimpar, btnExemplo, btnGraficoFi, btnGraficoFr;
    private JCheckBox checkOrdenar;
    private boolean tabelaCalculada = false;

    private TabelaFrequencia tabelaFrequencia = new TabelaFrequencia();

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

                 TabelaNominal t = new TabelaNominal();
//                t.setIconImage(new ImageIcon("./icon.png").getImage());
                t.setIconImage(new ImageIcon("./icon.png").getImage());

                t.setVisible(true);
            }
        });
    }

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


        // Painel superior de opções (barra de botões) com alinhamento à esquerda e à direita
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(COR_FUNDO);

        // Botão de menu à esquerda (ícone)
        JButton btnMenu = new JButton();
        btnMenu.setFont(new Font("Arial", Font.BOLD, 16));
        btnMenu.setPreferredSize(new Dimension(34, 26));
        btnMenu.setBackground(new Color(200, 200, 200));
        btnMenu.setForeground(new Color(40, 40, 40));
        btnMenu.setFocusPainted(false);
        btnMenu.setToolTipText("Menu");
        btnMenu.setOpaque(true);
        btnMenu.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        // Ícone de menu simples (três barras)
        btnMenu.setText("≡");
        btnMenu.addChangeListener(e -> {
            ButtonModel model = btnMenu.getModel();
            if (model.isPressed() || model.isArmed()) {
                btnMenu.setBackground(new Color(170, 170, 170));
            } else if (model.isRollover()) {
                btnMenu.setBackground(new Color(210, 210, 210));
            } else {
                btnMenu.setBackground(new Color(200, 200, 200));
            }
        });
        // Popup de menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem itemInserir = new JMenuItem("Inserir");
        itemInserir.addActionListener(ev -> abrirDialogInserir());

        JMenu menuExportar = new JMenu("Exportar");
        JMenuItem itemCopiarTabela = new JMenuItem("Copiar tabela");
        itemCopiarTabela.addActionListener(ev -> copiarTabelaParaExcel());
        JMenuItem itemSalvarGrafico = new JMenuItem("Salvar gráfico");
        itemSalvarGrafico.addActionListener(ev -> painelGrafico.exportarGrafico());
        menuExportar.add(itemCopiarTabela);
        menuExportar.add(itemSalvarGrafico);

        popupMenu.add(itemInserir);
        popupMenu.add(menuExportar);
        btnMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenu.show(btnMenu, 0, btnMenu.getHeight());
            }
        });
        JPanel painelMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        painelMenu.setOpaque(false);

    
    painelMenu.add(btnMenu);
        painelSuperior.add(painelMenu, BorderLayout.WEST);

        // Painel central (botões principais)
        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        painelEsquerda.setOpaque(false);

        btnGraficoFi = new JButton("📈 Gráfico Fi");
        estilizarBotao(btnGraficoFi);
        btnGraficoFi.setEnabled(false);
        btnGraficoFi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gerarGraficoFi();
            }
        });
        painelEsquerda.add(btnGraficoFi);
        btnGraficoFr = new JButton("📉 Gráfico Fr");
        estilizarBotao(btnGraficoFr);
        btnGraficoFr.setEnabled(false);
        btnGraficoFr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gerarGraficoFr();
            }
        });
        painelEsquerda.add(btnGraficoFr);
        painelSuperior.add(painelEsquerda, BorderLayout.CENTER);

        // Painel direita (checkbox primeiro, depois botões)
        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        painelDireita.setOpaque(false);
        checkOrdenar = new JCheckBox("Ordenar por Frequência", false);
        estilizarCheckbox(checkOrdenar);
        painelDireita.add(checkOrdenar);
        btnCalcular = new JButton("📊 Calcular");
        estilizarBotao(btnCalcular);
        btnCalcular.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcularTabela();
            }
        });
        painelDireita.add(btnCalcular);
        btnExemplo = new JButton("📝 Carregar Exemplo");
        estilizarBotao(btnExemplo, COR_CINZA);
        btnExemplo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                carregarExemplo();
            }
        });
        painelDireita.add(btnExemplo);
        btnLimpar = new JButton("🗑️ Limpar");
        estilizarBotao(btnLimpar, COR_VERMELHO);
        btnLimpar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                limparTudo();
            }
        });
        painelDireita.add(btnLimpar);
        painelSuperior.add(painelDireita, BorderLayout.EAST);

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
        painelGrafico = new PainelGraficoNominal(tabelaFrequencia, this::getTituloGrafico, this::getDescricaoY, this::getDescricaoX);
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


        // Painel principal com barra superior
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.add(painelSuperior, BorderLayout.NORTH);
        painelPrincipal.add(painelEntrada, BorderLayout.CENTER);

        // JSplitPane vertical para painel principal e conteúdo principal
        splitVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelPrincipal, splitHorizontal);
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

        // Remove painel de controles do painel de entrada (agora está no painel superior)
        // ...existing code...

    // Painel de configurações (lado direito da entrada)
    JPanel painelConfiguracoes = new JPanel();

    painelConfiguracoes.setLayout(new GridBagLayout());
    painelConfiguracoes.setBackground(COR_PAINEL);
    painelConfiguracoes.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(COR_BORDA),
        "Configurações do Gráfico",
        TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 10), COR_TEXTO
    ));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 4, 2, 4); // padding menor
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0; gbc.gridy = 0;
    JLabel labelTitulo = new JLabel("Título do gráfico");
    labelTitulo.setFont(new Font("Arial", Font.PLAIN, 10));
    painelConfiguracoes.add(labelTitulo, gbc);

    tituloGraficoField = new JTextField();
    tituloGraficoField.setColumns(16);
    tituloGraficoField.setFont(new Font("Arial", Font.PLAIN, 10));
    tituloGraficoField.setBackground(COR_PAINEL);
    gbc.gridx = 0; gbc.gridy = 1;
    painelConfiguracoes.add(tituloGraficoField, gbc);

    gbc.gridx = 0; gbc.gridy = 2;
    JLabel labelY = new JLabel("Descrição eixo Y");
    labelY.setFont(new Font("Arial", Font.PLAIN, 10));
    painelConfiguracoes.add(labelY, gbc);

    descricaoYField = new JTextField();
    descricaoYField.setColumns(16);
    descricaoYField.setFont(new Font("Arial", Font.PLAIN, 10));
    descricaoYField.setBackground(COR_PAINEL);
    gbc.gridx = 0; gbc.gridy = 3;
    painelConfiguracoes.add(descricaoYField, gbc);

    gbc.gridx = 0; gbc.gridy = 4;
    JLabel labelX = new JLabel("Descrição eixo X");
    labelX.setFont(new Font("Arial", Font.PLAIN, 10));
    painelConfiguracoes.add(labelX, gbc);

    descricaoXField = new JTextField();
    descricaoXField.setColumns(16);
    descricaoXField.setFont(new Font("Arial", Font.PLAIN, 10));
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
        botao.setFont(new Font("Arial", Font.BOLD, 10));
        botao.setBackground(corFundo);
        botao.setForeground(COR_BOTAO_TEXTO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corFundo.darker()),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
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
        botao.setFont(new Font("Arial", Font.BOLD, 10));
        botao.setBackground(COR_CINZA);
        botao.setForeground(COR_BOTAO_TEXTO);
        botao.setFocusPainted(false);
        botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_CINZA.darker()),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
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

    // MÉTODO PRINCIPAL MODULARIZADO
    private void calcularTabela() {
        try {
            String textoInput = inputDados.getText().trim();
            if (textoInput.isEmpty()) {
                mostrarErro("Digite os dados categóricos separados por vírgula ou quebra de linha!");
                return;
            }
            tabelaFrequencia.processarEntrada(textoInput, checkOrdenar.isSelected());
            if (tabelaFrequencia.getDadosOriginais().isEmpty()) {
                mostrarErro("Nenhum dado válido encontrado!");
                return;
            }
            criarTabelaCustomizada();
            gerarEstatisticas();
            tabelaCalculada = true;
            btnGraficoFi.setEnabled(true);
            btnGraficoFr.setEnabled(true);
//            btnCopiarTabela.setEnabled(true);
            if (splitHorizontal.getRightComponent() == scrollGrafico && painelGrafico.temTipoGrafico()) {
                painelGrafico.repaint();
            } else {
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
    painelTabelaContainer.setLayout(new BorderLayout());
    painelTabelaContainer.add(painelTabela, BorderLayout.NORTH);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.25;
    gbc.insets = new Insets(1, 1, 1, 1);
    int totalDados = tabelaFrequencia.getTotalDados();
    int freqAbsAcumulada = 0;
    double freqRelDecimalAcum = 0.0;
    String[] categorias = tabelaFrequencia.getCategorias();
    int[] valores = tabelaFrequencia.getValores();
    // Cabeçalho
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
    // Linhas de dados
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
    // Linha total
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

    // NOVO MÉTODO - Gerar estatísticas usando classe modular
    private void gerarEstatisticas() {
        String texto = EstatisticasNominais.gerarEstatisticas(
            tabelaFrequencia.getCategorias(),
            tabelaFrequencia.getValores(),
            tabelaFrequencia.getTotalDados()
        );
        outputEstatisticas.setText(texto);
    }

    private void copiarTabelaParaExcel() {
        if (!tabelaCalculada) {
            mostrarErro("Primeiro calcule a tabela para poder copiar!");
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Categoria\tFreq. Abs.\tFreq. Abs. Acm.\tFreq. Rel.\tFreq. Rel. Acm.\tFreq. Rel. %\tFreq. Rel. % Acm.\n");
            int totalDados = tabelaFrequencia.getTotalDados();
            int freqAbsAcumulada = 0;
            double freqRelDecimalAcum = 0.0;
            String[] categorias = tabelaFrequencia.getCategorias();
            int[] valores = tabelaFrequencia.getValores();
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
            sb.append(String.format("TOTAL\t%d\t%d\t%.4f\t%.4f\t%.2f%%\t%.2f%%\n",
                    totalDados, totalDados, 1.0, 1.0, 100.0, 100.0));
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
        painelGrafico.limparGrafico();
        painelGrafico.repaint();
    }

    private void mostrarErro(String mensagem) {
        JOptionPane pane = new JOptionPane(mensagem, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Erro");
        dialog.getContentPane().setBackground(COR_FUNDO);
        dialog.setVisible(true);
    }

    private String getTituloGrafico() {
        return (tituloGraficoField != null && !tituloGraficoField.getText().trim().isEmpty()) ? tituloGraficoField.getText().trim() : null;
    }
    private String getDescricaoY() {
        return (descricaoYField != null && !descricaoYField.getText().trim().isEmpty()) ? descricaoYField.getText().trim() : null;
    }
    private String getDescricaoX() {
        return (descricaoXField != null && !descricaoXField.getText().trim().isEmpty()) ? descricaoXField.getText().trim() : null;
    }

    void abrirDialogInserir() {
        JDialog dialog = new JDialog(this, "Inserir Dados em Lote", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);

        String[] colunas = {"Dado", "Quantidade"};
        Object[][] dados = new Object[5][2];
        JTable tabela = new JTable(new javax.swing.table.DefaultTableModel(dados, colunas) {
            @Override
            public boolean isCellEditable(int row, int column) { return true; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : String.class;
            }
        });
        tabela.setRowHeight(22);
        tabela.setFont(new Font("Arial", Font.PLAIN, 12));
        tabela.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(tabela);

        JButton btnAddLinha = new JButton("Adicionar Linha");
        btnAddLinha.setFont(new Font("Arial", Font.PLAIN, 11));
        btnAddLinha.addActionListener(ev -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tabela.getModel();
            model.addRow(new Object[]{"", 1});
        });

        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 12));
        btnConfirmar.addActionListener(ev -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tabela.getModel();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < model.getRowCount(); i++) {
                Object dado = model.getValueAt(i, 0);
                Object qtd = model.getValueAt(i, 1);
                if (dado != null && !dado.toString().trim().isEmpty() && qtd != null) {
                    int n = 1;
                    try { n = Integer.parseInt(qtd.toString()); } catch (Exception ex) { n = 1; }
                    if (sb.length() > 0) sb.append("\n");
                    for (int j = 0; j < n; j++) {
                        if (j > 0) sb.append(", ");
                        sb.append(dado.toString().trim());
                    }
                }
            }
            if (sb.length() > 0) {
                String atual = inputDados.getText().trim();
                if (!atual.isEmpty() && !atual.endsWith("\n")) atual += "\n";
                inputDados.setText(atual + sb.toString());
            }
            dialog.dispose();
        });

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 11));
        btnCancelar.addActionListener(ev -> dialog.dispose());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnAddLinha);
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnConfirmar);

        dialog.setLayout(new BorderLayout());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(painelBotoes, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}