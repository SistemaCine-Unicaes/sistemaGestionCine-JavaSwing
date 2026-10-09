package controlllers;

import config.Sesion;
import dao.UsuarioDAO;
import java.util.Arrays;
import utils.Tareas;
import views.Login;
import views.MDI;

public class LoginController {
    public LoginController(Login vista) {
        vista.addIngresarListener(e -> Tareas.validar(vista, () -> {
            String username = vista.getUsername().trim();
            char[] password = vista.getPassword();
            if (username.isBlank() || password.length == 0) {
                Arrays.fill(password, '\0');
                throw new IllegalArgumentException("Ingresa tu usuario y contraseña.");
            }
            Tareas.ejecutar(vista, () -> {
                try { return new UsuarioDAO().autenticarUsuario(username, new String(password)); }
                finally { Arrays.fill(password, '\0'); }
            }, usuario -> {
                vista.limpiarPassword();
                if (usuario == null) {
                    Tareas.error(vista, "Usuario o contraseña incorrectos, o usuario inactivo.");
                    return;
                }
                if (!"Admin".equalsIgnoreCase(usuario.getRol())
                        && !"Administrador".equalsIgnoreCase(usuario.getRol())
                        && !"Cajero".equalsIgnoreCase(usuario.getRol())) {
                    Tareas.error(vista, "El usuario no tiene un rol autorizado para este sistema.");
                    return;
                }
                Sesion.setUsuarioActual(usuario);
                MDI mdi = new MDI();
                mdi.setVisible(true);
                vista.dispose();
            });
        }));
    }
}
