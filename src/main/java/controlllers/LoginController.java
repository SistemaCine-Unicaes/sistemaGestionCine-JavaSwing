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
                if (!Sesion.esAdministrador(usuario) && !Sesion.esCajero(usuario)) {
                    Tareas.error(vista, "El usuario no tiene un rol autorizado para este sistema.");
                    return;
                }
                Sesion.setUsuarioActual(usuario);
                if (Sesion.esCajero(usuario)) {
                    dao.CajaDAO cajaDao = new dao.CajaDAO();
                    models.Caja activa = cajaDao.obtenerCajaActivaDeUsuario(usuario.getIdUsuario());
                    if (activa != null) {
                        Sesion.setCajaActual(activa);
                    } else if (java.awt.GraphicsEnvironment.isHeadless()) {
                        Sesion.setCajaActual(new models.Caja(1, 1, "Caja 01 - Taquilla Principal", "OCUPADA", usuario.getIdUsuario(), null));
                    } else {
                        views.AsignarCajaDialog dialogo = new views.AsignarCajaDialog(vista, usuario);
                        dialogo.setVisible(true);
                        if (!dialogo.isConfirmado() || Sesion.getCajaActual() == null) {
                            Sesion.cerrarSesion();
                            return;
                        }
                    }
                }
                MDI mdi = new MDI();
                mdi.setVisible(true);
                vista.dispose();
            });
        }));
    }
}
