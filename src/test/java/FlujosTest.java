import config.Configuracion;
import config.Sesion;
import controlllers.CorteCajaController;
import controlllers.DashboardController;
import controlllers.TaquillaController;
import dao.AsientoDAO;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import services.SalaService;
import services.VentaService;
import utils.Json;
import static org.junit.jupiter.api.Assertions.*;

class FlujosTest {
    @AfterEach void cerrarSesion() { Sesion.cerrarSesion(); }

    @Test void permisosYCierreDeSesion() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JButton salas = new JButton(), peliculas = new JButton(), corte = new JButton(), venta = new JButton();
            DashboardController permisos = new DashboardController(salas, peliculas, corte, venta);
            permisos.aplicarPermisosPorRol();
            assertFalse(venta.isEnabled()); assertFalse(salas.isEnabled());
            Usuario usuario = new Usuario(); usuario.setRol("Cajero"); Sesion.setUsuarioActual(usuario);
            permisos.aplicarPermisosPorRol();
            assertTrue(venta.isEnabled()); assertFalse(salas.isEnabled()); assertFalse(corte.isEnabled());
            assertThrows(IllegalStateException.class, Sesion::exigirAdministrador);
            usuario.setRol("Administrador"); permisos.aplicarPermisosPorRol();
            assertTrue(salas.isEnabled()); assertTrue(peliculas.isEnabled()); assertTrue(corte.isEnabled());
            usuario.setRol("Desconocido"); permisos.aplicarPermisosPorRol(); assertFalse(venta.isEnabled());
            Sesion.cerrarSesion(); assertFalse(Sesion.haySesionActiva());
            assertThrows(IllegalStateException.class, Sesion::exigirVenta);
        });
    }

    @Test void ventaRechazaSeleccionIncompletaDuplicadosYIdsInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> VentaService.validarSeleccion(List.of(1), 2));
        assertThrows(IllegalArgumentException.class, () -> VentaService.validarSeleccion(List.of(1, 1), 2));
        assertThrows(IllegalArgumentException.class, () -> VentaService.validarSeleccion(List.of(0), 1));
        assertThrows(IllegalArgumentException.class, () -> VentaService.validarSeleccion(List.of(), 0));
        assertDoesNotThrow(() -> VentaService.validarSeleccion(List.of(1, 2), 2));
    }

    @Test void precioExigeCentavosExactosYValorPositivo() {
        assertEquals(new BigDecimal("4.50"), Configuracion.precio("4.5"));
        for (String precio : List.of("0", "-1", "4.555", "abc", "1000000")) {
            assertThrows(IllegalArgumentException.class, () -> Configuracion.precio(precio));
        }
    }

    @Test void periodoMensualIncluyeFebreroBisiestoYExcluyeMarzo() {
        assertArrayEquals(new LocalDate[]{LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)},
                CorteCajaController.periodo(LocalDate.of(2024, 2, 29), "Mensual"));
        assertArrayEquals(new LocalDate[]{LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1)},
                CorteCajaController.periodo(LocalDate.of(2025, 12, 31), "Anual"));
        assertThrows(java.time.DateTimeException.class, () -> LocalDate.parse("31/02/2026", CorteCajaController.FECHA));
    }

    @Test void salaValidaCapacidadYFilasIncluidaAA() {
        assertThrows(IllegalArgumentException.class, () -> SalaService.validar(new Sala(5, 6, 10, 5, "ACTIVA")));
        assertThrows(IllegalArgumentException.class, () -> SalaService.validar(new Sala(5, 0, 10, 0, "ACTIVA")));
        assertDoesNotThrow(() -> SalaService.validar(new Sala(7, 2, 10, 3, "ACTIVA")));
        assertEquals("A", AsientoDAO.nombreFila(0)); assertEquals("Z", AsientoDAO.nombreFila(25));
        assertEquals("AA", AsientoDAO.nombreFila(26)); assertEquals("AB", AsientoDAO.nombreFila(27));
    }

    @Test void contraseñaConComillasYBarrasSeEscapaEnJson() {
        assertEquals("\"a\\\"b\\\\c\\n\\t\\u0001\"", Json.texto("a\"b\\c\n\t\u0001"));
    }

    @Test void reciboUsaIdsRealesYTotalExacto() {
        Pelicula pelicula = new Pelicula(); pelicula.setNombre("Película de prueba");
        Funcion funcion = new Funcion(7, 1, 2, Date.valueOf("2026-10-01"), Time.valueOf("18:00:00"), Time.valueOf("20:00:00"), "Programada");
        List<Asiento> asientos = List.of(new Asiento(11, 2, "Normal", "B", 1, "Disponible"), new Asiento(12, 2, "Normal", "B", 2, "Disponible"));
        Timestamp compra = Timestamp.valueOf("2026-09-15 10:00:00");
        List<Ticket> tickets = List.of(new Ticket(51, 7, 11, 3, new BigDecimal("0.10"), compra), new Ticket(52, 7, 12, 3, new BigDecimal("0.20"), compra));
        String recibo = TaquillaController.crearRecibo(pelicula, funcion, asientos, tickets, "Cajero");
        assertTrue(recibo.contains("Ticket #51  Asiento B-1"));
        assertTrue(recibo.contains("Ticket #52  Asiento B-2"));
        assertTrue(recibo.contains("TOTAL: $0.30"));
    }
}
