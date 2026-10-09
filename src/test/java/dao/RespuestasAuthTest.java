package dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Interpreta las respuestas de Supabase Auth sin enviar solicitudes. */
class RespuestasAuthTest {
    @Test void registroDistingueConfirmacionPendienteYCuentaActiva() {
        assertTrue(UsuarioDAO.interpretarRegistro(200,
                "{\"id\":\"1\",\"email\":\"ana@cine.com\",\"confirmation_sent_at\":\"2026-10-08T10:00:00Z\",\"identities\":[{\"id\":\"1\"}]}"));
        assertFalse(UsuarioDAO.interpretarRegistro(200,
                "{\"access_token\":\"x\",\"user\":{\"email\":\"ana@cine.com\",\"identities\":[{\"id\":\"1\"}]}}"));
    }

    @Test void registroExplicaLosRechazosDeSupabase() {
        // Con confirmación activa, un correo existente devuelve 200 sin identidades.
        IllegalArgumentException repetido = assertThrows(IllegalArgumentException.class,
                () -> UsuarioDAO.interpretarRegistro(200, "{\"id\":\"1\",\"identities\": [ ]}"));
        assertTrue(repetido.getMessage().contains("ya tiene una cuenta"));
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioDAO.interpretarRegistro(422, "{\"code\":422,\"error_code\":\"user_already_exists\",\"msg\":\"User already registered\"}"));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> UsuarioDAO.interpretarRegistro(422, "{\"error_code\":\"weak_password\"}")).getMessage().contains("débil"));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> UsuarioDAO.interpretarRegistro(400, "{\"error_code\":\"email_address_invalid\"}")).getMessage().contains("correo"));
        assertTrue(assertThrows(IllegalStateException.class,
                () -> UsuarioDAO.interpretarRegistro(429, "{\"error_code\":\"over_email_send_rate_limit\"}")).getMessage().contains("límite"));
        assertThrows(IllegalStateException.class, () -> UsuarioDAO.interpretarRegistro(422, "{\"error_code\":\"signup_disabled\"}"));
        assertThrows(IllegalStateException.class, () -> UsuarioDAO.interpretarRegistro(401, null));
        assertThrows(IllegalStateException.class, () -> UsuarioDAO.interpretarRegistro(503, ""));
    }

    @Test void inicioDeSesionAvisaSiElCorreoNoEstaConfirmado() {
        assertTrue(UsuarioDAO.interpretarInicio(200, "{\"access_token\":\"x\"}"));
        assertFalse(UsuarioDAO.interpretarInicio(400, "{\"error_code\":\"invalid_credentials\"}"));
        assertTrue(assertThrows(IllegalStateException.class,
                () -> UsuarioDAO.interpretarInicio(400, "{\"error_code\":\"email_not_confirmed\"}")).getMessage().contains("confirmado"));
        assertThrows(IllegalStateException.class, () -> UsuarioDAO.interpretarInicio(403, ""));
    }
}
