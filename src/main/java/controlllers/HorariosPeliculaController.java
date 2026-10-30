package controlllers;

import config.Conexion;
import config.Sesion;
import dao.FuncionDAO;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import models.Funcion;
import utils.Tareas;
import views.HorariosPeliculaView;
import views.MDI;

public final class HorariosPeliculaController {
    private final HorariosPeliculaView vista;
    private final int idPelicula;

    public HorariosPeliculaController(MDI mdi, HorariosPeliculaView vista, int idPelicula) {
        Sesion.exigirVenta(); this.vista = vista; this.idPelicula = idPelicula;
        vista.addVolverListener(e -> mdi.volverACartelera());
        vista.addActualizarListener(e -> Tareas.validar(vista, this::cargar));
        vista.setComprarListener(id -> Tareas.validar(vista, () -> mdi.abrirTaquilla(id)));
        cargar();
    }
    private void cargar() {
        Sesion.exigirVenta(); vista.setCargando(true);
        new SwingWorker<List<Funcion>, Void>() {
            @Override protected List<Funcion> doInBackground() throws Exception {
                try (Connection c = Conexion.getConexion()) {
                    return new FuncionDAO(c).listarDisponibles().stream()
                            .filter(f -> f.getIdPelicula() == idPelicula).toList();
                }
            }
            @Override protected void done() {
                vista.setCargando(false);
                try { vista.mostrarFunciones(get()); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); vista.mostrarErrorCarga(); }
                catch (ExecutionException e) {
                    java.util.logging.Logger.getLogger(HorariosPeliculaController.class.getName())
                            .log(java.util.logging.Level.WARNING, "No se pudieron cargar las funciones", e.getCause());
                    vista.mostrarErrorCarga();
                }
            }
        }.execute();
    }
}
