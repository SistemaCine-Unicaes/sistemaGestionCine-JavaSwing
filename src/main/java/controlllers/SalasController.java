package controlllers;

import config.Conexion;
import config.Sesion;
import dao.SalaDAO;
import java.sql.Connection;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import models.Sala;
import services.SalaService;
import utils.Tareas;
import views.SalasView;

public class SalasController {
    private final SalasView vista;
    private List<Sala> salas = List.of();
    private int idSeleccionado;

    public SalasController(SalasView vista) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        vista.getTablaSalas().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        vista.getTablaSalas().getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int fila = vista.getTablaSalas().getSelectedRow();
            if (fila < 0) { idSeleccionado = 0; return; }
            Sala sala = salas.get(vista.getTablaSalas().convertRowIndexToModel(fila));
            idSeleccionado = sala.getIdSala(); vista.mostrarSala(sala);
        });
        vista.addNuevaListener(e -> limpiar());
        vista.addGuardarListener(e -> Tareas.validar(vista, this::guardar));
        cargar();
    }

    private void guardar() {
        Sesion.exigirAdministrador();
        Sala sala;
        try { sala = vista.leerSala(); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Completa los campos con números enteros válidos."); }
        sala.setIdSala(idSeleccionado);
        SalaService.validar(sala);
        Tareas.ejecutar(vista, () -> { new SalaService().guardar(sala); return null; }, resultado -> { limpiar(); cargar(); });
    }

    private void limpiar() {
        idSeleccionado = 0;
        vista.getTablaSalas().clearSelection(); vista.limpiarFormulario();
    }

    private void cargar() {
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) { return new SalaDAO(c).listarSalas(); }
        }, lista -> {
            vista.getTablaSalas().clearSelection(); salas = lista;
            DefaultTableModel tabla = (DefaultTableModel) vista.getTablaSalas().getModel();
            tabla.setRowCount(0);
            for (Sala s : lista) tabla.addRow(new Object[]{s.getCapacidadTotal(),s.getAsientosEspeciales(),s.getTiempoDeLimpieza(),s.getAsientosPorFila()});
        });
    }
}
