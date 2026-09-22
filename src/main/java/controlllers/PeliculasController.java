package controlllers;

import config.Conexion;
import config.Sesion;
import dao.PeliculaDAO;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import models.Pelicula;
import utils.Tareas;
import views.PeliculasView;

public class PeliculasController {
    private final PeliculasView vista;
    private List<Pelicula> peliculas = List.of();
    private final TableRowSorter<DefaultTableModel> filtro;

    public PeliculasController(PeliculasView vista) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        filtro = new TableRowSorter<>((DefaultTableModel) vista.getTablaPeliculas().getModel());
        vista.getTablaPeliculas().setRowSorter(filtro);
        vista.getTablaPeliculas().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        vista.getTablaPeliculas().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionar();
        });
        vista.addFiltroDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        vista.addGuardarListener(e -> Tareas.validar(vista, () -> guardar(false)));
        vista.addActualizarListener(e -> Tareas.validar(vista, () -> guardar(true)));
        vista.addLimpiarListener(e -> limpiar());
        vista.addEliminarListener(e -> Tareas.validar(vista, this::eliminar));
        cargar();
    }

    private void filtrar() {
        filtro.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(vista.getFiltro().trim())));
    }

    private Pelicula seleccionada() {
        int fila = vista.getTablaPeliculas().getSelectedRow();
        return fila < 0 ? null : peliculas.get(vista.getTablaPeliculas().convertRowIndexToModel(fila));
    }

    private void seleccionar() {
        Pelicula p = seleccionada();
        if (p == null) return;
        vista.setTitulo(p.getNombre()); vista.setSinopsis(p.getSinopsis());
        vista.setDuracion(Integer.toString(p.getDuracion())); vista.setGenero(p.getGenero());
        vista.setDirector(p.getDirector()); vista.setEstado(p.getEstado());
    }

    private void cargar() {
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) { return new PeliculaDAO(c).obtenerTodas(); }
        }, lista -> {
            vista.getTablaPeliculas().clearSelection();
            peliculas = lista;
            DefaultTableModel tabla = (DefaultTableModel) vista.getTablaPeliculas().getModel();
            tabla.setRowCount(0);
            for (Pelicula p : lista) tabla.addRow(new Object[]{p.getNombre(),p.getSinopsis(),p.getDuracion(),p.getGenero(),p.getDirector(),p.getEstado()});
        });
    }

    private void guardar(boolean actualizar) {
        Sesion.exigirAdministrador();
        Pelicula anterior = seleccionada();
        if (actualizar && anterior == null) throw new IllegalArgumentException("Selecciona la película que deseas actualizar.");
        String nombre = vista.getTitulo().trim();
        if (nombre.isEmpty()) throw new IllegalArgumentException("Ingresa el título de la película.");
        int duracion;
        try { duracion = Integer.parseInt(vista.getDuracion().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingresa una duración válida en minutos."); }
        if (duracion <= 0 || duracion >= 1440) throw new IllegalArgumentException("La duración debe estar entre 1 y 1439 minutos.");
        Pelicula p = new Pelicula(actualizar ? anterior.getIdPelicula() : 0, nombre, vista.getSinopsis().trim(),
                duracion, vista.getGenero().trim(), vista.getDirector().trim(), null, null, null, vista.getEstado());
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) {
                PeliculaDAO dao = new PeliculaDAO(c);
                return actualizar ? dao.actualizarPelicula(p) : dao.insertarPelicula(p);
            }
        }, guardado -> {
            if (!guardado) { Tareas.error(vista, "La película ya no existe. Actualiza el listado."); return; }
            limpiar(); cargar();
        });
    }

    private void eliminar() {
        Sesion.exigirAdministrador();
        Pelicula p = seleccionada();
        if (p == null) throw new IllegalArgumentException("Selecciona una película.");
        if (JOptionPane.showConfirmDialog(vista, "¿Eliminar la película " + p.getNombre() + "?",
                "Eliminar película", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) { return new PeliculaDAO(c).eliminarPelicula(p.getIdPelicula()); }
        }, eliminado -> { limpiar(); cargar(); });
    }

    private void limpiar() {
        vista.getTablaPeliculas().clearSelection();
        vista.setTitulo(""); vista.setSinopsis(""); vista.setDuracion("");
        vista.setGenero(""); vista.setDirector(""); vista.setEstado("CARTELERA");
    }
}
