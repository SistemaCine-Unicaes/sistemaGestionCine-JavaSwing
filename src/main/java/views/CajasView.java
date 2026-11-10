package views;

import dao.CajaDAO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.*;
import views.estilos.*;

/**
 * Módulo de administración para ver el catálogo y estado de las cajas del cine,
 * con tarjetas interactivas de cada caja física similares a la vista de salas.
 */
public class CajasView extends JPanel {

    private final JPanel tarjetas = new JPanel(new RejillaAdaptable(3, 260, 16));
    private final JLabel cantidad = Tema.texto("Cargando cajas…", Tema.CUERPO, Tema.SECUNDARIO);
    private final JButton actualizar = Tema.botonSecundario("Actualizar cajas");
    private final JButton btnReporte = Tema.botonPrimario("Ver Reporte de Cajas");

    private Consumer<CajaDAO.InfoCaja> abrirReporte = caja -> {};
    private Consumer<CajaDAO.InfoCaja> liberarCaja = caja -> {};

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DateTimeFormatter HORA_MIN = DateTimeFormatter.ofPattern("HH:mm");

    public CajasView() {
        super(new BorderLayout());
        setBackground(Tema.FONDO);

        Pagina pagina = new Pagina();
        JPanel cabecera = new JPanel(new BorderLayout(0, 12));
        cabecera.setOpaque(false);

        cabecera.add(Tema.texto("Gestión de cajas", Tema.TITULO, Tema.TEXTO), BorderLayout.NORTH);

        JTextArea ayuda = new JTextArea("Monitorea las cajas registradoras en tiempo real, verifica el cajero asignado y consulta el historial de ventas y transacciones por caja física.");
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setEditable(false);
        ayuda.setOpaque(false);
        ayuda.setFont(Tema.CUERPO);
        ayuda.setForeground(Tema.SECUNDARIO);
        ayuda.setRows(2);
        cabecera.add(ayuda, BorderLayout.CENTER);

        // Agrupación de botones en la cabecera
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(actualizar);
        panelBotones.add(Box.createHorizontalStrut(16));
        panelBotones.add(btnReporte);
        cabecera.add(panelBotones, BorderLayout.SOUTH);

        pagina.add(cabecera, BorderLayout.NORTH);

        tarjetas.setOpaque(false);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 16));
        cuerpo.setOpaque(false);
        cuerpo.add(tarjetas, BorderLayout.NORTH);
        cuerpo.add(cantidad, BorderLayout.SOUTH);

        pagina.add(cuerpo, BorderLayout.CENTER);
        add(pagina.conScroll(), BorderLayout.CENTER);
    }

    public void mostrarCajas(List<CajaDAO.InfoCaja> cajas) {
        tarjetas.removeAll();
        long cajasOcupadas = 0;

        for (CajaDAO.InfoCaja caja : cajas) {
            boolean ocupada = "OCUPADA".equalsIgnoreCase(caja.estado());
            boolean mantenimiento = "MANTENIMIENTO".equalsIgnoreCase(caja.estado());
            if (ocupada) cajasOcupadas++;

            JPanel tarjeta = Tema.tarjeta();

            // Título de la tarjeta
            JLabel titulo = Tema.texto(caja.nombre(), Tema.SUBTITULO, Tema.TEXTO);
            titulo.setToolTipText(titulo.getText());
            tarjeta.add(titulo, BorderLayout.NORTH);

            // Datos del estado y operador
            JPanel datos = new JPanel(new GridLayout(0, 1, 0, 8));
            datos.setOpaque(false);

            // Estado
            String textoEstado = ocupada ? "Ocupada · En atención" : mantenimiento ? "En mantenimiento" : "Disponible";
            Color colorEstado = ocupada ? Tema.PRIMARIO : mantenimiento ? Tema.AVISO : Tema.EXITO;
            datos.add(Tema.texto(textoEstado, Tema.ETIQUETA, colorEstado));

            // Operador
            String operador = caja.nombreUsuarioActual() != null && !caja.nombreUsuarioActual().isBlank()
                    ? "Operador: " + caja.nombreUsuarioActual()
                    : "Sin operador activo";
            JLabel lblOp = Tema.texto(operador, Tema.CUERPO, Tema.TEXTO);
            lblOp.setToolTipText(lblOp.getText());
            datos.add(lblOp);

            // Horario de apertura
            String apertura = caja.fechaApertura() != null
                    ? "Apertura hoy: " + formatoHora(caja.fechaApertura())
                    : "Sesión: Inactiva";
            datos.add(Tema.texto(apertura, Tema.CUERPO, Tema.SECUNDARIO));

            // Ventas de hoy
            String ventasHoy = "Hoy: " + caja.ticketsHoy() + " tickets · " + MONEDA.format(caja.recaudacionHoy());
            datos.add(Tema.texto(ventasHoy, Tema.CUERPO, Tema.SECUNDARIO));

            tarjeta.add(datos, BorderLayout.CENTER);

            // Botones de acción
            JPanel pnlAcciones = new JPanel(new GridLayout(ocupada ? 2 : 1, 1, 0, 6));
            pnlAcciones.setOpaque(false);

            JButton ver = Tema.botonPrimario("Ver reporte de caja");
            ver.getAccessibleContext().setAccessibleName("Abrir reporte de " + caja.nombre());
            ver.addActionListener(e -> abrirReporte.accept(caja));
            pnlAcciones.add(ver);

            if (ocupada) {
                JButton btnLiberar = Tema.botonSecundario("Liberar caja");
                btnLiberar.addActionListener(e -> liberarCaja.accept(caja));
                pnlAcciones.add(btnLiberar);
            }

            tarjeta.add(pnlAcciones, BorderLayout.SOUTH);

            // Clic sobre la tarjeta completa
            java.awt.event.MouseAdapter clic = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    abrirReporte.accept(caja);
                }
            };
            tarjeta.addMouseListener(clic);
            titulo.addMouseListener(clic);
            datos.addMouseListener(clic);
            for (Component dato : datos.getComponents()) dato.addMouseListener(clic);
            tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            tarjetas.add(tarjeta);
        }

        cantidad.setText(cajas.isEmpty()
                ? "No hay cajas configuradas."
                : cajas.size() + " cajas físicas registradas (" + cajasOcupadas + " en atención actualmente)");

        tarjetas.revalidate();
        tarjetas.repaint();
    }

    private static String formatoHora(Timestamp ts) {
        if (ts == null) return "-";
        return HORA_MIN.format(ts.toLocalDateTime());
    }

    public void alAbrirReporte(Consumer<CajaDAO.InfoCaja> listener) {
        this.abrirReporte = listener;
    }

    public void alLiberarCaja(Consumer<CajaDAO.InfoCaja> listener) {
        this.liberarCaja = listener != null ? listener : caja -> {};
    }

    public void addActualizarListener(ActionListener listener) {
        actualizar.addActionListener(listener);
    }

    public void addReporteGeneralListener(ActionListener listener) {
        btnReporte.addActionListener(listener);
    }
}
