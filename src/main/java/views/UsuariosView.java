package views;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import views.estilos.Tema;

/** Gestión de cajeros y administradores. El diseño completo está en UsuariosView.form. */
public class UsuariosView extends javax.swing.JPanel {
    public static final String SIN_GENERO = "Sin especificar";

    public UsuariosView() {
        initComponents();
        aplicarEstilos();
        setModoEdicion(false);
    }

    /** Bordes de tarjeta, columnas adaptables, tabla y contraseñas: lo que el diseñador no representa. */
    private void aplicarEstilos() {
        scrPagina.getViewport().setBackground(Tema.FONDO);
        scrPagina.getVerticalScrollBar().setUnitIncrement(24);
        scrPagina.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tarjeta(pnlFormulario);
        tarjeta(pnlListado);
        views.estilos.Adaptable.columnas(pnlCampos, 2, 240, 12);
        views.estilos.Adaptable.columnas(pnlAcciones, 2, 180, 8);
        // Tema.campo crea los demás campos; las contraseñas usan JPasswordField y reciben el mismo estilo aquí.
        Tema.aplicarCampo(txtPassword);
        Tema.aplicarCampo(txtConfirmar);
        JLabel[] etiquetas = {lblNombre, lblUsername, lblEmail, lblRol, lblPassword, lblConfirmar, lblEstado, lblDui,
                lblTelefono, lblGenero, lblFechaNacimiento, lblFechaContratacion, lblDireccion, lblFiltro};
        JComponent[] controles = {txtNombre, txtUsername, txtEmail, cbRol, txtPassword, txtConfirmar, cbEstado, txtDui,
                txtTelefono, cbGenero, txtFechaNacimiento, txtFechaContratacion, txtDireccion, txtFiltro};
        for (int i = 0; i < etiquetas.length; i++) etiquetas[i].setLabelFor(controles[i]);
        // Los textos largos se ajustan a varias líneas en ventanas estrechas.
        for (JLabel etiqueta : new JLabel[]{lblSeccion, lblDescripcion, lblObligatorios, lblAyudaClave, lblFiltro, lblCantidad}) {
            views.estilos.TextoAdaptableUI.aplicar(etiqueta);
            etiqueta.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        // Tema.tabla envuelve la tabla en otro JScrollPane: se devuelve a su contenedor del formulario.
        Tema.tabla(tblUsuarios);
        scrTabla.setViewportView(tblUsuarios);
        scrTabla.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scrTabla.getViewport().setBackground(Tema.SUPERFICIE);
    }

    private static void tarjeta(JComponent panel) {
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE), panel.getBorder()));
    }

    /** Las opciones provienen de la base de datos cuando la columna es una enumeración. */
    public void setOpciones(List<String> roles, List<String> estados, List<String> generos) {
        cbRol.setModel(new DefaultComboBoxModel<>(roles.toArray(String[]::new)));
        cbEstado.setModel(new DefaultComboBoxModel<>(estados.toArray(String[]::new)));
        List<String> conVacio = new ArrayList<>(); conVacio.add(SIN_GENERO); conVacio.addAll(generos);
        cbGenero.setModel(new DefaultComboBoxModel<>(conVacio.toArray(String[]::new)));
    }

    // Un valor registrado antes del módulo (por ejemplo "Admin") se agrega para no perderlo al editar.
    private static void seleccionar(JComboBox<String> combo, String valor) {
        if (valor == null) { combo.setSelectedIndex(combo.getItemCount() > 0 ? 0 : -1); return; }
        if (((DefaultComboBoxModel<String>) combo.getModel()).getIndexOf(valor) < 0) combo.addItem(valor);
        combo.setSelectedItem(valor);
    }

    public void setModoEdicion(boolean edicion) {
        lblFormulario.setText(edicion ? "Editar usuario" : "Nuevo usuario");
        btnGuardar.setEnabled(!edicion); btnActualizar.setEnabled(edicion); btnEliminar.setEnabled(edicion);
        for (JTextField campo : new JTextField[]{txtEmail, txtPassword, txtConfirmar}) {
            campo.setEnabled(!edicion); campo.setBackground(edicion ? Tema.FONDO : Tema.SUPERFICIE);
        }
        if (edicion) limpiarPassword();
        lblAyudaClave.setText(edicion
                ? "El correo y la contraseña pertenecen a Supabase Auth y no se cambian desde esta pantalla."
                : "La contraseña debe tener al menos 8 caracteres y combinar letras y números.");
    }
    public void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setText(texto); lblMensaje.setForeground(error ? Tema.ERROR : Tema.EXITO);
    }
    public void mostrarCantidad(int visibles, int total) {
        lblCantidad.setText(visibles + " de " + total + " usuarios · Selecciona una fila para editar.");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrPagina = new javax.swing.JScrollPane();
        pnlPagina = new views.estilos.Pagina();
        pnlEncabezado = new javax.swing.JPanel();
        lblSeccion = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        pnlCuerpo = new javax.swing.JPanel();
        pnlFormulario = new javax.swing.JPanel();
        pnlCabecera = new javax.swing.JPanel();
        lblFormulario = new javax.swing.JLabel();
        lblObligatorios = new javax.swing.JLabel();
        pnlDatos = new javax.swing.JPanel();
        pnlCampos = new javax.swing.JPanel();
        pnlNombre = new javax.swing.JPanel();
        lblNombre = new javax.swing.JLabel();
        txtNombre = views.estilos.Tema.campo("", 20);
        pnlUsername = new javax.swing.JPanel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = views.estilos.Tema.campo("", 20);
        pnlEmail = new javax.swing.JPanel();
        lblEmail = new javax.swing.JLabel();
        txtEmail = views.estilos.Tema.campo("", 20);
        pnlRol = new javax.swing.JPanel();
        lblRol = new javax.swing.JLabel();
        cbRol = new javax.swing.JComboBox<>();
        pnlPassword = new javax.swing.JPanel();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        pnlConfirmar = new javax.swing.JPanel();
        lblConfirmar = new javax.swing.JLabel();
        txtConfirmar = new javax.swing.JPasswordField();
        pnlEstado = new javax.swing.JPanel();
        lblEstado = new javax.swing.JLabel();
        cbEstado = new javax.swing.JComboBox<>();
        pnlDui = new javax.swing.JPanel();
        lblDui = new javax.swing.JLabel();
        txtDui = views.estilos.Tema.campo("", 20);
        pnlTelefono = new javax.swing.JPanel();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = views.estilos.Tema.campo("", 20);
        pnlGenero = new javax.swing.JPanel();
        lblGenero = new javax.swing.JLabel();
        cbGenero = new javax.swing.JComboBox<>();
        pnlFechaNacimiento = new javax.swing.JPanel();
        lblFechaNacimiento = new javax.swing.JLabel();
        txtFechaNacimiento = views.estilos.Tema.campo("", 20);
        pnlFechaContratacion = new javax.swing.JPanel();
        lblFechaContratacion = new javax.swing.JLabel();
        txtFechaContratacion = views.estilos.Tema.campo("", 20);
        pnlDireccion = new javax.swing.JPanel();
        lblDireccion = new javax.swing.JLabel();
        txtDireccion = views.estilos.Tema.campo("", 20);
        lblAyudaClave = new javax.swing.JLabel();
        pnlPie = new javax.swing.JPanel();
        pnlAcciones = new javax.swing.JPanel();
        btnGuardar = views.estilos.Tema.botonPrimario("");
        btnActualizar = views.estilos.Tema.botonSecundario("");
        btnLimpiar = views.estilos.Tema.botonSecundario("");
        btnEliminar = views.estilos.Tema.botonSecundario("");
        lblMensaje = views.estilos.Tema.mensaje("", views.estilos.Tema.SECUNDARIO);
        pnlListado = new javax.swing.JPanel();
        pnlListadoTitulo = new javax.swing.JPanel();
        lblListado = new javax.swing.JLabel();
        pnlFiltro = new javax.swing.JPanel();
        lblFiltro = new javax.swing.JLabel();
        txtFiltro = views.estilos.Tema.campo("", 20);
        btnRecargar = views.estilos.Tema.botonSecundario("");
        scrTabla = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        lblCantidad = new javax.swing.JLabel();

        setBackground(new java.awt.Color(244, 245, 247));
        setLayout(new java.awt.BorderLayout());

        scrPagina.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlPagina.setBackground(new java.awt.Color(244, 245, 247));
        pnlPagina.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlPagina.setLayout(new java.awt.BorderLayout(0, 24));

        pnlEncabezado.setOpaque(false);
        pnlEncabezado.setLayout(new javax.swing.BoxLayout(pnlEncabezado, javax.swing.BoxLayout.PAGE_AXIS));

        lblSeccion.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblSeccion.setForeground(new java.awt.Color(180, 35, 60));
        lblSeccion.setText("ADMINISTRACIÓN  /  PERSONAL");
        lblSeccion.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlEncabezado.add(lblSeccion);

        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 34, 53));
        lblTitulo.setText("Usuarios");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlEncabezado.add(lblTitulo);

        lblDescripcion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblDescripcion.setForeground(new java.awt.Color(88, 101, 121));
        lblDescripcion.setText("Crea cuentas de cajeros y administradores y controla su acceso al sistema.");
        pnlEncabezado.add(lblDescripcion);

        pnlPagina.add(pnlEncabezado, java.awt.BorderLayout.NORTH);

        pnlCuerpo.setOpaque(false);
        pnlCuerpo.setLayout(new java.awt.BorderLayout(0, 24));

        pnlFormulario.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormulario.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlFormulario.setLayout(new java.awt.BorderLayout(16, 16));

        pnlCabecera.setOpaque(false);
        pnlCabecera.setLayout(new java.awt.BorderLayout(0, 6));

        lblFormulario.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblFormulario.setForeground(new java.awt.Color(24, 34, 53));
        lblFormulario.setText("Nuevo usuario");
        pnlCabecera.add(lblFormulario, java.awt.BorderLayout.NORTH);

        lblObligatorios.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblObligatorios.setForeground(new java.awt.Color(88, 101, 121));
        lblObligatorios.setText("* Campos obligatorios. Las fechas usan el formato dd/mm/aaaa.");
        pnlCabecera.add(lblObligatorios, java.awt.BorderLayout.CENTER);

        pnlFormulario.add(pnlCabecera, java.awt.BorderLayout.NORTH);

        pnlDatos.setOpaque(false);
        pnlDatos.setLayout(new java.awt.BorderLayout(0, 12));

        pnlCampos.setOpaque(false);
        pnlCampos.setLayout(new java.awt.GridLayout(0, 2, 12, 12));

        pnlNombre.setOpaque(false);
        pnlNombre.setLayout(new java.awt.BorderLayout(0, 6));

        lblNombre.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblNombre.setForeground(new java.awt.Color(24, 34, 53));
        lblNombre.setText("Nombre completo *");
        pnlNombre.add(lblNombre, java.awt.BorderLayout.NORTH);

        txtNombre.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtNombre.setForeground(new java.awt.Color(24, 34, 53));
        txtNombre.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlNombre.add(txtNombre, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlNombre);

        pnlUsername.setOpaque(false);
        pnlUsername.setLayout(new java.awt.BorderLayout(0, 6));

        lblUsername.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblUsername.setForeground(new java.awt.Color(24, 34, 53));
        lblUsername.setText("Usuario para iniciar sesión *");
        pnlUsername.add(lblUsername, java.awt.BorderLayout.NORTH);

        txtUsername.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtUsername.setForeground(new java.awt.Color(24, 34, 53));
        txtUsername.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlUsername.add(txtUsername, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlUsername);

        pnlEmail.setOpaque(false);
        pnlEmail.setLayout(new java.awt.BorderLayout(0, 6));

        lblEmail.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblEmail.setForeground(new java.awt.Color(24, 34, 53));
        lblEmail.setText("Correo *");
        pnlEmail.add(lblEmail, java.awt.BorderLayout.NORTH);

        txtEmail.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtEmail.setForeground(new java.awt.Color(24, 34, 53));
        txtEmail.setToolTipText("El correo identifica la cuenta en Supabase Auth y no se cambia después de crearla.");
        txtEmail.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlEmail.add(txtEmail, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlEmail);

        pnlRol.setOpaque(false);
        pnlRol.setLayout(new java.awt.BorderLayout(0, 6));

        lblRol.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblRol.setForeground(new java.awt.Color(24, 34, 53));
        lblRol.setText("Rol *");
        pnlRol.add(lblRol, java.awt.BorderLayout.NORTH);

        cbRol.setBackground(new java.awt.Color(255, 255, 255));
        cbRol.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cbRol.setForeground(new java.awt.Color(24, 34, 53));
        cbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cajero", "Administrador" }));
        cbRol.setPreferredSize(new java.awt.Dimension(160, 40));
        pnlRol.add(cbRol, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlRol);

        pnlPassword.setOpaque(false);
        pnlPassword.setLayout(new java.awt.BorderLayout(0, 6));

        lblPassword.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(24, 34, 53));
        lblPassword.setText("Contraseña *");
        pnlPassword.add(lblPassword, java.awt.BorderLayout.NORTH);

        txtPassword.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtPassword.setForeground(new java.awt.Color(24, 34, 53));
        txtPassword.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlPassword.add(txtPassword, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlPassword);

        pnlConfirmar.setOpaque(false);
        pnlConfirmar.setLayout(new java.awt.BorderLayout(0, 6));

        lblConfirmar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblConfirmar.setForeground(new java.awt.Color(24, 34, 53));
        lblConfirmar.setText("Confirmar contraseña *");
        pnlConfirmar.add(lblConfirmar, java.awt.BorderLayout.NORTH);

        txtConfirmar.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtConfirmar.setForeground(new java.awt.Color(24, 34, 53));
        txtConfirmar.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlConfirmar.add(txtConfirmar, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlConfirmar);

        pnlEstado.setOpaque(false);
        pnlEstado.setLayout(new java.awt.BorderLayout(0, 6));

        lblEstado.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(24, 34, 53));
        lblEstado.setText("Estado");
        pnlEstado.add(lblEstado, java.awt.BorderLayout.NORTH);

        cbEstado.setBackground(new java.awt.Color(255, 255, 255));
        cbEstado.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cbEstado.setForeground(new java.awt.Color(24, 34, 53));
        cbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo", "Inactivo" }));
        cbEstado.setPreferredSize(new java.awt.Dimension(160, 40));
        pnlEstado.add(cbEstado, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlEstado);

        pnlDui.setOpaque(false);
        pnlDui.setLayout(new java.awt.BorderLayout(0, 6));

        lblDui.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblDui.setForeground(new java.awt.Color(24, 34, 53));
        lblDui.setText("DUI (12345678-9)");
        pnlDui.add(lblDui, java.awt.BorderLayout.NORTH);

        txtDui.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtDui.setForeground(new java.awt.Color(24, 34, 53));
        txtDui.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlDui.add(txtDui, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlDui);

        pnlTelefono.setOpaque(false);
        pnlTelefono.setLayout(new java.awt.BorderLayout(0, 6));

        lblTelefono.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTelefono.setForeground(new java.awt.Color(24, 34, 53));
        lblTelefono.setText("Teléfono");
        pnlTelefono.add(lblTelefono, java.awt.BorderLayout.NORTH);

        txtTelefono.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtTelefono.setForeground(new java.awt.Color(24, 34, 53));
        txtTelefono.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlTelefono.add(txtTelefono, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlTelefono);

        pnlGenero.setOpaque(false);
        pnlGenero.setLayout(new java.awt.BorderLayout(0, 6));

        lblGenero.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblGenero.setForeground(new java.awt.Color(24, 34, 53));
        lblGenero.setText("Género");
        pnlGenero.add(lblGenero, java.awt.BorderLayout.NORTH);

        cbGenero.setBackground(new java.awt.Color(255, 255, 255));
        cbGenero.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cbGenero.setForeground(new java.awt.Color(24, 34, 53));
        cbGenero.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sin especificar", "Femenino", "Masculino", "Otro" }));
        cbGenero.setPreferredSize(new java.awt.Dimension(160, 40));
        pnlGenero.add(cbGenero, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlGenero);

        pnlFechaNacimiento.setOpaque(false);
        pnlFechaNacimiento.setLayout(new java.awt.BorderLayout(0, 6));

        lblFechaNacimiento.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFechaNacimiento.setForeground(new java.awt.Color(24, 34, 53));
        lblFechaNacimiento.setText("Fecha de nacimiento");
        pnlFechaNacimiento.add(lblFechaNacimiento, java.awt.BorderLayout.NORTH);

        txtFechaNacimiento.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtFechaNacimiento.setForeground(new java.awt.Color(24, 34, 53));
        txtFechaNacimiento.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlFechaNacimiento.add(txtFechaNacimiento, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlFechaNacimiento);

        pnlFechaContratacion.setOpaque(false);
        pnlFechaContratacion.setLayout(new java.awt.BorderLayout(0, 6));

        lblFechaContratacion.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFechaContratacion.setForeground(new java.awt.Color(24, 34, 53));
        lblFechaContratacion.setText("Fecha de contratación");
        pnlFechaContratacion.add(lblFechaContratacion, java.awt.BorderLayout.NORTH);

        txtFechaContratacion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtFechaContratacion.setForeground(new java.awt.Color(24, 34, 53));
        txtFechaContratacion.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlFechaContratacion.add(txtFechaContratacion, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlFechaContratacion);

        pnlDireccion.setOpaque(false);
        pnlDireccion.setLayout(new java.awt.BorderLayout(0, 6));

        lblDireccion.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblDireccion.setForeground(new java.awt.Color(24, 34, 53));
        lblDireccion.setText("Dirección");
        pnlDireccion.add(lblDireccion, java.awt.BorderLayout.NORTH);

        txtDireccion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtDireccion.setForeground(new java.awt.Color(24, 34, 53));
        txtDireccion.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlDireccion.add(txtDireccion, java.awt.BorderLayout.CENTER);

        pnlCampos.add(pnlDireccion);

        pnlDatos.add(pnlCampos, java.awt.BorderLayout.NORTH);

        lblAyudaClave.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblAyudaClave.setForeground(new java.awt.Color(88, 101, 121));
        lblAyudaClave.setText("La contraseña debe tener al menos 8 caracteres y combinar letras y números.");
        pnlDatos.add(lblAyudaClave, java.awt.BorderLayout.CENTER);

        pnlFormulario.add(pnlDatos, java.awt.BorderLayout.CENTER);

        pnlPie.setOpaque(false);
        pnlPie.setLayout(new java.awt.BorderLayout(0, 12));

        pnlAcciones.setOpaque(false);
        pnlAcciones.setLayout(new java.awt.GridLayout(0, 2, 8, 8));

        btnGuardar.setBackground(new java.awt.Color(180, 35, 60));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar usuario");
        btnGuardar.setPreferredSize(new java.awt.Dimension(180, 40));
        pnlAcciones.add(btnGuardar);

        btnActualizar.setBackground(new java.awt.Color(255, 255, 255));
        btnActualizar.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnActualizar.setForeground(new java.awt.Color(24, 34, 53));
        btnActualizar.setText("Guardar cambios");
        btnActualizar.setPreferredSize(new java.awt.Dimension(180, 40));
        pnlAcciones.add(btnActualizar);

        btnLimpiar.setBackground(new java.awt.Color(255, 255, 255));
        btnLimpiar.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(24, 34, 53));
        btnLimpiar.setText("Nuevo usuario");
        btnLimpiar.setPreferredSize(new java.awt.Dimension(180, 40));
        pnlAcciones.add(btnLimpiar);

        btnEliminar.setBackground(new java.awt.Color(255, 255, 255));
        btnEliminar.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(180, 35, 24));
        btnEliminar.setText("Eliminar usuario");
        btnEliminar.setPreferredSize(new java.awt.Dimension(180, 40));
        pnlAcciones.add(btnEliminar);

        pnlPie.add(pnlAcciones, java.awt.BorderLayout.NORTH);

        lblMensaje.setBackground(new java.awt.Color(244, 245, 247));
        lblMensaje.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblMensaje.setForeground(new java.awt.Color(88, 101, 121));
        lblMensaje.setText("Completa los datos para agregar un cajero o administrador.");
        lblMensaje.setOpaque(true);
        pnlPie.add(lblMensaje, java.awt.BorderLayout.CENTER);

        pnlFormulario.add(pnlPie, java.awt.BorderLayout.SOUTH);

        pnlCuerpo.add(pnlFormulario, java.awt.BorderLayout.NORTH);

        pnlListado.setBackground(new java.awt.Color(255, 255, 255));
        pnlListado.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlListado.setLayout(new java.awt.BorderLayout(16, 16));

        pnlListadoTitulo.setOpaque(false);
        pnlListadoTitulo.setLayout(new java.awt.BorderLayout(8, 12));

        lblListado.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblListado.setForeground(new java.awt.Color(24, 34, 53));
        lblListado.setText("Usuarios registrados");
        pnlListadoTitulo.add(lblListado, java.awt.BorderLayout.NORTH);

        pnlFiltro.setOpaque(false);
        pnlFiltro.setLayout(new java.awt.BorderLayout(0, 6));

        lblFiltro.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFiltro.setForeground(new java.awt.Color(24, 34, 53));
        lblFiltro.setText("Buscar por nombre, usuario, correo o rol");
        pnlFiltro.add(lblFiltro, java.awt.BorderLayout.NORTH);

        txtFiltro.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtFiltro.setForeground(new java.awt.Color(24, 34, 53));
        txtFiltro.setPreferredSize(new java.awt.Dimension(240, 40));
        pnlFiltro.add(txtFiltro, java.awt.BorderLayout.CENTER);

        pnlListadoTitulo.add(pnlFiltro, java.awt.BorderLayout.CENTER);

        btnRecargar.setBackground(new java.awt.Color(255, 255, 255));
        btnRecargar.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        btnRecargar.setForeground(new java.awt.Color(24, 34, 53));
        btnRecargar.setText("Actualizar listado");
        btnRecargar.setPreferredSize(new java.awt.Dimension(180, 40));
        pnlListadoTitulo.add(btnRecargar, java.awt.BorderLayout.SOUTH);

        pnlListado.add(pnlListadoTitulo, java.awt.BorderLayout.NORTH);

        scrTabla.setPreferredSize(new java.awt.Dimension(500, 230));

        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nombre", "Usuario", "Correo", "Rol", "Estado", "DUI", "Teléfono"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrTabla.setViewportView(tblUsuarios);

        pnlListado.add(scrTabla, java.awt.BorderLayout.CENTER);

        lblCantidad.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblCantidad.setForeground(new java.awt.Color(88, 101, 121));
        lblCantidad.setText("Cargando usuarios…");
        pnlListado.add(lblCantidad, java.awt.BorderLayout.SOUTH);

        pnlCuerpo.add(pnlListado, java.awt.BorderLayout.CENTER);

        pnlPagina.add(pnlCuerpo, java.awt.BorderLayout.CENTER);

        scrPagina.setViewportView(pnlPagina);

        add(scrPagina, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    public String getNombre() { return txtNombre.getText(); }
    public void setNombre(String nombre) { txtNombre.setText(nombre); }
    public String getUsername() { return txtUsername.getText(); }
    public void setUsername(String username) { txtUsername.setText(username); }
    public String getEmail() { return txtEmail.getText(); }
    public void setEmail(String email) { txtEmail.setText(email); }
    public char[] getPassword() { return txtPassword.getPassword(); }
    public char[] getConfirmacion() { return txtConfirmar.getPassword(); }
    public void setPassword(String password) { txtPassword.setText(password); }
    public void setConfirmacion(String password) { txtConfirmar.setText(password); }
    public void limpiarPassword() { txtPassword.setText(""); txtConfirmar.setText(""); }
    public String getRol() { return (String) cbRol.getSelectedItem(); }
    public void setRol(String rol) { seleccionar(cbRol, rol); }
    public String getEstado() { return (String) cbEstado.getSelectedItem(); }
    public void setEstado(String estado) { seleccionar(cbEstado, estado); }
    public String getGenero() {
        String genero = (String) cbGenero.getSelectedItem();
        return genero == null || SIN_GENERO.equals(genero) ? null : genero;
    }
    public void setGenero(String genero) { seleccionar(cbGenero, genero == null || genero.isBlank() ? SIN_GENERO : genero); }
    public String getDui() { return txtDui.getText(); }
    public void setDui(String dui) { txtDui.setText(dui); }
    public String getTelefono() { return txtTelefono.getText(); }
    public void setTelefono(String telefono) { txtTelefono.setText(telefono); }
    public String getFechaNacimiento() { return txtFechaNacimiento.getText().trim(); }
    public void setFechaNacimiento(String fecha) { txtFechaNacimiento.setText(fecha); }
    public String getFechaContratacion() { return txtFechaContratacion.getText().trim(); }
    public void setFechaContratacion(String fecha) { txtFechaContratacion.setText(fecha); }
    public String getDireccion() { return txtDireccion.getText(); }
    public void setDireccion(String direccion) { txtDireccion.setText(direccion); }
    public String getFiltro() { return txtFiltro.getText(); }
    public javax.swing.JTable getTablaUsuarios() { return tblUsuarios; }

    public void addGuardarListener(java.awt.event.ActionListener listener) { btnGuardar.addActionListener(listener); }
    public void addActualizarListener(java.awt.event.ActionListener listener) { btnActualizar.addActionListener(listener); }
    public void addLimpiarListener(java.awt.event.ActionListener listener) { btnLimpiar.addActionListener(listener); }
    public void addEliminarListener(java.awt.event.ActionListener listener) { btnEliminar.addActionListener(listener); }
    public void addRecargarListener(java.awt.event.ActionListener listener) { btnRecargar.addActionListener(listener); }
    public void addFiltroDocumentListener(javax.swing.event.DocumentListener listener) {
        txtFiltro.getDocument().addDocumentListener(listener);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnRecargar;
    private javax.swing.JComboBox<String> cbEstado;
    private javax.swing.JComboBox<String> cbGenero;
    private javax.swing.JComboBox<String> cbRol;
    private javax.swing.JLabel lblAyudaClave;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblConfirmar;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblDireccion;
    private javax.swing.JLabel lblDui;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFechaContratacion;
    private javax.swing.JLabel lblFechaNacimiento;
    private javax.swing.JLabel lblFiltro;
    private javax.swing.JLabel lblFormulario;
    private javax.swing.JLabel lblGenero;
    private javax.swing.JLabel lblListado;
    private javax.swing.JLabel lblMensaje;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblObligatorios;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblSeccion;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlCabecera;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JPanel pnlConfirmar;
    private javax.swing.JPanel pnlCuerpo;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlDireccion;
    private javax.swing.JPanel pnlDui;
    private javax.swing.JPanel pnlEmail;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlEstado;
    private javax.swing.JPanel pnlFechaContratacion;
    private javax.swing.JPanel pnlFechaNacimiento;
    private javax.swing.JPanel pnlFiltro;
    private javax.swing.JPanel pnlFormulario;
    private javax.swing.JPanel pnlGenero;
    private javax.swing.JPanel pnlListado;
    private javax.swing.JPanel pnlListadoTitulo;
    private javax.swing.JPanel pnlNombre;
    private javax.swing.JPanel pnlPagina;
    private javax.swing.JPanel pnlPassword;
    private javax.swing.JPanel pnlPie;
    private javax.swing.JPanel pnlRol;
    private javax.swing.JPanel pnlTelefono;
    private javax.swing.JPanel pnlUsername;
    private javax.swing.JScrollPane scrPagina;
    private javax.swing.JScrollPane scrTabla;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JPasswordField txtConfirmar;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtDui;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFechaContratacion;
    private javax.swing.JTextField txtFechaNacimiento;
    private javax.swing.JTextField txtFiltro;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtTelefono;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
