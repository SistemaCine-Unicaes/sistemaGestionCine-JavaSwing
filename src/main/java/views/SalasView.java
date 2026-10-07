package views;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import models.Sala;
import views.estilos.*;

/** Catálogo de salas con distribuciones fijas. */
public class SalasView extends JPanel {
    private final JPanel tarjetas = new JPanel(new RejillaAdaptable(3, 260, 16));
    private final JLabel cantidad = Tema.texto("Cargando salas…", Tema.CUERPO, Tema.SECUNDARIO);
    private final JButton actualizar = Tema.botonSecundario("Actualizar salas");
    private Consumer<Sala> abrir = sala -> {};

    public SalasView() {
        super(new BorderLayout()); setBackground(Tema.FONDO);
        Pagina pagina = new Pagina();
        JPanel cabecera = new JPanel(new BorderLayout(0, 12)); cabecera.setOpaque(false);
        cabecera.add(Tema.texto("Gestión de salas", Tema.TITULO, Tema.TEXTO), BorderLayout.NORTH);
        JTextArea ayuda = new JTextArea("Abre una sala para ver su distribución y administrar el mantenimiento de la sala o de sus asientos.");
        ayuda.setLineWrap(true); ayuda.setWrapStyleWord(true); ayuda.setEditable(false);
        ayuda.setOpaque(false); ayuda.setFont(Tema.CUERPO); ayuda.setForeground(Tema.SECUNDARIO); ayuda.setRows(2);
        cabecera.add(ayuda, BorderLayout.CENTER); cabecera.add(actualizar, BorderLayout.SOUTH);
        pagina.add(cabecera, BorderLayout.NORTH); tarjetas.setOpaque(false);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 16)); cuerpo.setOpaque(false);
        cuerpo.add(tarjetas, BorderLayout.NORTH); cuerpo.add(cantidad, BorderLayout.SOUTH);
        pagina.add(cuerpo, BorderLayout.CENTER); add(pagina.conScroll(), BorderLayout.CENTER);
    }

    public void mostrarSalas(List<Sala> salas) {
        tarjetas.removeAll();
        for (Sala sala : salas) {
            JPanel tarjeta = Tema.tarjeta();
            JLabel titulo = Tema.texto("Sala " + sala.getIdSala() + " · " + sala.getNombre().replaceFirst("^Sala ", ""), Tema.SUBTITULO, Tema.TEXTO);
            titulo.setToolTipText(titulo.getText()); tarjeta.add(titulo, BorderLayout.NORTH);
            JPanel datos = new JPanel(new GridLayout(0,1,0,10)); datos.setOpaque(false);
            datos.add(Tema.texto(sala.getCapacidadTotal() + " asientos · " + sala.getAsientosEspeciales() + " especiales", Tema.CUERPO, Tema.SECUNDARIO));
            datos.add(Tema.texto("Limpieza: " + sala.getTiempoDeLimpieza() + " min", Tema.CUERPO, Tema.SECUNDARIO));
            datos.add(Tema.texto("ACTIVA".equals(sala.getEstado()) ? "Activa" : "Desactivada · mantenimiento", Tema.ETIQUETA,
                    "ACTIVA".equals(sala.getEstado()) ? Tema.EXITO : Tema.ERROR));
            JLabel motivo = Tema.texto(sala.getMotivoInactividad() == null ? " " : sala.getMotivoInactividad(), Tema.CUERPO, Tema.SECUNDARIO);
            motivo.setToolTipText(motivo.getText()); datos.add(motivo); tarjeta.add(datos, BorderLayout.CENTER);
            JButton ver = Tema.botonPrimario("Ver sala y asientos");
            ver.getAccessibleContext().setAccessibleName("Abrir sala " + sala.getIdSala());
            ver.addActionListener(e -> abrir.accept(sala)); tarjeta.add(ver, BorderLayout.SOUTH);
            java.awt.event.MouseAdapter clic = new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { abrir.accept(sala); }
            };
            tarjeta.addMouseListener(clic); titulo.addMouseListener(clic); datos.addMouseListener(clic);
            for (Component dato : datos.getComponents()) dato.addMouseListener(clic);
            tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); tarjetas.add(tarjeta);
        }
        cantidad.setText(salas.isEmpty() ? "No hay salas. Instala el catálogo predefinido en la base de datos." : salas.size() + " salas registradas");
        tarjetas.revalidate(); tarjetas.repaint();
    }
    public void alAbrir(Consumer<Sala> listener) { abrir = listener; }
    public void addActualizarListener(ActionListener listener) { actualizar.addActionListener(listener); }
}
