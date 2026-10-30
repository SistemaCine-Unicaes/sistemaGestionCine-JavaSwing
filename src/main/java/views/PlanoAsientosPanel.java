package views;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import models.Asiento;
import views.estilos.Tema;

/** Mismo plano en mantenimiento y venta. El ancho se adapta sin cambiar las posiciones. */
public class PlanoAsientosPanel extends JPanel implements Scrollable {
    private final Map<Component, Point> posiciones = new LinkedHashMap<>();
    public PlanoAsientosPanel() { setLayout(null); setBackground(Tema.FONDO); }
    public void colocar(JComponent boton, Asiento asiento, int filaOriginal) {
        posiciones.put(boton,new Point(asiento.getColumnaPlano(), asiento.getFilaPlano() < 0 ? filaOriginal : asiento.getFilaPlano()));
        add(boton);
    }
    @Override public void removeAll() { posiciones.clear(); super.removeAll(); }
    private int columnas() { return posiciones.values().stream().mapToInt(p -> p.x+1).max().orElse(1); }
    private int filas() { return posiciones.values().stream().mapToInt(p -> p.y+1).max().orElse(1); }
    private int paso() {
        int ancho = getParent() instanceof JViewport v ? v.getWidth() : getWidth();
        return Math.max(40,Math.min(64,(ancho-24)/columnas()));
    }
    @Override public Dimension getPreferredSize() { return new Dimension(columnas()*paso()+24, filas()*paso()+24); }
    @Override public void doLayout() {
        int paso = paso(); int inicio = Math.max(12,(getWidth()-columnas()*paso())/2);
        posiciones.forEach((componente,posicion) -> {
            componente.setBounds(inicio+posicion.x*paso,12+posicion.y*paso,paso-6,paso-6);
            componente.setFont(Tema.ETIQUETA.deriveFont(paso < 48 ? 10f : 12f));
        });
    }
    public Dimension getPreferredScrollableViewportSize() { return new Dimension(760,430); }
    public int getScrollableUnitIncrement(Rectangle r,int o,int d) { return 40; }
    public int getScrollableBlockIncrement(Rectangle r,int o,int d) { return Math.max(40,(o==SwingConstants.HORIZONTAL?r.width:r.height)-40); }
    public boolean getScrollableTracksViewportWidth() { return getParent()!=null && getParent().getWidth()>=columnas()*40+24; }
    public boolean getScrollableTracksViewportHeight() { return false; }
}
