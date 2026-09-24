package views.estilos;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;

/** Punto compartido de colores, medidas y componentes. No cambia el tema global de Swing. */
public final class Tema {
    public static final Color FONDO = Color.decode("#F4F5F7");
    public static final Color SUPERFICIE = Color.WHITE;
    public static final Color TEXTO = Color.decode("#182235");
    public static final Color SECUNDARIO = Color.decode("#586579");
    public static final Color BORDE = Color.decode("#DCE1E8");
    public static final Color PRIMARIO = Color.decode("#B4233C");
    public static final Color EXITO = Color.decode("#18734C");
    public static final Color AVISO = Color.decode("#8A5800");
    public static final Color ERROR = Color.decode("#B42318");
    public static final int ESPACIO = 8;
    public static final int SEPARACION = 16;
    public static final int MARGEN = 24;
    public static final int ALTO_CONTROL = 40;
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 28);
    public static final Font SUBTITULO = new Font("SansSerif", Font.BOLD, 18);
    public static final Font CUERPO = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font ETIQUETA = new Font("SansSerif", Font.BOLD, 12);

    private Tema() { }

    public static JLabel texto(String texto, Font fuente, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente);
        etiqueta.setForeground(color);
        return etiqueta;
    }

    public static JPanel tarjeta() {
        JPanel panel = new JPanel(new BorderLayout(SEPARACION, SEPARACION));
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(MARGEN, MARGEN, MARGEN, MARGEN)));
        return panel;
    }

    public static JButton botonPrimario(String texto) {
        return boton(texto, PRIMARIO, Color.WHITE);
    }

    public static JButton botonSecundario(String texto) {
        return boton(texto, SUPERFICIE, TEXTO);
    }

    private static JButton boton(String texto, Color fondo, Color frente) {
        JButton boton = new JButton(texto);
        boton.setUI(new BasicButtonUI());
        boton.setFont(CUERPO.deriveFont(Font.BOLD));
        boton.setBackground(fondo);
        boton.setForeground(frente);
        boton.setOpaque(true);
        boton.setRolloverEnabled(true);
        boton.setFocusPainted(true);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fondo.equals(SUPERFICIE) ? BORDE : fondo, 2),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        boton.addChangeListener(e -> {
            boolean activo = boton.isEnabled() && (boton.getModel().isRollover() || boton.getModel().isPressed());
            boton.setBackground(!boton.isEnabled() ? BORDE : activo ? fondo.darker() : fondo);
        });
        return boton;
    }

    public static JTextField campo(String valor, int columnas) {
        JTextField campo = new JTextField(valor, columnas);
        campo.setFont(CUERPO);
        campo.setForeground(TEXTO);
        campo.setBackground(SUPERFICIE);
        campo.setCaretColor(PRIMARIO);
        campo.setPreferredSize(new Dimension(campo.getPreferredSize().width, ALTO_CONTROL));
        campo.setBorder(bordeCampo(BORDE));
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { campo.setBorder(bordeCampo(PRIMARIO)); }
            @Override public void focusLost(FocusEvent e) { campo.setBorder(bordeCampo(BORDE)); }
        });
        return campo;
    }

    private static Border bordeCampo(Color color) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    public static JScrollPane tabla(JTable tabla) {
        tabla.setFont(CUERPO);
        tabla.setForeground(TEXTO);
        tabla.setBackground(SUPERFICIE);
        tabla.setRowHeight(44);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(Color.decode("#FBE9ED"));
        tabla.setSelectionForeground(TEXTO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(ETIQUETA);
        tabla.getTableHeader().setForeground(SECUNDARIO);
        tabla.getTableHeader().setBackground(FONDO);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionado, boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(t, valor, seleccionado, foco, fila, columna);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                if (!seleccionado) setBackground(fila % 2 == 0 ? SUPERFICIE : FONDO);
                if (foco) setBorder(BorderFactory.createLineBorder(PRIMARIO, 2));
                return this;
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(SUPERFICIE);
        return scroll;
    }

    public static JLabel mensaje(String texto, Color color) {
        JLabel mensaje = texto(texto, CUERPO, color);
        mensaje.setOpaque(true);
        mensaje.setBackground(FONDO);
        mensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, color),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return mensaje;
    }
}
