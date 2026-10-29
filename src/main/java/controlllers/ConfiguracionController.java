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
            try (Connection c = Conexion.getConexion()) { 
                return new ConfiguracionDAO(c).obtenerPrecios(); 
            }
        }, precios -> {
            if (precios != null) {
                vista.setPrecioAdulto(precios.adulto() == null ? "" : precios.adulto().toPlainString());
                vista.setPrecioNino(precios.nino() == null ? "" : precios.nino().toPlainString());
                vista.setPrecioAdultoMayor(precios.adultoMayor() == null ? "" : precios.adultoMayor().toPlainString());
            } else {
                vista.setPrecioAdulto("");
                vista.setPrecioNino("");
                vista.setPrecioAdultoMayor("");
            }
            vista.habilitarEdicion(true);
        });
    }

    private void guardar() {
        Sesion.exigirAdministrador();
        BigDecimal adulto = Configuracion.precio(vista.getPrecioAdulto());
        BigDecimal nino = Configuracion.precio(vista.getPrecioNino());
        BigDecimal adultoMayor = Configuracion.precio(vista.getPrecioAdultoMayor());

        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) { 
                new ConfiguracionDAO(c).guardarPrecios(adulto, nino, adultoMayor); 
            }
            return new ConfiguracionDAO.Precios(adulto, nino, adultoMayor);
        }, guardados -> {
            vista.setPrecioAdulto(guardados.adulto().toPlainString());
            vista.setPrecioNino(guardados.nino().toPlainString());
            vista.setPrecioAdultoMayor(guardados.adultoMayor().toPlainString());
            JOptionPane.showMessageDialog(vista, "Precios guardados exitosamente. Se aplicarán a las nuevas ventas.");
        });
    }
}