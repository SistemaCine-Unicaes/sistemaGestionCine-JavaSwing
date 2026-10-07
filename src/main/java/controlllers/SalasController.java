package controlllers;

import config.Conexion;
import config.Sesion;
import dao.AsientoDAO;
import dao.SalaDAO;
import java.sql.Connection;
import models.Sala;
import services.SalaService;
import utils.Tareas;
import views.SalasView;
import views.MantenimientoSalaView;

public class SalasController {
    private final SalasView vista;
    public SalasController(SalasView vista) {
        Sesion.exigirAdministrador(); this.vista = vista;
        vista.alAbrir(this::abrir);
        vista.addActualizarListener(e -> cargar()); cargar();
    }
    private void cargar() {
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) { return new SalaDAO(c).listarSalas(); }
        }, vista::mostrarSalas);
    }
    private record Detalle(Sala sala, java.util.List<models.Asiento> asientos) {}
    private Detalle leer(int id) throws Exception {
        Sesion.exigirAdministrador();
        try (Connection c = Conexion.getConexion()) {
            Sala sala = new SalaDAO(c).obtenerSalaPorId(id);
            if (sala == null) throw new IllegalStateException("La sala ya no existe.");
            return new Detalle(sala,new AsientoDAO(c).obtenerAsientosPorSala(id));
        }
    }
    private void abrir(Sala sala) {
        Tareas.ejecutar(vista, () -> leer(sala.getIdSala()), detalle -> {
            MantenimientoSalaView modal = new MantenimientoSalaView(javax.swing.SwingUtilities.getWindowAncestor(vista));
            modal.mostrar(detalle.sala(),detalle.asientos());
            modal.alCambiar((asiento, activar, motivo) -> Tareas.validar(modal, () -> {
                SalaService.validarMotivo(activar,motivo);
                Tareas.ejecutar(modal, () -> {
                    new SalaService().cambiarEstado(sala.getIdSala(),asiento,activar,motivo);
                    return leer(sala.getIdSala());
                }, nuevo -> modal.mostrar(nuevo.sala(),nuevo.asientos()));
            }));
            modal.setVisible(true); cargar();
        });
    }
}
