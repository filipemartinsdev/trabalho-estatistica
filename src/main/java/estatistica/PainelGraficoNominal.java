package estatistica;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;

public class PainelGraficoNominal extends JPanel {
    private String tipoGrafico = "";
    private java.util.List<Rectangle> colunasBounds = new ArrayList<>();
    private Color[] cores;
    private final TabelaFrequencia tabelaFrequencia;
    private final java.util.function.Supplier<String> tituloSupplier;
    private final java.util.function.Supplier<String> descricaoYSupplier;
    private final java.util.function.Supplier<String> descricaoXSupplier;

    public PainelGraficoNominal(TabelaFrequencia tabelaFrequencia, java.util.function.Supplier<String> tituloSupplier, java.util.function.Supplier<String> descricaoYSupplier, java.util.function.Supplier<String> descricaoXSupplier) {
        this.tabelaFrequencia = tabelaFrequencia;
        this.tituloSupplier = tituloSupplier;
        this.descricaoYSupplier = descricaoYSupplier;
        this.descricaoXSupplier = descricaoXSupplier;
        setBackground(new Color(255, 255, 255));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String[] categorias = tabelaFrequencia.getCategorias();
                if (categorias == null || categorias.length == 0) return;
                for (int i = 0; i < colunasBounds.size(); i++) {
                    Rectangle r = colunasBounds.get(i);
                    if (r != null && r.contains(e.getPoint())) {
                        if (cores == null) return;
                        Color nova = JColorChooser.showDialog(PainelGraficoNominal.this, "Escolha a cor da categoria: " + categorias[i], cores[i]);
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
        String[] categorias = tabelaFrequencia.getCategorias();
        if (categorias != null && categorias.length > 0) {
            int larguraMinima = Math.max(80, categorias.length * 100);
            int alturaMinima = 400;
            setPreferredSize(new Dimension(larguraMinima, alturaMinima));
            revalidate();
        }
    }
    private void gerarCores() {
        String[] categorias = tabelaFrequencia.getCategorias();
        if (categorias == null) return;
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
        setBackground(new Color(255, 255, 255));
        String[] categorias = tabelaFrequencia.getCategorias();
        int[] valores = tabelaFrequencia.getValores();
        int totalDados = tabelaFrequencia.getTotalDados();
        if (cores == null || (categorias != null && cores.length != categorias.length)) gerarCores();
        if (categorias == null || categorias.length == 0 || tipoGrafico.isEmpty()) {
            g.setColor(new Color(33, 37, 41));
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
        double[] valoresGrafico = new double[categorias.length];
        if (tipoGrafico.equals("Fi")) {
            for (int i = 0; i < valores.length; i++) valoresGrafico[i] = valores[i];
        } else if (tipoGrafico.equals("Fr")) {
            for (int i = 0; i < valores.length; i++) valoresGrafico[i] = (valores[i] * 100.0) / totalDados;
        }
        double maxEscala = calcularEscalaMaxima(valoresGrafico);
        int numMarcas = 10;
        double[] valoresEscala = gerarValoresEscala(maxEscala, numMarcas);
        g2d.setColor(new Color(33, 37, 41));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margem, margem, margem, getHeight() - margem - 40);
        g2d.drawLine(margem, getHeight() - margem - 40, getWidth() - margem, getHeight() - margem - 40);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= numMarcas; i++) {
            double valor = valoresEscala[i];
            double proporcao = valor / maxEscala;
            int yPos = getHeight() - margem - 40 - (int)(proporcao * alturaGrafico);
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, 0));
            if (i > 0) g2d.drawLine(margem, yPos, getWidth() - margem, yPos);
            g2d.setColor(new Color(33, 37, 41));
            g2d.setStroke(new BasicStroke(1));
            String label;
            if (tipoGrafico.equals("Fr")) {
                label = String.format(valor == (int)valor ? "%.0f%%" : "%.1f%%", valor);
            } else {
                label = String.format(valor == (int)valor ? "%.0f" : "%.1f", valor);
            }
            g2d.drawString(label, margem - 30, yPos + 4);
            g2d.drawLine(margem - 5, yPos, margem, yPos);
        }
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
            g2d.setColor(cores[i]);
            g2d.fillRect(x, y, larguraColuna, alturaColuna);
            g2d.setColor(new Color(33, 37, 41));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(x, y, larguraColuna, alturaColuna);
            g2d.setColor(new Color(33, 37, 41));
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
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.setColor(new Color(33, 37, 41));
            String categoria = categorias[i];
            if (categoria.length() > 12) categoria = categoria.substring(0, 10) + "..";
            AffineTransform original = g2d.getTransform();
            int labelX = x + larguraColuna / 2;
            int labelY = getHeight() - margem - 24;
            int labelWidth = g2d.getFontMetrics().stringWidth(categoria);
            g2d.drawString(categoria, labelX - labelWidth/2, labelY);
            while (colunasBounds.size() <= i) colunasBounds.add(null);
            colunasBounds.set(i, new Rectangle(x, y, larguraColuna, Math.max(alturaColuna, 2)));
        }
        g2d.setColor(new Color(33, 37, 41));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String tituloDefault = tipoGrafico.equals("Fi") ?
            "Gráfico de Frequência Absoluta (Fi)" :
            "Gráfico de Frequência Relativa (Fr %)";
        String titulo = tituloSupplier.get();
        if (titulo == null || titulo.isEmpty()) titulo = tituloDefault;
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(titulo);
        g2d.drawString(titulo, (getWidth() - titleWidth) / 2, 25);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String labelY = descricaoYSupplier.get();
        if (labelY == null || labelY.isEmpty()) labelY = tipoGrafico.equals("Fi") ? "Frequência Absoluta" : "Frequência Relativa (%)";
        AffineTransform original = g2d.getTransform();
        g2d.rotate(Math.toRadians(-90), 15, getHeight() / 2);
        g2d.drawString(labelY, 15, getHeight() / 2);
        g2d.setTransform(original);
        String labelX = descricaoXSupplier.get();
        if (labelX == null || labelX.isEmpty()) labelX = "Categorias";
        g2d.drawString(labelX, getWidth() / 2 - Math.max(40, g2d.getFontMetrics().stringWidth(labelX)/2), getHeight()-60);
        if (categorias.length <= 8) desenharLegenda(g2d, categorias, valores, totalDados);
    }
    public void exportarGrafico() {
        String[] categorias = tabelaFrequencia.getCategorias();
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
            if (!arquivo.getName().toLowerCase().endsWith(".png")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".png");
            }
            try {
                BufferedImage imagem = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = imagem.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                paint(g2d);
                g2d.dispose();
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
    private void desenharLegenda(Graphics2D g2d, String[] categorias, int[] valores, int totalDados) {
        int legendaX = getWidth() - 160;
        int legendaY = 60;
        int alturaItem = 18;
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(legendaX - 10, legendaY - 25, 150, categorias.length * alturaItem + 30, 5, 5);
        g2d.setColor(new Color(222, 226, 230));
        g2d.drawRoundRect(legendaX - 10, legendaY - 25, 150, categorias.length * alturaItem + 30, 5, 5);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(33, 37, 41));
        g2d.drawString("Legenda", legendaX, legendaY - 10);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i < Math.min(categorias.length, 8); i++) {
            int y = legendaY + i * alturaItem;
            g2d.setColor(cores[i]);
            g2d.fillRect(legendaX, y, 14, 14);
            g2d.setColor(new Color(33, 37, 41));
            g2d.drawRect(legendaX, y, 14, 14);
            String categoria = categorias[i];
            if (categoria.length() > 18) categoria = categoria.substring(0, 15) + "...";
            String valorFreq = tipoGrafico.equals("Fr") ?
                    String.format(" (%.1f%%)", (valores[i] * 100.0) / totalDados) :
                    String.format(" (%d)", valores[i]);
            String textoLegenda = categoria + valorFreq;
            if (textoLegenda.length() > 22) {
                textoLegenda = categoria.substring(0, Math.max(1, 18 - valorFreq.length())) + "..." + valorFreq;
            }
            g2d.drawString(textoLegenda, legendaX + 20, y + 11);
        }
    }
}
