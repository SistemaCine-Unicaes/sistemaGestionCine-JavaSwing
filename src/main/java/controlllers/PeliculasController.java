package controlllers;

import config.Conexion;
import config.Sesion;
import dao.PeliculaDAO;
import dao.ReportePeliculaDAO;
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
import utils.ReportePeliculaPDF;
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
        vista.addRecargarListener(e -> Tareas.validar(vista, this::cargar));
        vista.addReporteListener(e -> Tareas.validar(vista, () -> generarReporte(Integer.parseInt(e.getActionCommand()))));
        cargar();
    }

    private void filtrar() {
        filtro.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(vista.getFiltro().trim())));
        vista.mostrarCantidad(vista.getTablaPeliculas().getRowCount(), peliculas.size());
    }

    private Pelicula seleccionada() {
        int fila = vista.getTablaPeliculas().getSelectedRow();
        return fila < 0 ? null : peliculas.get(vista.getTablaPeliculas().convertRowIndexToModel(fila));
    }

    private void seleccionar() {
        Pelicula p = seleccionada();
        if (p == null) { limpiarFormulario(); return; }
        vista.setTitulo(p.getNombre()); vista.setSinopsis(p.getSinopsis());
        vista.setDuracion(Integer.toString(p.getDuracion())); vista.setGenero(p.getGenero());
        vista.setDirector(p.getDirector()); vista.setEstado(p.getEstado());
        vista.setFechaEstreno(p.getFechaEstreno() == null ? "" : p.getFechaEstreno().toLocalDate().format(CorteCajaController.FECHA));
        vista.setTipoEstreno(p.getTipoEstreno()); vista.setImagenUrl(p.getImagenUrl());
        vista.mostrarPoster(); vista.setModoEdicion(true);
        vista.mostrarMensaje("Editando la película #" + p.getIdPelicula() + ". Guarda los cambios al terminar.", false);
    }

    private void cargar() {
        Sesion.exigirAdministrador();
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) { return new PeliculaDAO(c).obtenerTodas(); }
        }, lista -> {
            vista.getTablaPeliculas().clearSelection();
            peliculas = lista;
            DefaultTableModel tabla = (DefaultTableModel) vista.getTablaPeliculas().getModel();
            tabla.setRowCount(0);
            for (Pelicula p : lista) tabla.addRow(new Object[]{p.getNombre(),p.getSinopsis(),p.getDuracion(),p.getGenero(),p.getDirector(),p.getEstado(),"Ver reporte"});
            filtrar();
        });
    }

    private void generarReporte(int filaModelo) {
        Sesion.exigirAdministrador();
        if (filaModelo < 0 || filaModelo >= peliculas.size()) throw new IllegalArgumentException("Actualiza el listado e inténtalo de nuevo.");
        Pelicula pelicula = peliculas.get(filaModelo);
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) { return new ReportePeliculaDAO(c).obtener(pelicula.getIdPelicula()); }
        }, reporte -> {
            if (reporte == null) { Tareas.error(vista, "La película ya no existe. Actualiza el listado."); return; }
            javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
            selector.setDialogTitle("Guardar reporte de " + reporte.pelicula().getNombre());
            selector.setSelectedFile(new java.io.File(nombreArchivo(reporte.pelicula().getNombre())));
            if (selector.showSaveDialog(vista) != javax.swing.JFileChooser.APPROVE_OPTION) return;
            java.nio.file.Path archivo = selector.getSelectedFile().toPath();
            if (!archivo.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
                archivo = archivo.resolveSibling(archivo.getFileName() + ".pdf");
            }
            if (java.nio.file.Files.exists(archivo)
                    && javax.swing.JOptionPane.showConfirmDialog(vista, "El archivo ya existe. ¿Deseas reemplazarlo?",
                            "Confirmar reemplazo", javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) return;
            java.nio.file.Path destino = archivo;
            Tareas.ejecutar(vista, () -> {
                ReportePeliculaPDF.generar(reporte, destino);
                return destino;
            }, generado -> {
                vista.mostrarMensaje("Reporte PDF guardado en " + generado.toAbsolutePath(), false);
                javax.swing.JOptionPane.showMessageDialog(vista, "Reporte generado correctamente:\n" + generado.toAbsolutePath(),
                        "Reporte de película", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            });
        });
    }

    private static String nombreArchivo(String titulo) {
        String seguro = titulo == null ? "pelicula" : titulo.trim().replaceAll("[^\\p{L}\\p{N}._-]+", "_");
        if (seguro.isBlank()) seguro = "pelicula";
        return "reporte_" + seguro + ".pdf";
    }

    private void guardar(boolean actualizar) {
        Sesion.exigirAdministrador();
        Pelicula anterior = seleccionada();
        if (actualizar && anterior == null) throw new IllegalArgumentException("Selecciona la película que deseas actualizar.");
        Pelicula p = leerFormulario(vista, actualizar ? anterior.getIdPelicula() : 0);
        Tareas.ejecutar(vista, () -> {
            Sesion.exigirAdministrador();
            try (Connection c = Conexion.getConexion()) {
                PeliculaDAO dao = new PeliculaDAO(c);
                return actualizar ? dao.actualizarPelicula(p) : dao.insertarPelicula(p);
            }
        }, guardado -> {
            if (!guardado) { Tareas.error(vista, "La película ya no existe. Actualiza el listado."); return; }
            limpiar(); cargar();
            vista.mostrarMensaje(actualizar ? "Cambios guardados correctamente." : "Película agregada correctamente.", false);
        });
    }

    static Pelicula leerFormulario(PeliculasView vista, int id) {
        String nombre = vista.getTitulo().trim();
        if (nombre.isEmpty()) throw new IllegalArgumentException("Ingresa el título de la película.");
        validarLongitud(nombre, "El título", 150);
        validarLongitud(vista.getGenero().trim(), "El género", 50);
        validarLongitud(vista.getDirector().trim(), "El director", 100);
        validarLongitud(vista.getTipoEstreno(), "El tipo de estreno", 50);
        int duracion;
        try { duracion = Integer.parseInt(vista.getDuracion().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Ingresa una duración válida en minutos."); }
        if (duracion <= 0 || duracion >= 1440) throw new IllegalArgumentException("La duración debe estar entre 1 y 1439 minutos.");
        java.sql.Date fecha = null;
        if (!vista.getFechaEstreno().isEmpty()) {
            try {
                java.time.LocalDate dia = java.time.LocalDate.parse(vista.getFechaEstreno(), CorteCajaController.FECHA);
                if (dia.getYear() < 1 || dia.getYear() > 9999) throw new IllegalArgumentException();
                fecha = java.sql.Date.valueOf(dia);
            } catch (java.time.DateTimeException | IllegalArgumentException e) {
                throw new IllegalArgumentException("Ingresa una fecha de estreno válida: dd/mm/aaaa (ej. 24/09/2026).");
            }
        }
        String imagen = vista.getImagenUrl();
        if (!imagen.isEmpty()) {
            try {
                if (imagen.contains("://")) {
                    java.net.URI uri = java.net.URI.create(imagen);
                    if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                            || uri.getHost() == null) throw new IllegalArgumentException();
                } else java.nio.file.Path.of(imagen);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ingresa una URL HTTP(S) o una ruta local válida para el póster.");
            }
        }
        return new Pelicula(id, nombre, vista.getSinopsis().trim(), duracion, vista.getGenero().trim(),
                vista.getDirector().trim(), fecha, vista.getTipoEstreno(), imagen.isEmpty() ? null : imagen, vista.getEstado());
    }

    private static void validarLongitud(String texto, String campo, int limite) {
        if (texto != null && texto.codePointCount(0, texto.length()) > limite) {
            throw new IllegalArgumentException(campo + " admite hasta " + limite + " caracteres.");
        }
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
        }, eliminado -> {
            if (!eliminado) { Tareas.error(vista, "La película ya no existe. Actualiza el listado."); return; }
            limpiar(); cargar(); vista.mostrarMensaje("Película eliminada correctamente.", false);
        });
    }

    private void limpiar() {
        vista.getTablaPeliculas().clearSelection();
        limpiarFormulario();
        vista.mostrarMensaje("Completa los datos para agregar una película.", false);
    }

    private void limpiarFormulario() {
        vista.setTitulo(""); vista.setSinopsis(""); vista.setDuracion("");
        vista.setGenero(""); vista.setDirector(""); vista.setEstado("CARTELERA");
        vista.setFechaEstreno(""); vista.setTipoEstreno(null); vista.setImagenUrl("");
        vista.mostrarPoster(); vista.setModoEdicion(false);
    }
}
