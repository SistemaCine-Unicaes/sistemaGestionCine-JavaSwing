package views;

import dao.CajaDAO;
import dao.ReporteCajaDetalladoDAO;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import models.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CajasViewTest {

    @BeforeEach
    void iniciarSesionAdmin() {
        Usuario admin = new Usuario();
        admin.setIdUsuario(1);
        admin.setNombre("Admin Test");
        admin.setRol("Administrador");
        config.Sesion.setUsuarioActual(admin);
    }

    @AfterEach
    void cerrarSesion() {
        config.Sesion.cerrarSesion();
    }

    @Test
    void cajasViewMuestraTarjetasYPermiteAbrirReporte() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CajasView vista = new CajasView();
            var c1 = new CajaDAO.InfoCaja(1, 1, "Caja 01 - Taquilla Principal", "OCUPADA", 2, "Carlos López",
                    Timestamp.valueOf("2026-11-09 08:30:00"), 15, new BigDecimal("75.00"));
            var c2 = new CajaDAO.InfoCaja(2, 2, "Caja 02 - Taquilla Central", "DISPONIBLE", null, null,
                    null, 0, BigDecimal.ZERO);

            vista.mostrarCajas(List.of(c1, c2));

            AtomicReference<CajaDAO.InfoCaja> seleccionada = new AtomicReference<>();
            vista.alAbrirReporte(seleccionada::set);

            JButton primerBoton = findButton(vista, "Ver reporte de caja");
            assertNotNull(primerBoton, "Debe existir el botón Ver reporte de caja");
            primerBoton.doClick();

            assertNotNull(seleccionada.get());
            assertEquals(1, seleccionada.get().idCaja());
            assertEquals("Caja 01 - Taquilla Principal", seleccionada.get().nombre());
        });
    }

    @Test
    void reportePorCajaRangoFechasValidacion() {
        // Rango válido
        LocalDate[] rango = controlllers.ReportePorCajaController.validarRango("01/10/2026", "31/10/2026");
        assertEquals(LocalDate.of(2026, 10, 1), rango[0]);
        assertEquals(LocalDate.of(2026, 10, 31), rango[1]);

        // Fecha inicial inválida
        assertThrows(IllegalArgumentException.class, () ->
                controlllers.ReportePorCajaController.validarRango("32/10/2026", "31/10/2026"));

        // Fecha final inválida
        assertThrows(IllegalArgumentException.class, () ->
                controlllers.ReportePorCajaController.validarRango("01/10/2026", "fecha-invalida"));

        // Desde posterior a Hasta
        assertThrows(IllegalArgumentException.class, () ->
                controlllers.ReportePorCajaController.validarRango("10/11/2026", "01/11/2026"));
    }

    @Test
    void reportePorCajaFiltroSoloCombosYCalculaPeriodos() {
        ReportePorCajaView vista = new ReportePorCajaView();

        // Verificar que los combos están presentes
        assertNotNull(vista.getCmbCajas());
        assertNotNull(vista.getCmbPeriodo());

        // Verificar que el combo de período no tiene "Personalizado"
        for (int i = 0; i < vista.getCmbPeriodo().getItemCount(); i++) {
            assertNotEquals("Personalizado", vista.getCmbPeriodo().getItemAt(i));
        }

        // Probar cálculo de rangos preestablecidos
        LocalDate hoy = LocalDate.now();
        LocalDate[] rangoHoy = controlllers.ReportePorCajaController.calcularRangoPeriodo("Hoy");
        assertEquals(hoy, rangoHoy[0]);
        assertEquals(hoy, rangoHoy[1]);

        LocalDate[] rangoEsteMes = controlllers.ReportePorCajaController.calcularRangoPeriodo("Este mes");
        assertEquals(hoy.withDayOfMonth(1), rangoEsteMes[0]);
        assertEquals(hoy.withDayOfMonth(hoy.lengthOfMonth()), rangoEsteMes[1]);

        LocalDate[] rangoProximos = controlllers.ReportePorCajaController.calcularRangoPeriodo("Próximos 30 días");
        assertEquals(hoy, rangoProximos[0]);
        assertEquals(hoy.plusDays(30), rangoProximos[1]);
    }

    private static JButton findButton(java.awt.Container container, String text) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JButton btn && text.equals(btn.getText())) {
                return btn;
            }
            if (comp instanceof java.awt.Container sub) {
                JButton found = findButton(sub, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
