package views.estilos;

import java.awt.*;
import javax.swing.*;

/** Ajustes compartidos para páginas, tablas y ventanas pequeñas. */
public final class Adaptable {
    private Adaptable() {}
    public static void envolver(JScrollPane scroll, JComponent contenido) {
        Pagina pagina = new Pagina();
        pagina.setBorder(BorderFactory.createEmptyBorder());
        pagina.add(contenido, BorderLayout.NORTH);
        scroll.setViewportView(pagina);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
    }
    public static void limitarVentana(Window ventana, int ancho, int alto) {
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        ventana.setMinimumSize(new Dimension(Math.min(360, pantalla.width), Math.min(300, pantalla.height)));
        ventana.setSize(Math.min(ancho, pantalla.width), Math.min(alto, pantalla.height));
        ventana.setLocationRelativeTo(ventana.getOwner());
    }
    public static void columnas(JPanel panel, int maximo, int ancho, int espacio) {
        panel.setLayout(new RejillaAdaptable(maximo, ancho, espacio));
    }
}
