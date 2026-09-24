package controlllers;

import config.Sesion;
import javax.swing.AbstractButton;

public class DashboardController {
    private final AbstractButton[] administracion;
    private final AbstractButton taquilla;

    public DashboardController(AbstractButton salas, AbstractButton peliculas,
            AbstractButton corteCaja, AbstractButton taquilla, AbstractButton... adicionales) {
        this.taquilla = taquilla;
        administracion = new AbstractButton[3 + adicionales.length];
        administracion[0] = salas;
        administracion[1] = peliculas;
        administracion[2] = corteCaja;
        System.arraycopy(adicionales, 0, administracion, 3, adicionales.length);
    }

    public void aplicarPermisosPorRol() {
        for (AbstractButton boton : administracion) boton.setEnabled(Sesion.esAdministrador());
        var usuario = Sesion.getUsuarioActual();
        taquilla.setEnabled(usuario != null && (Sesion.esAdministrador()
                || "Cajero".equalsIgnoreCase(usuario.getRol())));
    }
}