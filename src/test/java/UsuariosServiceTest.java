import config.Sesion;
import models.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import services.UsuarioService;
import static org.junit.jupiter.api.Assertions.*;

class UsuariosServiceTest {
    @AfterEach void cerrarSesion() { Sesion.cerrarSesion(); }

    private static Usuario usuario(int id, String rol) {
        Usuario u = new Usuario(); u.setIdUsuario(id); u.setRol(rol); u.setNombre("Usuario " + id);
        u.setUsername("usuario" + id); u.setEmail("usuario" + id + "@cine.com"); u.setEstado("Activo");
        return u;
    }

    private static UsuarioService sinConexion() {
        return new UsuarioService(() -> { fail("No debe abrir conexión"); return null; },
                (email, clave) -> { fail("No debe registrar en Supabase"); return false; });
    }

    @Test void contraseñaExigeLongitudLetrasNumerosYConfirmacion() {
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave("Ab1".toCharArray(), "Ab1".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave("abcdefgh".toCharArray(), "abcdefgh".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave("12345678".toCharArray(), "12345678".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave("Clave1234".toCharArray(), "Clave12345".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave(null, null));
        String larga = "a1".repeat(37);
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave(larga.toCharArray(), larga.toCharArray()));
        // Los caracteres acentuados ocupan dos bytes en UTF-8: 36 "ñ" superan el límite de 72 bytes.
        String acentos = "ñ".repeat(36) + "1";
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validarClave(acentos.toCharArray(), acentos.toCharArray()));
        assertEquals("Clave1234", UsuarioService.validarClave("Clave1234".toCharArray(), "Clave1234".toCharArray()));
        assertEquals("ñandú 2026", UsuarioService.validarClave("ñandú 2026".toCharArray(), "ñandú 2026".toCharArray()));
    }

    @Test void rolesReconocidosIncluyenAdminHeredado() {
        assertTrue(Sesion.esAdministrador(usuario(1, "Administrador")));
        assertTrue(Sesion.esAdministrador(usuario(1, "admin")));
        assertFalse(Sesion.esAdministrador(usuario(1, "Cajero")));
        assertTrue(Sesion.esCajero(usuario(1, "CAJERO")));
        assertFalse(Sesion.esCajero(null)); assertFalse(Sesion.esAdministrador((Usuario) null));
        assertDoesNotThrow(() -> UsuarioService.validar(usuario(1, "Admin")));
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validar(usuario(1, "Cliente")));
        Usuario sinEstado = usuario(1, "Cajero"); sinEstado.setEstado(null);
        assertThrows(IllegalArgumentException.class, () -> UsuarioService.validar(sinEstado));
    }

    @Test void cajeroNoGestionaUsuariosNiAbreConexion() {
        Sesion.setUsuarioActual(usuario(2, "Cajero"));
        UsuarioService servicio = sinConexion();
        assertThrows(IllegalStateException.class, servicio::cargar);
        assertThrows(IllegalStateException.class, () -> servicio.crear(usuario(0, "Cajero"), "Clave1234".toCharArray(), "Clave1234".toCharArray()));
        assertThrows(IllegalStateException.class, () -> servicio.actualizar(usuario(3, "Administrador")));
        assertThrows(IllegalStateException.class, () -> servicio.eliminar(3));
        Sesion.cerrarSesion();
        assertThrows(IllegalStateException.class, servicio::cargar);
    }

    @Test void administradorNoSeEliminaASiMismoYLosDatosInvalidosNoLleganABaseDeDatos() {
        Sesion.setUsuarioActual(usuario(1, "Administrador"));
        UsuarioService servicio = sinConexion();
        assertThrows(IllegalStateException.class, () -> servicio.eliminar(1));
        Usuario invalido = usuario(0, "Cajero"); invalido.setEmail("correo-invalido");
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(invalido, "Clave1234".toCharArray(), "Clave1234".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(usuario(0, "Cajero"), "Clave1234".toCharArray(), "Otra1234".toCharArray()));
        assertThrows(IllegalArgumentException.class, () -> servicio.actualizar(invalido));
    }
}
