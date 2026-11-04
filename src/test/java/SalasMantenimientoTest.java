import org.junit.jupiter.api.Test;
import services.SalaService;
import static org.junit.jupiter.api.Assertions.*;

class SalasMantenimientoTest {
    @Test void motivoObligatorioYLimitadoAlDesactivar() {
        assertThrows(IllegalArgumentException.class,()->SalaService.validarMotivo(false,"  "));
        assertThrows(IllegalArgumentException.class,()->SalaService.validarMotivo(false,"x".repeat(301)));
        assertEquals("Mantenimiento",SalaService.validarMotivo(false," Mantenimiento "));
        assertNull(SalaService.validarMotivo(true,null));
    }
    @Test void cajeroNoPuedeCambiarEstadoNiConsultarConexion() {
        models.Usuario u=new models.Usuario(); u.setRol("Cajero"); config.Sesion.setUsuarioActual(u);
        try {
            SalaService servicio=new SalaService(()-> { fail("No debe abrir conexión"); return null; });
            assertThrows(IllegalStateException.class,()->servicio.cambiarEstado(1,null,false,"Mantenimiento"));
            assertThrows(IllegalStateException.class,()->servicio.cambiarEstado(1,2,true,null));
        } finally { config.Sesion.cerrarSesion(); }
    }
}
