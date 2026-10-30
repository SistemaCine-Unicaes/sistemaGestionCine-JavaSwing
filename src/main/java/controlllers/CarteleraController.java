package controlllers;

import config.Conexion;
import config.Sesion;
import dao.PeliculaDAO;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import models.Pelicula;
import utils.Tareas;
import views.CarteleraView;
import views.MDI;

public final class CarteleraController {
    private final CarteleraView vista;

    public CarteleraController(MDI mdi, CarteleraView vista) {
        Sesion.exigirVenta();
        this.vista = vista;
        vista.addActualizarListener(e -> Tareas.validar(vista, this::cargar));
        vista.setHorariosListener(pelicula -> Tareas.validar(vista, () -> mdi.abrirHorarios(pelicula)));
        cargar();
    }

    private void cargar() {
        Sesion.exigirVenta();
        vista.setCargando(true);
        new SwingWorker<List<Pelicula>, Void>() {
            @Override protected List<Pelicula> doInBackground() throws Exception {
                try (Connection c = Conexion.getConexion()) {
                    return new PeliculaDAO(c).obtenerTodas();
                }
            }
            @Override protected void done() {
                vista.setCargando(false);
                try {
                    vista.mostrarCatalogo(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); vista.mostrarErrorCarga();
                } catch (ExecutionException e) {
                    java.util.logging.Logger.getLogger(CarteleraController.class.getName())
                            .log(java.util.logging.Level.WARNING, "No se pudo cargar la cartelera", e.getCause());
                    vista.mostrarErrorCarga();
                }
            }
        }.execute();
    }
}
