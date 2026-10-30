package views.estilos;

import java.awt.*;
import javax.swing.*;

/** Página que se adapta al ancho del módulo y permite alcanzar todos sus controles. */
public class Pagina extends JPanel implements Scrollable {
    public Pagina() {
        super(new BorderLayout(0, 24));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    }
    public JScrollPane conScroll() {
        JScrollPane scroll = new JScrollPane(this);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Tema.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }
    public Dimension getPreferredScrollableViewportSize() { return new Dimension(800, 700); }
    public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
    public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(24, r.height - 24); }
    public boolean getScrollableTracksViewportWidth() { return true; }
    public boolean getScrollableTracksViewportHeight() { return getParent() != null && getParent().getHeight() > getPreferredSize().height; }
}
