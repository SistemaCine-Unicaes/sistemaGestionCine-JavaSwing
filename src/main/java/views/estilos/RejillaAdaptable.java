package views.estilos;

import java.awt.*;

/** Recalcula columnas y altura preferida usando el ancho disponible. */
public class RejillaAdaptable extends GridLayout {
    private final int maxColumnas;
    private final int anchoColumna;
    public RejillaAdaptable(int maxColumnas, int anchoColumna, int espacio) {
        super(0, maxColumnas, espacio, espacio);
        this.maxColumnas = maxColumnas;
        this.anchoColumna = anchoColumna;
    }
    private void adaptar(Container padre) {
        Insets i = padre.getInsets();
        int ancho = padre.getWidth() - i.left - i.right;
        if (ancho <= 0) ancho = maxColumnas * (anchoColumna + getHgap());
        setColumns(Math.max(1, Math.min(maxColumnas, (ancho + getHgap()) / (anchoColumna + getHgap()))));
    }
    @Override public Dimension preferredLayoutSize(Container padre) { adaptar(padre); return super.preferredLayoutSize(padre); }
    @Override public void layoutContainer(Container padre) { adaptar(padre); super.layoutContainer(padre); }
}
