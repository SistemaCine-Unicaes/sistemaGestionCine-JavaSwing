package controlllers;

import config.Sesion;
import dao.CajaDAO;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import utils.Tareas;
import views.CajasView;
import views.ReportePorCajaView;

/**
 * Controlador para la administración de cajas del cine.
 */
public class CajasController {

    private final CajasView vista;
    private final CajaDAO dao;

    public CajasController(CajasView vista) {
        this(vista, new CajaDAO());
    }

    public CajasController(CajasView vista, CajaDAO dao) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        this.dao = dao;

        vista.alAbrirReporte(this::abrirReporteDeCaja);
        vista.alLiberarCaja(this::confirmarLiberacionCaja);
        vista.addActualizarListener(e -> cargar());
        vista.addReporteGeneralListener(e -> abrirReporteDeCaja(null));

        cargar();
    }

    public void cargar() {
        if (!vista.isShowing()) {
            try {
                vista.mostrarCajas(dao.listarConResumenHoy());
            } catch (Exception ignored) {}
            return;
        }
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            return dao.listarConResumenHoy();
        }, vista::mostrarCajas);
    }

    private void abrirReporteDeCaja(CajaDAO.InfoCaja caja) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        ReportePorCajaView vistaReporte = new ReportePorCajaView();
        ReportePorCajaController controller = new ReportePorCajaController(vistaReporte);

        if (caja != null) {
            controller.filtrarPorCaja(caja.nombre());
        }

        Window ventana = SwingUtilities.getWindowAncestor(vista);
        JDialog dialog = new JDialog(
                ventana instanceof Frame f ? f : null,
                "Reporte de Ventas y Uso por Caja",
                true
        );
        dialog.setContentPane(vistaReporte);
        dialog.pack();
        dialog.setSize(980, 620);
        dialog.setLocationRelativeTo(vista);
        dialog.setVisible(true);
    }

    private void confirmarLiberacionCaja(CajaDAO.InfoCaja caja) {
        if (caja == null) return;
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                vista,
                "¿Deseas cerrar el turno y liberar «" + caja.nombre() + "»?\n"
                + "El operador actual (" + (caja.nombreUsuarioActual() != null ? caja.nombreUsuarioActual() : "desconocido") + ") quedará desvinculado.",
                "Confirmar liberación de caja",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (!vista.isShowing()) {
                dao.forzarLiberacion(caja.idCaja());
                cargar();
                return;
            }
            Tareas.ejecutar(vista, () -> {
                Sesion.exigirAdministrador();
                dao.forzarLiberacion(caja.idCaja());
                return dao.listarConResumenHoy();
            }, vista::mostrarCajas);
        }
    }
}
