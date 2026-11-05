package controlllers;

import config.Sesion;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import models.Usuario;
import services.UsuarioService;
import utils.Tareas;
import views.UsuariosView;

public class UsuariosController {
    private final UsuariosView vista;
    private final UsuarioService servicio;
    private List<Usuario> usuarios = List.of();
    private final TableRowSorter<DefaultTableModel> filtro;

    public UsuariosController(UsuariosView vista) { this(vista, new UsuarioService()); }

    UsuariosController(UsuariosView vista, UsuarioService servicio) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        this.servicio = servicio;
        filtro = new TableRowSorter<>((DefaultTableModel) vista.getTablaUsuarios().getModel());
        vista.getTablaUsuarios().setRowSorter(filtro);
        vista.getTablaUsuarios().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        vista.getTablaUsuarios().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionar();
        });
        vista.addFiltroDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        vista.addGuardarListener(e -> Tareas.validar(vista, this::crear));
        vista.addActualizarListener(e -> Tareas.validar(vista, this::actualizar));
        vista.addLimpiarListener(e -> limpiar());
        vista.addEliminarListener(e -> Tareas.validar(vista, this::eliminar));
        vista.addRecargarListener(e -> Tareas.validar(vista, this::cargar));
        cargar();
    }

    private void filtrar() {
        filtro.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(vista.getFiltro().trim())));
        vista.mostrarCantidad(vista.getTablaUsuarios().getRowCount(), usuarios.size());
    }

    private Usuario seleccionado() {
        int fila = vista.getTablaUsuarios().getSelectedRow();
        return fila < 0 ? null : usuarios.get(vista.getTablaUsuarios().convertRowIndexToModel(fila));
    }

    private void seleccionar() {
        Usuario u = seleccionado();
        if (u == null) { limpiarFormulario(); return; }
        vista.setNombre(u.getNombre()); vista.setUsername(u.getUsername()); vista.setEmail(u.getEmail());
        vista.setRol(u.getRol()); vista.setEstado(u.getEstado()); vista.setDui(u.getDui());
        vista.setTelefono(u.getTelefono()); vista.setGenero(u.getGenero()); vista.setDireccion(u.getDireccion());
        vista.setFechaNacimiento(u.getFechaNacimiento() == null ? "" : u.getFechaNacimiento().toLocalDate().format(CorteCajaController.FECHA));
        vista.setFechaContratacion(u.getFechaContratacion() == null ? "" : u.getFechaContratacion().toLocalDate().format(CorteCajaController.FECHA));
        vista.setModoEdicion(true);
        vista.mostrarMensaje("Editando a " + u.getUsername() + ". Guarda los cambios al terminar.", false);
    }

    private void cargar() {
        Sesion.exigirAdministrador();
        Tareas.ejecutar(vista, servicio::cargar, catalogo -> {
            vista.getTablaUsuarios().clearSelection();
            vista.setOpciones(catalogo.roles(), catalogo.estados(), catalogo.generos());
            usuarios = catalogo.usuarios();
            DefaultTableModel tabla = (DefaultTableModel) vista.getTablaUsuarios().getModel();
            tabla.setRowCount(0);
            for (Usuario u : usuarios) {
                tabla.addRow(new Object[]{u.getNombre(), u.getUsername(), u.getEmail(), u.getRol(), u.getEstado(), u.getDui(), u.getTelefono()});
            }
            limpiarFormulario();
            filtrar();
        });
    }

    private void crear() {
        Sesion.exigirAdministrador();
        Usuario u = leerFormulario(vista, 0);
        char[] clave = vista.getPassword(), confirmacion = vista.getConfirmacion();
        try {
            UsuarioService.validarClave(clave, confirmacion);
        } catch (IllegalArgumentException e) {
            Arrays.fill(clave, '\0'); Arrays.fill(confirmacion, '\0');
            throw e;
        }
        Tareas.ejecutar(vista, () -> {
            try { return servicio.crear(u, clave, confirmacion); }
            finally { Arrays.fill(clave, '\0'); Arrays.fill(confirmacion, '\0'); }
        }, creado -> {
            limpiar(); cargar();
            vista.mostrarMensaje(creado.requiereConfirmacion()
                    ? "Usuario " + u.getUsername() + " creado. Debe abrir el enlace enviado a " + u.getEmail() + " antes de iniciar sesión."
                    : "Usuario " + u.getUsername() + " creado. Ya puede iniciar sesión.", false);
        });
    }

    private void actualizar() {
        Sesion.exigirAdministrador();
        Usuario anterior = seleccionado();
        if (anterior == null) throw new IllegalArgumentException("Selecciona el usuario que deseas actualizar.");
        Usuario u = leerFormulario(vista, anterior.getIdUsuario());
        Tareas.ejecutar(vista, () -> servicio.actualizar(u), guardado -> {
            if (!guardado) { Tareas.error(vista, "El usuario ya no existe. Actualiza el listado."); return; }
            limpiar(); cargar(); vista.mostrarMensaje("Cambios guardados correctamente.", false);
        });
    }

    static Usuario leerFormulario(UsuariosView vista, int id) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombre(vista.getNombre().trim());
        u.setUsername(vista.getUsername().trim());
        u.setEmail(vista.getEmail().trim().toLowerCase(Locale.ROOT));
        u.setRol(vista.getRol()); u.setEstado(vista.getEstado()); u.setGenero(vista.getGenero());
        u.setDui(opcional(vista.getDui())); u.setTelefono(opcional(vista.getTelefono()));
        u.setDireccion(opcional(vista.getDireccion()));
        u.setFechaNacimiento(fecha(vista.getFechaNacimiento(), "nacimiento"));
        u.setFechaContratacion(fecha(vista.getFechaContratacion(), "contratación"));
        UsuarioService.validar(u);
        return u;
    }

    private static String opcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static java.sql.Date fecha(String texto, String campo) {
        if (texto.isEmpty()) return null;
        try {
            java.time.LocalDate dia = java.time.LocalDate.parse(texto, CorteCajaController.FECHA);
            if (dia.getYear() < 1 || dia.getYear() > 9999) throw new IllegalArgumentException();
            return java.sql.Date.valueOf(dia);
        } catch (java.time.DateTimeException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Ingresa una fecha de " + campo + " válida: dd/mm/aaaa (ej. 24/09/1998).");
        }
    }

    private void eliminar() {
        Sesion.exigirAdministrador();
        Usuario u = seleccionado();
        if (u == null) throw new IllegalArgumentException("Selecciona un usuario.");
        if (JOptionPane.showConfirmDialog(vista, "¿Eliminar al usuario " + u.getUsername() + "?\n"
                + "Solo se puede eliminar si no tiene ventas. Para impedir su acceso sin borrar datos, cámbialo a Inactivo.",
                "Eliminar usuario", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Tareas.ejecutar(vista, () -> servicio.eliminar(u.getIdUsuario()), eliminado -> {
            if (!eliminado) { Tareas.error(vista, "El usuario ya no existe. Actualiza el listado."); return; }
            limpiar(); cargar(); vista.mostrarMensaje("Usuario eliminado correctamente.", false);
        });
    }

    private void limpiar() {
        vista.getTablaUsuarios().clearSelection();
        limpiarFormulario();
        vista.mostrarMensaje("Completa los datos para agregar un cajero o administrador.", false);
    }

    private void limpiarFormulario() {
        vista.setNombre(""); vista.setUsername(""); vista.setEmail(""); vista.limpiarPassword();
        vista.setRol("Cajero"); vista.setEstado(UsuarioService.ACTIVO); vista.setGenero(null);
        vista.setDui(""); vista.setTelefono(""); vista.setDireccion("");
        vista.setFechaNacimiento(""); vista.setFechaContratacion("");
        vista.setModoEdicion(false);
    }
}
