package views;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import views.estilos.Tema;

public class UsuariosView extends javax.swing.JPanel {
    public static final String SIN_GENERO = "Sin especificar";
    private final JLabel lblFormulario = Tema.texto("Nuevo usuario", Tema.SUBTITULO, Tema.TEXTO);
    private final JLabel lblAyudaClave = Tema.texto("", Tema.CUERPO, Tema.SECUNDARIO);
    private final JLabel lblMensaje = Tema.mensaje("Completa los datos para agregar un cajero o administrador.", Tema.SECUNDARIO);
    private final JLabel lblCantidad = Tema.texto("Cargando usuarios…", Tema.CUERPO, Tema.SECUNDARIO);

    public UsuariosView() {
        initComponents();
        configurarVista();
    }

    /** Se monta después de initComponents para conservar los controles del formulario NetBeans. */
    private void configurarVista() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        for (JTextField campo : new JTextField[]{txtNombre, txtUsername, txtEmail, txtPassword, txtConfirmar, txtDui,
                txtTelefono, txtFechaNacimiento, txtFechaContratacion, txtDireccion, txtFiltro}) {
            Tema.aplicarCampo(campo);
        }
        for (JComboBox<String> combo : List.of(cbRol, cbEstado, cbGenero)) {
            combo.setFont(Tema.CUERPO); combo.setBackground(Tema.SUPERFICIE);
            combo.setForeground(Tema.TEXTO); combo.setPreferredSize(new Dimension(160, Tema.ALTO_CONTROL));
        }
        Tema.aplicarBoton(btnGuardar, true);
        for (JButton boton : new JButton[]{btnActualizar, btnLimpiar, btnEliminar, btnRecargar}) Tema.aplicarBoton(boton, false);
        btnGuardar.setText("Guardar usuario"); btnActualizar.setText("Guardar cambios");
        btnLimpiar.setText("Nuevo usuario"); btnEliminar.setText("Eliminar usuario"); btnEliminar.setForeground(Tema.ERROR);
        txtEmail.setToolTipText("El correo identifica la cuenta en Supabase Auth y no se cambia después de crearla.");

        views.estilos.Pagina pagina = new views.estilos.Pagina();
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.PAGE_AXIS));
        encabezado.setOpaque(false);
        encabezado.add(Tema.texto("ADMINISTRACIÓN  /  PERSONAL", Tema.ETIQUETA, Tema.PRIMARIO));
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(Tema.texto("Usuarios", Tema.TITULO, Tema.TEXTO));
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(Tema.texto("Crea cuentas de cajeros y administradores y controla su acceso al sistema.", Tema.CUERPO, Tema.SECUNDARIO));
        pagina.add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 24));
        cuerpo.setOpaque(false);
        JPanel formulario = Tema.tarjeta();
        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false); cabecera.add(lblFormulario, BorderLayout.NORTH);
        cabecera.add(Tema.texto("* Campos obligatorios. Las fechas usan el formato dd/mm/aaaa.", Tema.CUERPO, Tema.SECUNDARIO), BorderLayout.CENTER);
        formulario.add(cabecera, BorderLayout.NORTH);

        JPanel campos = new JPanel(new views.estilos.RejillaAdaptable(2, 240, 12));
        campos.setOpaque(false);
        campos.add(grupo("Nombre completo *", txtNombre)); campos.add(grupo("Usuario para iniciar sesión *", txtUsername));
        campos.add(grupo("Correo *", txtEmail)); campos.add(grupo("Rol *", cbRol));
        campos.add(grupo("Contraseña *", txtPassword)); campos.add(grupo("Confirmar contraseña *", txtConfirmar));
        campos.add(grupo("Estado", cbEstado)); campos.add(grupo("DUI (12345678-9)", txtDui));
        campos.add(grupo("Teléfono", txtTelefono)); campos.add(grupo("Género", cbGenero));
        campos.add(grupo("Fecha de nacimiento", txtFechaNacimiento)); campos.add(grupo("Fecha de contratación", txtFechaContratacion));
        campos.add(grupo("Dirección", txtDireccion));
        JPanel datos = new JPanel(new BorderLayout(0, 12));
        datos.setOpaque(false); datos.add(campos, BorderLayout.NORTH); datos.add(lblAyudaClave, BorderLayout.CENTER);
        formulario.add(datos, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        JPanel acciones = new JPanel(new views.estilos.RejillaAdaptable(2, 180, 8));
        acciones.setOpaque(false);
        acciones.add(btnGuardar); acciones.add(btnActualizar); acciones.add(btnLimpiar); acciones.add(btnEliminar);
        pie.add(acciones, BorderLayout.NORTH); pie.add(lblMensaje, BorderLayout.CENTER);
        formulario.add(pie, BorderLayout.SOUTH);
        cuerpo.add(formulario, BorderLayout.NORTH);

        JPanel listado = Tema.tarjeta();
        JPanel tituloListado = new JPanel(new BorderLayout(8, 12));
        tituloListado.setOpaque(false);
        tituloListado.add(Tema.texto("Usuarios registrados", Tema.SUBTITULO, Tema.TEXTO), BorderLayout.NORTH);
        tituloListado.add(grupo("Buscar por nombre, usuario, correo o rol", txtFiltro), BorderLayout.CENTER);
        tituloListado.add(btnRecargar, BorderLayout.SOUTH);
        listado.add(tituloListado, BorderLayout.NORTH);
        JScrollPane tabla = Tema.tabla(tblUsuarios);
        tabla.setPreferredSize(new Dimension(500, 230));
        listado.add(tabla, BorderLayout.CENTER);
        listado.add(lblCantidad, BorderLayout.SOUTH);
        cuerpo.add(listado, BorderLayout.CENTER);
        pagina.add(cuerpo, BorderLayout.CENTER);
        add(pagina.conScroll(), BorderLayout.CENTER);
        setModoEdicion(false);
    }

    private JPanel grupo(String texto, JComponent campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 6)); panel.setOpaque(false);
        JLabel etiqueta = Tema.texto(texto, Tema.ETIQUETA, Tema.TEXTO); etiqueta.setLabelFor(campo);
        panel.add(etiqueta, BorderLayout.NORTH); panel.add(campo, BorderLayout.CENTER);
        return panel;
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

        jLabel1 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbRol = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        jLabel6 = new javax.swing.JLabel();
        txtConfirmar = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();
        cbEstado = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        txtDui = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        cbGenero = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        txtFechaNacimiento = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtFechaContratacion = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        btnGuardar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        txtFiltro = new javax.swing.JTextField();
        btnRecargar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();

        jLabel1.setText("Nombre completo:");

        jLabel2.setText("Usuario:");

        jLabel3.setText("Correo:");

        jLabel4.setText("Rol:");

        cbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cajero", "Administrador" }));

        jLabel5.setText("Contraseña:");

        jLabel6.setText("Confirmar contraseña:");

        jLabel7.setText("Estado:");

        cbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo", "Inactivo" }));

        jLabel8.setText("DUI:");

        jLabel9.setText("Teléfono:");

        jLabel10.setText("Género:");

        cbGenero.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Sin especificar", "Femenino", "Masculino", "Otro" }));

        jLabel11.setText("Fecha de nacimiento:");

        jLabel12.setText("Fecha de contratación:");

        jLabel13.setText("Dirección:");

        btnGuardar.setText("Guardar");

        btnActualizar.setText("Actualizar");

        btnLimpiar.setText("Limpiar");

        btnEliminar.setText("Eliminar");

        jLabel14.setText("Filtrar usuarios:");

        btnRecargar.setText("Actualizar listado");

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
        jScrollPane1.setViewportView(tblUsuarios);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11)
                            .addComponent(jLabel13))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(txtEmail)
                            .addComponent(txtPassword)
                            .addComponent(cbEstado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtTelefono)
                            .addComponent(txtFechaNacimiento)
                            .addComponent(txtDireccion))
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(cbRol, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtConfirmar)
                            .addComponent(txtDui)
                            .addComponent(cbGenero, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtFechaContratacion)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnGuardar)
                        .addGap(18, 18, 18)
                        .addComponent(btnActualizar)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRecargar)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(cbRol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtConfirmar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtDui, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(cbGenero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtFechaNacimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(txtFechaContratacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnActualizar)
                    .addComponent(btnLimpiar)
                    .addComponent(btnEliminar))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRecargar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
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
