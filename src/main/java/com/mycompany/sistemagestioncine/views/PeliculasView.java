package com.mycompany.sistemagestioncine.views;

import com.mycompany.sistemagestioncine.dao.PeliculaDAO;
import com.mycompany.sistemagestioncine.models.Pelicula;

public class PeliculasView extends javax.swing.JPanel {
    
    private int idSeleccionado = -1;
    
    private final com.mycompany.sistemagestioncine.dao.PeliculaDAO dao;
    private int idPeliculaSeleccionada = -1;

    public PeliculasView() {
        initComponents();
        dao = new com.mycompany.sistemagestioncine.dao.PeliculaDAO(com.mycompany.sistemagestioncine.config.Conexion.getConexion());
        cargarTabla();
        
            
    }
    
    private void cargarTabla() {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblPeliculas.getModel();
        modelo.setColumnIdentifiers(new String[]{"ID", "Título", "Sinopsis", "Duración", "Género", "Director", "Estado"});
        modelo.setRowCount(0); 
        
        java.util.List<com.mycompany.sistemagestioncine.models.Pelicula> lista = dao.obtenerTodas();
        for (com.mycompany.sistemagestioncine.models.Pelicula p : lista) {
            modelo.addRow(new Object[]{
                p.getIdPelicula(), p.getNombre(), p.getSinopsis(), p.getDuracion(), p.getGenero(), p.getDirector(), p.getEstado()
            });
        }
    }
    
    private void cargarDatosExtraPelicula(int id) {
    String sql = "SELECT imagen_url, fecha_estreno, tipo_estreno FROM Pelicula WHERE id_pelicula = ?";
    
    try (java.sql.Connection conn = com.mycompany.sistemagestioncine.config.Conexion.getConexion();
         java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
         
        ps.setInt(1, id);
        
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                // Asignamos la imagen
                txtImagenUrl.setText(rs.getString("imagen_url") != null ? rs.getString("imagen_url") : "");
                
                // Asignamos la fecha (ajusta el nombre del txt si es diferente)
                java.sql.Date fecha = rs.getDate("fecha_estreno");
                txtFechaEstreno.setText(fecha != null ? fecha.toString() : "");
                
                // Asignamos el tipo de estreno (ajusta el nombre del combobox si es diferente)
                String tipo = rs.getString("tipo_estreno");
                if (tipo != null) {
                    cbxTipoEstreno.setSelectedItem(tipo); 
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Error al cargar datos adicionales: " + e.getMessage());
    }
    
    
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
        cbxTipoEstreno = new javax.swing.JComboBox<>();
        btnGuardar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtFiltro = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPeliculas = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        txtImagenUrl = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtFechaEstreno = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        cbEstado = new javax.swing.JComboBox<>();

        setName(""); // NOI18N

        jLabel1.setText("Título:");

        jLabel2.setText("Sinopsis:");

        jLabel3.setText("Duración (min):");

        jLabel4.setText("Género:");

        jLabel5.setText("Director:");

        jLabel6.setText("Estado:");

        cbxTipoEstreno.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MUNDIAL", "ESTANDAR" }));
        cbxTipoEstreno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxTipoEstrenoActionPerformed(evt);
            }
        });

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

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
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        jLabel7.setText("Filtrar peliículas:");

        txtFiltro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFiltroKeyReleased(evt);
            }
        });

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
        tblPeliculas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPeliculasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPeliculas);

        jLabel8.setText("Poster");

        jLabel9.setText("Fecha de Estreno");

        txtFechaEstreno.setText("Formato: YYYY-MM-DD");
        txtFechaEstreno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFechaEstrenoActionPerformed(evt);
            }
        });

        jLabel10.setText("Tipo de estreno");

        cbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CARTELERA", "PROXIMAMENTE" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnGuardar)
                                .addGap(18, 18, 18)
                                .addComponent(btnActualizar)
                                .addGap(18, 18, 18)
                                .addComponent(btnLimpiar)
                                .addGap(18, 18, 18)
                                .addComponent(btnEliminar)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtFechaEstreno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel8)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtImagenUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                    .addComponent(cbEstado, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxTipoEstreno, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(txtTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)
                                    .addComponent(txtGenero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel10)
                                .addGap(6, 6, 6)))
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
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnGuardar)
                                    .addComponent(btnActualizar)
                                    .addComponent(btnLimpiar)
                                    .addComponent(btnEliminar))
                                .addGap(25, 25, 25))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8)
                                    .addComponent(txtImagenUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(txtFechaEstreno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(cbxTipoEstreno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void limpiarCampos() {
     idPeliculaSeleccionada = -1;
        txtTitulo.setText("");
        txtSinopsis.setText("");
        txtDuracion.setText("");
        txtGenero.setText("");
        txtDirector.setText("");
        cbxTipoEstreno.setSelectedIndex(0);
        txtFechaEstreno.setText("");
        txtImagenUrl.setText("");

}
    
    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarActionPerformed
        // Validamos usando nuestra variable global en lugar de la selección de la tabla
    if (idSeleccionado == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una película de la tabla primero");
        return;
    }

    String sql = "UPDATE Pelicula SET nombre = ?, sinopsis = ?, duracion = ?, genero = ?, director = ?, estado = CAST(? AS estado_pelicula), imagen_url = ?, fecha_estreno = ?, tipo_estreno = ? WHERE id_pelicula = ?";

    try (java.sql.Connection conn = com.mycompany.sistemagestioncine.config.Conexion.getConexion();
         java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, txtTitulo.getText().trim());
        ps.setString(2, txtSinopsis.getText().trim());
        ps.setInt(3, Integer.parseInt(txtDuracion.getText().trim()));
        ps.setString(4, txtGenero.getText().trim());
        ps.setString(5, txtDirector.getText().trim());
        ps.setString(6, cbEstado.getSelectedItem().toString());
        ps.setString(7, txtImagenUrl.getText().trim());
        ps.setDate(8, java.sql.Date.valueOf(txtFechaEstreno.getText().trim()));
        ps.setString(9, cbxTipoEstreno.getSelectedItem().toString());
        ps.setInt(10, idSeleccionado);

        int filasAfectadas = ps.executeUpdate();
if (filasAfectadas > 0) {
    javax.swing.JOptionPane.showMessageDialog(this, "Operación realizada correctamente.");
    
    idSeleccionado = -1; // Reiniciamos la variable de control
    limpiarCampos();     // Vaciamos los JTextFields
    cargarTabla();       // <-- ESTA LÍNEA RECARGA LA TABLA VISUALMENTE AL INSTANTE
} else {
    javax.swing.JOptionPane.showMessageDialog(this, "No se pudo completar la operación.");
}

    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        System.err.println("Error SQL en Actualizar: " + e.getMessage());
    }
    }//GEN-LAST:event_btnActualizarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        String nombre = txtTitulo.getText().trim();
    String sinopsis = txtSinopsis.getText().trim();
    String genero = txtGenero.getText().trim();
    String director = txtDirector.getText().trim();
    String tipoEstreno = cbxTipoEstreno.getSelectedItem().toString(); // Si usas JComboBox, sería cbxTipoEstreno.getSelectedItem().toString()
    String imagenUrl = txtImagenUrl.getText().trim();
    String duracionStr = txtDuracion.getText().trim();
    String fechaStr = txtFechaEstreno.getText().trim();

    // 2. Validaciones básicas de campos vacíos
    if (nombre.isEmpty() || duracionStr.isEmpty() || fechaStr.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Por favor, complete los campos obligatorios (Nombre, Duración, Fecha).", 
            "Campos incompletos", 
            javax.swing.JOptionPane.WARNING_MESSAGE);
        return; // Detiene la ejecución si faltan datos
    }

    // 3. Conversión y validación de la duración (debe ser número entero)
    int duracion = 0;
    try {
        duracion = Integer.parseInt(duracionStr);
    } catch (NumberFormatException e) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "La duración debe ser un número entero (ej. 120).", 
            "Error de formato", 
            javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 4. Conversión y validación de la fecha
    java.sql.Date fechaEstreno = null;
    try {
        fechaEstreno = java.sql.Date.valueOf(fechaStr);
    } catch (IllegalArgumentException e) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "La fecha debe tener exactamente el formato YYYY-MM-DD (ej. 2026-10-15).", 
            "Error de formato", 
            javax.swing.JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 5. Crear el objeto Pelicula y asignarle los valores
    Pelicula peliculaNueva = new Pelicula();
    peliculaNueva.setNombre(nombre);
    peliculaNueva.setSinopsis(sinopsis);
    peliculaNueva.setDuracion(duracion);
    peliculaNueva.setGenero(genero);
    peliculaNueva.setDirector(director);
    peliculaNueva.setFechaEstreno(fechaEstreno);
    peliculaNueva.setTipoEstreno(tipoEstreno);
    peliculaNueva.setImagenUrl(imagenUrl);
    peliculaNueva.setEstado("CARTELERA"); // Valor por defecto

    // 6. Conectar a la BD y guardar usando el DAO
    try (java.sql.Connection conn = com.mycompany.sistemagestioncine.config.Conexion.getConexion()) {
        
        PeliculaDAO peliculaDAO = new PeliculaDAO(conn);
        
        // Asumiendo que tu método en PeliculaDAO se llama insertar() o guardar()
        boolean guardado = peliculaDAO.insertarPelicula(peliculaNueva); 

        if (guardado) {
            javax.swing.JOptionPane.showMessageDialog(this, "Película guardada exitosamente en la base de datos.");
            
            // Opcional: Limpiar los campos después de guardar
            txtTitulo.setText("");
            txtSinopsis.setText("");
            txtDuracion.setText("");
            txtGenero.setText("");
            txtDirector.setText("");
            txtFechaEstreno.setText("");
            txtImagenUrl.setText("");
            
            limpiarCampos();     // Vaciamos los JTextFields
            cargarTabla();
            
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "No se pudo guardar la película. Revisa la consola para más detalles.", 
                "Error interno", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Error de conexión a la base de datos: " + e.getMessage(), 
            "Error", 
            javax.swing.JOptionPane.ERROR_MESSAGE);
    }
        
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        idPeliculaSeleccionada = -1;
        txtTitulo.setText("");
        txtSinopsis.setText("");
        txtDuracion.setText("");
        txtGenero.setText("");
        txtDirector.setText("");
        txtFechaEstreno.setText("");
        txtImagenUrl.setText("");
        cbxTipoEstreno.setSelectedIndex(0);
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (idSeleccionado == -1) {
        javax.swing.JOptionPane.showMessageDialog(this, "Seleccione una película de la tabla primero");
        return;
    }

    int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar esta película?", "Confirmar Eliminación", javax.swing.JOptionPane.YES_NO_OPTION);
    
    if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
        String sql = "DELETE FROM Pelicula WHERE id_pelicula = ?";
        
        try (java.sql.Connection conn = com.mycompany.sistemagestioncine.config.Conexion.getConexion();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, idSeleccionado);
            
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Película eliminada correctamente.");
                
                // Es vital reiniciar la variable a -1 después de borrar para evitar errores accidentales
                idSeleccionado = -1; 
                
                // Aquí puedes llamar a tu método limpiarCampos() y cargarTabla() para refrescar la vista
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "No se pudo eliminar la película.");
            }
            
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            System.err.println("Error SQL en Eliminar: " + e.getMessage());
        }
    }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void tblPeliculasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPeliculasMouseClicked
      int filaSeleccionada = tblPeliculas.getSelectedRow();
    
    if (filaSeleccionada >= 0) {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblPeliculas.getModel();
        
        // Capturamos el ID de la columna 0 para usarlo en la actualización y en la consulta extra
        idSeleccionado = Integer.parseInt(modelo.getValueAt(filaSeleccionada, 0).toString());
        
        txtTitulo.setText(modelo.getValueAt(filaSeleccionada, 1) != null ? modelo.getValueAt(filaSeleccionada, 1).toString() : "");
        txtSinopsis.setText(modelo.getValueAt(filaSeleccionada, 2) != null ? modelo.getValueAt(filaSeleccionada, 2).toString() : "");       
        txtDuracion.setText(modelo.getValueAt(filaSeleccionada, 3) != null ? modelo.getValueAt(filaSeleccionada, 3).toString() : "");       
        txtGenero.setText(modelo.getValueAt(filaSeleccionada, 4) != null ? modelo.getValueAt(filaSeleccionada, 4).toString() : "");       
        txtDirector.setText(modelo.getValueAt(filaSeleccionada, 5) != null ? modelo.getValueAt(filaSeleccionada, 5).toString() : "");       
        cbEstado.setSelectedItem(modelo.getValueAt(filaSeleccionada, 6).toString());

        cargarDatosExtraPelicula(idSeleccionado);
        }
    
    
    
  
    }//GEN-LAST:event_tblPeliculasMouseClicked

    private void txtFiltroKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFiltroKeyReleased
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblPeliculas.getModel();
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> trs = new javax.swing.table.TableRowSorter<>(modelo);
        tblPeliculas.setRowSorter(trs);
        
        trs.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + txtFiltro.getText(), 0,1));
    }//GEN-LAST:event_txtFiltroKeyReleased

    private void txtFechaEstrenoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFechaEstrenoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFechaEstrenoActionPerformed

    private void cbxTipoEstrenoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxTipoEstrenoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbxTipoEstrenoActionPerformed

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
        return cbxTipoEstreno.getSelectedItem().toString();
    }

    public void setEstado(String estado) {
        cbxTipoEstreno.setSelectedItem(estado);
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
    private javax.swing.JComboBox<String> cbxTipoEstreno;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPeliculas;
    private javax.swing.JTextField txtDirector;
    private javax.swing.JTextField txtDuracion;
    private javax.swing.JTextField txtFechaEstreno;
    private javax.swing.JTextField txtFiltro;
    private javax.swing.JTextField txtGenero;
    private javax.swing.JTextField txtImagenUrl;
    private javax.swing.JTextField txtSinopsis;
    private javax.swing.JTextField txtTitulo;
    // End of variables declaration//GEN-END:variables
}