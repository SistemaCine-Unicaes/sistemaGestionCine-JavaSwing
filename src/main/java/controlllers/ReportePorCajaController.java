package controlllers;

import config.Sesion;
import dao.ReporteCajaDetalladoDAO;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import utils.ReportePorCajaPDF;
import utils.Tareas;
import views.ReportePorCajaView;

/**
 * Controlador de la vista de reporte de uso y ventas por caja física con rango de fechas.
 */
public class ReportePorCajaController {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.US);

    private final ReportePorCajaView vista;
    private final ReporteCajaDetalladoDAO dao;
    private ReporteCajaDetalladoDAO.ReporteCajas ultimoReporte;
    private String ultimoFiltroCaja = "Todas las cajas";
    private String ultimoPeriodoTexto = "Últimos 30 días";
    private boolean silenciarEventos = false;

    public ReportePorCajaController(ReportePorCajaView vista) {
        this(vista, new ReporteCajaDetalladoDAO());
    }

    public ReportePorCajaController(ReportePorCajaView vista, ReporteCajaDetalladoDAO dao) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        this.dao = dao;

        silenciarEventos = true;
        cargarComboCajas();
        initEventos();
        silenciarEventos = false;

        ejecutarFiltro();
    }

    private void cargarComboCajas() {
        silenciarEventos = true;
        try {
            List<String> nombres = dao.obtenerNombresCajas();
            vista.getCmbCajas().removeAllItems();
            vista.getCmbCajas().addItem("Todas las cajas");
            for (String n : nombres) {
                vista.getCmbCajas().addItem(n);
            }
        } catch (Exception ignored) {
        } finally {
            silenciarEventos = false;
        }
    }

    private void initEventos() {
        // Al cambiar cualquier selector de combo box, el reporte se actualiza automáticamente
        vista.getCmbCajas().addActionListener(e -> {
            if (!silenciarEventos) {
                ejecutarFiltro();
            }
        });

        vista.getCmbPeriodo().addActionListener(e -> {
            if (!silenciarEventos) {
                ejecutarFiltro();
            }
        });

        vista.getBtnFiltrar().addActionListener(e -> {
            if (!silenciarEventos) {
                ejecutarFiltro();
            }
        });

        vista.getBtnExportarPDF().addActionListener(e -> {
            if (ultimoReporte == null) {
                JOptionPane.showMessageDialog(vista, "No hay datos cargados para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String usuario = Sesion.getUsuarioActual() != null ? Sesion.getUsuarioActual().getNombre() : "Administrador";
            ReportePorCajaPDF.exportarInteractivo(vista, ultimoReporte, ultimoFiltroCaja, ultimoPeriodoTexto, usuario);
        });
    }

    public static LocalDate[] calcularRangoPeriodo(String preset) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde;
        LocalDate hasta;

        if (preset == null) {
            preset = "Últimos 30 días";
        }

        switch (preset) {
            case "Hoy" -> {
                desde = hoy;
                hasta = hoy;
            }
            case "Este mes" -> {
                desde = hoy.withDayOfMonth(1);
                hasta = hoy.withDayOfMonth(hoy.lengthOfMonth());
            }
            case "Último mes" -> {
                LocalDate mesPasado = hoy.minusMonths(1);
                desde = mesPasado.withDayOfMonth(1);
                hasta = mesPasado.withDayOfMonth(mesPasado.lengthOfMonth());
            }
            case "Próximos 30 días" -> {
                desde = hoy;
                hasta = hoy.plusDays(30);
            }
            case "Todo el historial" -> {
                desde = LocalDate.of(2020, 1, 1);
                hasta = hoy.plusYears(1);
            }
            default -> { // "Últimos 30 días"
                desde = hoy.minusDays(30);
                hasta = hoy;
            }
        }

        return new LocalDate[]{desde, hasta};
    }

    public void filtrarPorCaja(String nombreCaja) {
        silenciarEventos = true;
        try {
            vista.setCajaSeleccionada(nombreCaja);
        } finally {
            silenciarEventos = false;
        }
        ejecutarFiltro();
    }

    public static LocalDate[] validarRango(String textoDesde, String textoHasta) {
        LocalDate desde;
        LocalDate hasta;

        try {
            desde = LocalDate.parse(textoDesde.trim(), FECHA);
        } catch (Exception e) {
            throw new IllegalArgumentException("La fecha 'Desde' no es válida. Usa el formato dd/mm/aaaa.");
        }

        try {
            hasta = LocalDate.parse(textoHasta.trim(), FECHA);
        } catch (Exception e) {
            throw new IllegalArgumentException("La fecha 'Hasta' no es válida. Usa el formato dd/mm/aaaa.");
        }

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
        }

        return new LocalDate[]{desde, hasta};
    }

    private void ejecutarFiltro() {
        Object selPeriodo = vista.getCmbPeriodo().getSelectedItem();
        String preset = selPeriodo != null ? selPeriodo.toString() : "Últimos 30 días";
        LocalDate[] rango = calcularRangoPeriodo(preset);
        LocalDate desde = rango[0];
        LocalDate hasta = rango[1];

        // Sincronizar campos auxiliares para compatibilidad de getters
        vista.getTxtFechaDesde().setText(desde.format(FECHA));
        vista.getTxtFechaHasta().setText(hasta.format(FECHA));

        Object selCaja = vista.getCmbCajas().getSelectedItem();
        String caja = selCaja != null ? selCaja.toString() : "Todas las cajas";
        LocalDate hastaExclusiva = hasta.plusDays(1);
        String periodoTexto = preset.equalsIgnoreCase("Todo el historial")
                ? "Todo el historial"
                : preset + " (" + desde.format(FECHA) + " al " + hasta.format(FECHA) + ")";

        if (!vista.isShowing() || java.awt.GraphicsEnvironment.isHeadless()) {
            ultimoReporte = dao.obtenerUsoPorCaja(caja, desde, hastaExclusiva);
            ultimoFiltroCaja = caja;
            ultimoPeriodoTexto = periodoTexto;
            actualizarTablaYTotales(ultimoReporte);
            return;
        }

        vista.getLblResumenTotales().setText("Consultando datos de cajas...");
        javax.swing.SwingWorker<ReporteCajaDetalladoDAO.ReporteCajas, Void> worker = new javax.swing.SwingWorker<>() {
            @Override
            protected ReporteCajaDetalladoDAO.ReporteCajas doInBackground() {
                Sesion.exigirAdministrador();
                return dao.obtenerUsoPorCaja(caja, desde, hastaExclusiva);
            }

            @Override
            protected void done() {
                try {
                    ultimoReporte = get();
                    ultimoFiltroCaja = caja;
                    ultimoPeriodoTexto = periodoTexto;
                    actualizarTablaYTotales(ultimoReporte);
                } catch (Exception e) {
                    vista.getLblResumenTotales().setText("No se pudieron cargar los datos: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void actualizarTablaYTotales(ReporteCajaDetalladoDAO.ReporteCajas reporte) {
        DefaultTableModel modelo = (DefaultTableModel) vista.getTblReporte().getModel();
        modelo.setRowCount(0);

        for (ReporteCajaDetalladoDAO.UsoCaja f : reporte.filas()) {
            modelo.addRow(new Object[]{
                    f.nombreCaja(),
                    f.nombreUsuario(),
                    f.rol(),
                    f.ticketsVendidos(),
                    dinero(f.totalRecaudado()),
                    formatoFecha(f.primeraVenta()),
                    formatoFecha(f.ultimaVenta())
            });
        }

        vista.getLblResumenTotales().setText(String.format(
                "Tickets vendidos: %d  |  Recaudación: %s  |  Cajas con ventas: %d  |  Operadores: %d",
                reporte.totalTickets(), dinero(reporte.totalRecaudado()), reporte.cajasOperadas(), reporte.usuariosDistintos()
        ));
    }

    private static String dinero(BigDecimal monto) {
        return monto == null ? "$0.00" : MONEDA.format(monto);
    }

    private static String formatoFecha(Timestamp ts) {
        if (ts == null) return "-";
        return FECHA_HORA.format(ts.toLocalDateTime());
    }
}
