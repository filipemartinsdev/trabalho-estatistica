package estatistica;

import javax.swing.*;
import java.awt.*;

public class UtilitariosUI {
    public static void estilizarBotao(JButton botao, Color corFundo, Color corTexto) {
        botao.setFont(new Font("Arial", Font.BOLD, 12));
        botao.setBackground(corFundo);
        botao.setForeground(corTexto);
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
    public static void estilizarCheckbox(JCheckBox checkbox, Color corFundo, Color corTexto) {
        checkbox.setFont(new Font("Arial", Font.PLAIN, 12));
        checkbox.setBackground(corFundo);
        checkbox.setForeground(corTexto);
        checkbox.setFocusPainted(false);
    }
}
