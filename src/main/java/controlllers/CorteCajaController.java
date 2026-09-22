package controlllers;

import config.Conexion;
import config.Sesion;
import dao.ReportesDAO;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
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
            vista.setTicketsVendidosText("Tickets Vendidos: " + reporte.tickets());
            vista.setTotalIngresosText("Total Ingresos: $" + reporte.total().toPlainString());
            DefaultTableModel tabla = (DefaultTableModel) vista.getTablaVentas().getModel();
            tabla.setRowCount(0);
            for (var fila : reporte.cajeros()) tabla.addRow(new Object[]{fila.nombre() + " (#" + fila.idUsuario() + ")", fila.tickets(), "$" + fila.total().toPlainString()});
        });
    }
}
