package controlllers;

import config.Conexion;
import config.Sesion;
import dao.ReportesDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import utils.Tareas;
import views.CorteCajaView;

public class CorteCajaController {
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final CorteCajaView vista;
    // El PDF exporta exactamente el corte que está en pantalla.
    private ReportesDAO.Reporte mostrado;
    private LocalDate[] rangoMostrado;
    private String tipoMostrado;

    public CorteCajaController(CorteCajaView vista) {
        Sesion.exigirAdministrador(); this.vista = vista;
        vista.setFecha(LocalDate.now(java.time.ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador"))).format(FECHA));
        vista.addGenerarReporteListener(e -> Tareas.validar(vista, this::generar));
        vista.addExportarPdfListener(e -> Tareas.validar(vista, this::exportarPdf));
        generar();
    }

    public static LocalDate[] periodo(LocalDate fecha, String tipo) {
        return switch (tipo) {
            case "Diario" -> new LocalDate[]{fecha, fecha.plusDays(1)};
            case "Mensual" -> new LocalDate[]{fecha.withDayOfMonth(1), fecha.withDayOfMonth(1).plusMonths(1)};
            case "Anual" -> new LocalDate[]{fecha.withDayOfYear(1), fecha.withDayOfYear(1).plusYears(1)};
            default -> throw new IllegalArgumentException("Tipo de reporte inválido.");
        };
    }

    private void generar() {
        Sesion.exigirAdministrador();
        LocalDate fecha;
        try { fecha = LocalDate.parse(vista.getFecha().trim(), FECHA); }
        catch (java.time.DateTimeException e) { throw new IllegalArgumentException("Ingresa una fecha válida con formato dd/mm/aaaa."); }
        String tipo = vista.getTipoReporte();
        LocalDate[] rango = periodo(fecha, tipo);
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) { return new ReportesDAO(c).obtenerReporte(rango[0], rango[1]); }
        }, reporte -> {
            mostrado = reporte; rangoMostrado = rango; tipoMostrado = tipo;
            vista.setPeriodo("Periodo " + tipo.toLowerCase() + ": del " + rango[0].format(FECHA)
                    + " al " + rango[1].minusDays(1).format(FECHA));
            BigDecimal total = reporte.total();
            vista.setTicketsVendidos(String.valueOf(reporte.tickets()));
            vista.setIngresos(dinero(total), reporte.tickets() == 0 ? "$0.00"
                    : dinero(total.divide(BigDecimal.valueOf(reporte.tickets()), 2, RoundingMode.HALF_UP)), reporte.cajeros().size());
            DefaultTableModel tabla = (DefaultTableModel) vista.getTablaVentas().getModel();
            tabla.setRowCount(0);
            List<ReportesDAO.VentaCajero> filas = reporte.cajeros().stream()
                    .sorted(Comparator.comparing(ReportesDAO.VentaCajero::total).reversed()).toList();
            for (var fila : filas) tabla.addRow(new Object[]{fila.nombre() + " (#" + fila.idUsuario() + ")", fila.tickets(),
                    dinero(fila.total()), total.signum() == 0 ? 0.0 : fila.total().doubleValue() * 100 / total.doubleValue()});
        });
    }

    private void exportarPdf() {
        Sesion.exigirAdministrador();
        if (mostrado == null) throw new IllegalStateException("Genera el reporte antes de exportarlo.");
        ReportesDAO.Reporte reporte = mostrado; LocalDate[] rango = rangoMostrado; String tipo = tipoMostrado;
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
        selector.setDialogTitle("Guardar reporte de corte de caja");
        selector.setSelectedFile(new java.io.File("corte_caja_" + tipo.toLowerCase() + "_" + rango[0] + ".pdf"));
        if (selector.showSaveDialog(vista) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        java.nio.file.Path elegido = selector.getSelectedFile().toPath();
        java.nio.file.Path archivo = elegido.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? elegido : elegido.resolveSibling(elegido.getFileName() + ".pdf");
        if (java.nio.file.Files.exists(archivo) && javax.swing.JOptionPane.showConfirmDialog(vista,
                "El archivo ya existe. ¿Deseas reemplazarlo?", "Confirmar reemplazo",
                javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) return;
        String usuario = java.util.Objects.toString(Sesion.exigirSesion().getNombre(), "Administrador");
        Tareas.ejecutar(vista, () -> {
            utils.ReporteCajaPDF.generar(reporte, tipo, rango[0], rango[1].minusDays(1), usuario, archivo);
            return archivo;
        }, generado -> javax.swing.JOptionPane.showMessageDialog(vista, "Reporte generado correctamente:\n"
                + generado.toAbsolutePath(), "Corte de caja", javax.swing.JOptionPane.INFORMATION_MESSAGE));
    }

    private static String dinero(BigDecimal monto) {
        return "$" + monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
