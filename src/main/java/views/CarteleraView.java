package views;

import java.awt.*;
import java.awt.event.ActionListener;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import models.Pelicula;
import views.estilos.Tema;

/** Catálogo de pósteres con acceso a la pantalla de horarios. */
public class CarteleraView extends JPanel {
    private final JTextField buscar = Tema.campo("", 22);
    private final JButton actualizar = Tema.botonSecundario("Actualizar cartelera");
    private final JLabel estado = Tema.mensaje("Cargando cartelera…", Tema.SECUNDARIO);
    private final Rejilla tarjetas = new Rejilla();
    private final List<Pelicula> peliculas = new ArrayList<>();
    private final List<JPanel> paneles = new ArrayList<>();
    private Consumer<Pelicula> horariosListener = pelicula -> {};

    public CarteleraView() {
        setLayout(new BorderLayout(0, 24));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel cabecera = new JPanel(new BorderLayout(0, 16));
        cabecera.setOpaque(false);
        JPanel titulos = new JPanel();
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.PAGE_AXIS));
        titulos.setOpaque(false);
        titulos.add(Tema.texto("PELÍCULAS  /  CARTELERA", Tema.ETIQUETA, Tema.PRIMARIO));
        titulos.add(Box.createVerticalStrut(8));
        titulos.add(Tema.texto("Cartelera", Tema.TITULO, Tema.TEXTO));
        titulos.add(Box.createVerticalStrut(8));
        titulos.add(Tema.texto("Elige una película y encuentra tu próxima función.", Tema.CUERPO, Tema.SECUNDARIO));
        cabecera.add(titulos, BorderLayout.NORTH);
        JPanel filtros = new JPanel(new BorderLayout(12, 0));
        filtros.setOpaque(false);
        JLabel etiqueta = Tema.texto("Buscar película", Tema.ETIQUETA, Tema.TEXTO);
        etiqueta.setLabelFor(buscar);
        buscar.setToolTipText("Buscar por título, con o sin acentos");
        filtros.add(etiqueta, BorderLayout.NORTH);
        filtros.add(buscar, BorderLayout.CENTER);
        filtros.add(actualizar, BorderLayout.SOUTH);
        cabecera.add(filtros, BorderLayout.CENTER);
        cabecera.add(estado, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);
        JScrollPane catalogo = new JScrollPane(tarjetas);
        catalogo.setBorder(BorderFactory.createEmptyBorder());
        catalogo.getViewport().setBackground(Tema.FONDO);
        catalogo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        catalogo.getVerticalScrollBar().setUnitIncrement(24);
        add(catalogo, BorderLayout.CENTER);
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
    }

    public void mostrarCatalogo(List<Pelicula> catalogo) {
        peliculas.clear(); paneles.clear();
        for (Pelicula p : catalogo) {
            if (!"CARTELERA".equals(p.getEstado())) continue;
            peliculas.add(p);
            JPanel tarjeta = Tema.tarjeta();
            tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));
            PosterView poster = new PosterView(p.getImagenUrl(), 190, 270);
            poster.setToolTipText(p.getNombre());
            poster.getAccessibleContext().setAccessibleName("Póster de " + p.getNombre());
            tarjeta.add(poster, BorderLayout.CENTER);
            JButton ver = Tema.botonPrimario("Ver horarios");
            ver.setToolTipText(p.getNombre());
            ver.getAccessibleContext().setAccessibleName("Ver horarios de " + p.getNombre());
            ver.addActionListener(e -> horariosListener.accept(p));
            tarjeta.add(ver, BorderLayout.SOUTH);
            paneles.add(tarjeta);
        }
        filtrar();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private void filtrar() {
        tarjetas.removeAll();
        String filtro = normalizar(buscar.getText());
        for (int i = 0; i < peliculas.size(); i++) {
            if (normalizar(peliculas.get(i).getNombre()).contains(filtro)) tarjetas.add(paneles.get(i));
        }
        int cantidad = tarjetas.getComponentCount();
        estado.setForeground(Tema.SECUNDARIO);
        estado.setText(peliculas.isEmpty() ? "Todavía no hay películas en cartelera."
                : cantidad == 0 ? "No se encontraron películas con ese título."
                : cantidad + (cantidad == 1 ? " película en cartelera" : " películas en cartelera"));
        tarjetas.revalidate(); tarjetas.repaint();
    }

    public void setCargando(boolean cargando) {
        actualizar.setEnabled(!cargando); buscar.setEnabled(!cargando);
        if (cargando) { mostrarCatalogo(List.of()); estado.setText("Cargando cartelera…"); }
    }
    public void mostrarErrorCarga() {
        estado.setForeground(Tema.ERROR);
        estado.setText("No se pudo cargar la cartelera. Pulsa «Actualizar cartelera» para reintentar.");
    }
    public void addActualizarListener(ActionListener listener) { actualizar.addActionListener(listener); }
    public void setHorariosListener(Consumer<Pelicula> listener) { horariosListener = listener; }

    private static final class Rejilla extends JPanel implements Scrollable {
        Rejilla() { setBackground(Tema.FONDO); setLayout(new GridLayout(0, 1, 16, 16)); }
        private int columnas() { return Math.max(1, getWidth() / 240); }
        @Override public void doLayout() { ((GridLayout) getLayout()).setColumns(columnas()); super.doLayout(); }
        @Override public Dimension getPreferredSize() {
            int filas = (getComponentCount() + columnas() - 1) / columnas();
            return new Dimension(600, Math.max(0, filas * 370 - 16));
        }
        public Dimension getPreferredScrollableViewportSize() { return new Dimension(600, 370); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(24, r.height - 24); }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
