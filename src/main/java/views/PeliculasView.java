package views;

import java.awt.*;
import javax.swing.*;
import views.estilos.Tema;

public class PeliculasView extends javax.swing.JPanel {
    private final JTextField txtFechaEstreno = Tema.campo("", 12);
    private final JTextField txtImagenUrl = Tema.campo("", 24);
    private final JComboBox<String> cbTipoEstreno = new JComboBox<>(new String[]{"Sin especificar", "MUNDIAL", "ESTANDAR"});
    private final JButton btnRecargar = Tema.botonSecundario("Actualizar listado");
    private final JLabel lblFormulario = Tema.texto("Nueva película", Tema.SUBTITULO, Tema.TEXTO);
    private final JLabel lblMensaje = Tema.mensaje("Completa los datos para agregar una película.", Tema.SECUNDARIO);
    private final JLabel lblCantidad = Tema.texto("Cargando películas…", Tema.CUERPO, Tema.SECUNDARIO);
    private final JPanel pnlPoster = new JPanel(new BorderLayout());

    public PeliculasView() {
        initComponents();
        cbEstado.addItem("ARCHIVADA");
        configurarVista();
    }

    /** Se monta después de initComponents para conservar los controles y eventos del formulario NetBeans. */
    private void configurarVista() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        for (JTextField campo : new JTextField[]{txtTitulo, txtSinopsis, txtDuracion, txtGenero, txtDirector, txtFiltro}) {
            Tema.aplicarCampo(campo);
        }
        for (JComboBox<String> combo : java.util.List.of(cbEstado, cbTipoEstreno)) {
            combo.setFont(Tema.CUERPO); combo.setBackground(Tema.SUPERFICIE);
            combo.setForeground(Tema.TEXTO); combo.setPreferredSize(new Dimension(160, Tema.ALTO_CONTROL));
        }
        Tema.aplicarBoton(btnGuardar, true);
        for (JButton boton : new JButton[]{btnActualizar, btnLimpiar, btnEliminar}) Tema.aplicarBoton(boton, false);
        btnGuardar.setText("Guardar película"); btnActualizar.setText("Guardar cambios");
        btnLimpiar.setText("Nueva película"); btnEliminar.setForeground(Tema.ERROR);

        JPanel pagina = new Pagina();
        pagina.setLayout(new BorderLayout(0, 24));
        pagina.setBackground(Tema.FONDO);
        pagina.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.PAGE_AXIS));
        encabezado.setOpaque(false);
        encabezado.add(Tema.texto("ADMINISTRACIÓN  /  CATÁLOGO", Tema.ETIQUETA, Tema.PRIMARIO));
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(Tema.texto("Películas", Tema.TITULO, Tema.TEXTO));
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(Tema.texto("Agrega películas y prepara su información para la cartelera.", Tema.CUERPO, Tema.SECUNDARIO));
        pagina.add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 24));
        cuerpo.setOpaque(false);
        JPanel formulario = Tema.tarjeta();
        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false); cabecera.add(lblFormulario, BorderLayout.NORTH);
        cabecera.add(Tema.texto("* Campos obligatorios. Fecha y póster son opcionales.", Tema.CUERPO, Tema.SECUNDARIO), BorderLayout.CENTER);
        formulario.add(cabecera, BorderLayout.NORTH);

        JPanel campos = new JPanel(new views.estilos.RejillaAdaptable(2, 240, 12));
        campos.setOpaque(false);
        campos.add(grupo("Título *", txtTitulo)); campos.add(grupo("Género", txtGenero));
        campos.add(grupo("Duración en minutos *", txtDuracion)); campos.add(grupo("Director", txtDirector));
        campos.add(grupo("Estado", cbEstado)); campos.add(grupo("Tipo de estreno", cbTipoEstreno));
        campos.add(grupo("Fecha de estreno (dd/mm/aaaa)", txtFechaEstreno));
        campos.add(grupo("Sinopsis", txtSinopsis));
        JPanel datos = new JPanel(new BorderLayout(0, 12));
        datos.setOpaque(false); datos.add(campos, BorderLayout.NORTH);
        JPanel imagen = new JPanel(new BorderLayout(8, 8));
        imagen.setOpaque(false); imagen.add(grupo("URL o ruta del póster", txtImagenUrl), BorderLayout.NORTH);
        JButton previsualizar = Tema.botonSecundario("Ver póster");
        previsualizar.addActionListener(e -> mostrarPoster());
        JPanel accionesPoster = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        accionesPoster.setOpaque(false); accionesPoster.add(previsualizar);
        imagen.add(accionesPoster, BorderLayout.CENTER);
        datos.add(imagen, BorderLayout.CENTER);
        pnlPoster.setBackground(Tema.FONDO);
        pnlPoster.setPreferredSize(new Dimension(160, 210));
        mostrarPoster();
        JPanel zonaPoster = new JPanel(new BorderLayout());
        zonaPoster.setOpaque(false); zonaPoster.add(pnlPoster, BorderLayout.NORTH);
        JPanel ficha = new JPanel(new BorderLayout(24, 16)) {
            private Boolean estrecho;
            @Override public void doLayout() {
                boolean nuevo = getWidth() < 720;
                if (estrecho == null || nuevo != estrecho) {
                    estrecho = nuevo; remove(zonaPoster);
                    add(zonaPoster, estrecho ? BorderLayout.SOUTH : BorderLayout.EAST);
                }
                super.doLayout();
            }
        };
        ficha.setOpaque(false); ficha.add(datos, BorderLayout.CENTER); ficha.add(zonaPoster, BorderLayout.EAST);
        formulario.add(ficha, BorderLayout.CENTER);
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
        tituloListado.add(Tema.texto("Películas registradas", Tema.SUBTITULO, Tema.TEXTO), BorderLayout.NORTH);
        tituloListado.add(grupo("Buscar en el listado", txtFiltro), BorderLayout.CENTER);
        tituloListado.add(btnRecargar, BorderLayout.SOUTH);
        listado.add(tituloListado, BorderLayout.NORTH);
        JScrollPane tabla = Tema.tabla(tblPeliculas);
        tabla.setPreferredSize(new Dimension(500, 230));
        listado.add(tabla, BorderLayout.CENTER);
        listado.add(lblCantidad, BorderLayout.SOUTH);
        cuerpo.add(listado, BorderLayout.CENTER);
        pagina.add(cuerpo, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(pagina);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
        setModoEdicion(false);
    }

    private JPanel grupo(String texto, JComponent campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 6)); panel.setOpaque(false);
        JLabel etiqueta = Tema.texto(texto, Tema.ETIQUETA, Tema.TEXTO); etiqueta.setLabelFor(campo);
        panel.add(etiqueta, BorderLayout.NORTH); panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    public void mostrarPoster() {
        pnlPoster.removeAll(); pnlPoster.add(new PosterView(getImagenUrl()), BorderLayout.CENTER);
        pnlPoster.revalidate(); pnlPoster.repaint();
    }
    public String getFechaEstreno() { return txtFechaEstreno.getText().trim(); }
    public void setFechaEstreno(String fecha) { txtFechaEstreno.setText(fecha); }
    public String getImagenUrl() { return txtImagenUrl.getText().trim(); }
    public void setImagenUrl(String url) { txtImagenUrl.setText(url); }
    public String getTipoEstreno() {
        String valor = (String) cbTipoEstreno.getSelectedItem();
        return "Sin especificar".equals(valor) ? null : valor;
    }
    public void setTipoEstreno(String tipo) {
        String valor = tipo == null || tipo.isBlank() ? "Sin especificar" : tipo;
        if (((DefaultComboBoxModel<String>) cbTipoEstreno.getModel()).getIndexOf(valor) < 0) cbTipoEstreno.addItem(valor);
        cbTipoEstreno.setSelectedItem(valor);
    }
    public void setModoEdicion(boolean edicion) {
        lblFormulario.setText(edicion ? "Editar película" : "Nueva película");
        btnGuardar.setEnabled(!edicion); btnActualizar.setEnabled(edicion); btnEliminar.setEnabled(edicion);
    }
    public void mostrarMensaje(String texto, boolean error) {
        lblMensaje.setText(texto); lblMensaje.setForeground(error ? Tema.ERROR : Tema.EXITO);
    }
    public void mostrarCantidad(int visibles, int total) { lblCantidad.setText(visibles + " de " + total + " películas · Selecciona una fila para editar."); }
    public void addRecargarListener(java.awt.event.ActionListener listener) { btnRecargar.addActionListener(listener); }

    private static final class Pagina extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return new Dimension(900, 750); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(24, r.height - 24); }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    public void addFiltroDocumentListener(javax.swing.event.DocumentListener listener) {
        txtFiltro.getDocument().addDocumentListener(listener);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtTitulo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtSinopsis = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDuracion = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtGenero = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDirector = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cbEstado = new javax.swing.JComboBox<>();
        btnGuardar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtFiltro = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPeliculas = new javax.swing.JTable();

        jLabel1.setText("Título:");

        jLabel2.setText("Sinopsis:");

        jLabel3.setText("Duración (min):");

        jLabel4.setText("Género:");

        jLabel5.setText("Director:");

        jLabel6.setText("Estado:");

        cbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CARTELERA", "PROXIMAMENTE" }));

        btnGuardar.setText("Guardar");

        btnActualizar.setText("Actualizar");
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnEliminar.setText("Eliminar");

        jLabel7.setText("Filtrar peliículas:");

        tblPeliculas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Título", "Sinopsis", "Duración", "Género", "Director", "Estado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblPeliculas);

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
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(txtSinopsis)
                            .addComponent(txtDuracion))
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtGenero, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(txtDirector)
                            .addComponent(cbEstado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnGuardar)
                        .addGap(18, 18, 18)
                        .addComponent(btnActualizar)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar)
                        .addGap(18, 18, 18)
                        .addComponent(btnEliminar))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(txtGenero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtSinopsis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtDirector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtDuracion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(cbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar)
                    .addComponent(btnActualizar)
                    .addComponent(btnLimpiar)
                    .addComponent(btnEliminar))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLimpiarActionPerformed

    public String getTitulo() {
        return txtTitulo.getText();
    }

    public void setTitulo(String titulo) {
        txtTitulo.setText(titulo);
    }

    public String getSinopsis() {
        return txtSinopsis.getText();
    }

    public void setSinopsis(String sinopsis) {
        txtSinopsis.setText(sinopsis);
    }

    public String getDuracion() {
        return txtDuracion.getText();
    }

    public void setDuracion(String duracion) {
        txtDuracion.setText(duracion);
    }

    public String getGenero() {
        return txtGenero.getText();
    }

    public void setGenero(String genero) {
        txtGenero.setText(genero);
    }

    public String getDirector() {
        return txtDirector.getText();
    }

    public void setDirector(String director) {
        txtDirector.setText(director);
    }

    public String getEstado() {
        return cbEstado.getSelectedItem().toString();
    }

    public void setEstado(String estado) {
        cbEstado.setSelectedItem(estado);
    }

    public String getFiltro() {
        return txtFiltro.getText();
    }

    public javax.swing.JTable getTablaPeliculas() {
        return tblPeliculas;
    }

    public void addGuardarListener(java.awt.event.ActionListener listener) {
        btnGuardar.addActionListener(listener);
    }

    public void addActualizarListener(java.awt.event.ActionListener listener) {
        btnActualizar.addActionListener(listener);
    }

    public void addLimpiarListener(java.awt.event.ActionListener listener) {
        btnLimpiar.addActionListener(listener);
    }

    public void addEliminarListener(java.awt.event.ActionListener listener) {
        btnEliminar.addActionListener(listener);
    }

    public void addFiltroKeyListener(java.awt.event.KeyListener listener) {
        txtFiltro.addKeyListener(listener);
    }

    public void addTablaMouseListener(java.awt.event.MouseListener listener) {
        tblPeliculas.addMouseListener(listener);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JComboBox<String> cbEstado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPeliculas;
    private javax.swing.JTextField txtDirector;
    private javax.swing.JTextField txtDuracion;
    private javax.swing.JTextField txtFiltro;
    private javax.swing.JTextField txtGenero;
    private javax.swing.JTextField txtSinopsis;
    private javax.swing.JTextField txtTitulo;
    // End of variables declaration//GEN-END:variables
}
