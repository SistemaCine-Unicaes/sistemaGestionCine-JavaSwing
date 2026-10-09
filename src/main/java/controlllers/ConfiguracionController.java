package controlllers;

import config.Conexion;
import config.Configuracion;
import config.Sesion;
import dao.ConfiguracionDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.swing.JOptionPane;
import utils.Tareas;
import views.ConfiguracionView;

public final class ConfiguracionController {
    private final ConfiguracionView vista;

    public ConfiguracionController(ConfiguracionView vista) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        vista.addGuardarListener(e -> Tareas.validar(vista, this::guardar));
        vista.addRecargarListener(e -> Tareas.validar(vista, this::cargar));
        cargar();
    }

    private void cargar() {
        Sesion.exigirAdministrador();
        vista.habilitarEdicion(false);
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) { return new ConfiguracionDAO(c).obtenerPrecio(); }
        }, precio -> {
            vista.setPrecio(precio == null ? "" : precio.toPlainString());
            vista.habilitarEdicion(true);
        });
    }

    private void guardar() {
        Sesion.exigirAdministrador();
        BigDecimal precio = Configuracion.precio(vista.getPrecio());
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) { new ConfiguracionDAO(c).guardarPrecio(precio); }
            return precio;
        }, guardado -> {
            vista.setPrecio(guardado.toPlainString());
            JOptionPane.showMessageDialog(vista, "Precio guardado. Se aplicará a las nuevas ventas.");
        });
    }
}