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
    public CorteCajaController(CorteCajaView vista) {
        Sesion.exigirAdministrador(); this.vista = vista;
        vista.setFecha(LocalDate.now(java.time.ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador"))).format(FECHA));
        vista.addGenerarReporteListener(e -> Tareas.validar(vista, this::generar));
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
        LocalDate[] rango = periodo(fecha, vista.getTipoReporte());
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) { return new ReportesDAO(c).obtenerReporte(rango[0], rango[1]); }
        }, reporte -> {
            vista.setPeriodo("Periodo " + vista.getTipoReporte().toLowerCase() + ": del " + rango[0].format(FECHA)
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

    private static String dinero(BigDecimal monto) {
        return "$" + monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
