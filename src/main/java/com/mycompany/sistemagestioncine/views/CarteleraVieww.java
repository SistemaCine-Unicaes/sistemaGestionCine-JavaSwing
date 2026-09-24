/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.sistemagestioncine.views;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Danie
 */
public class CarteleraVieww extends javax.swing.JPanel {

    /**
     * Creates new form CarteleraVieww
     */
    public CarteleraVieww() {
        initComponents();
        // Cargamos todas las tarjetas al iniciar (sin filtro)
        cargarTarjetasPeliculas("");
    }
    
   private void cargarPoster(String urlString) {
    try {
        if (urlString == null || urlString.trim().isEmpty()) {
            lblPoster.setIcon(null);
            lblPoster.setText("Sin Póster");
            return;
        }

        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        
        java.awt.Image imagenOriginal = javax.imageio.ImageIO.read(connection.getInputStream());

        if (imagenOriginal != null) {
            int ancho = lblPoster.getWidth() > 0 ? lblPoster.getWidth() : 200;
            int alto = lblPoster.getHeight() > 0 ? lblPoster.getHeight() : 300;
            
            java.awt.Image imagenEscalada = imagenOriginal.getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH);
            lblPoster.setIcon(new javax.swing.ImageIcon(imagenEscalada));
            lblPoster.setText("");
        }
    } catch (java.io.IOException e) {
        System.err.println("Error al descargar la imagen: " + e.getMessage());
        lblPoster.setIcon(null);
        lblPoster.setText("Imagen no disponible");
    }
}
   
  private void cargarImagenTarjeta(String urlString, javax.swing.JLabel label) {
    // Verificamos que no sea nulo, vacío, y que realmente empiece con "http"
    if (urlString == null || urlString.trim().isEmpty() || !urlString.trim().toLowerCase().startsWith("http")) {
        label.setText("Sin Póster");
        return;
    }

    new Thread(() -> {
        try {
            // trim() es vital para limpiar espacios guardados por error en la base de datos
            java.net.URL url = new java.net.URL(urlString.trim());
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            
            // Mismo User-Agent exacto que funciona en tu panel lateral
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            connection.setConnectTimeout(5000); // Evita que la tarjeta se quede cargando infinito
            connection.setReadTimeout(5000);
            
            java.awt.Image imagenOriginal = javax.imageio.ImageIO.read(connection.getInputStream());

            if (imagenOriginal != null) {
                java.awt.Image imagenEscalada = imagenOriginal.getScaledInstance(120, 160, java.awt.Image.SCALE_SMOOTH);
                javax.swing.ImageIcon icono = new javax.swing.ImageIcon(imagenEscalada);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    label.setIcon(icono);
                    label.setText(""); 
                });
            }
        } catch (IOException e) {
            // Ahora NetBeans te mostrará la razón exacta si la descarga vuelve a fallar
            System.err.println("Fallo al descargar póster para la tarjeta (" + urlString.trim() + "): " + e.getMessage());
            javax.swing.SwingUtilities.invokeLater(() -> label.setText("Sin Imagen"));
        }
    }).start();
}
    
   private void cargarTarjetasPeliculas(String filtroBusqueda) {
    panelContenedorTarjetas.removeAll();

    String sql = "SELECT id_pelicula, nombre, imagen_url, duracion, genero, fecha_estreno FROM Pelicula WHERE estado = 'CARTELERA'";
    
    if (filtroBusqueda != null && !filtroBusqueda.trim().isEmpty()) {
        sql += " AND nombre ILIKE ?";
    }

    try (java.sql.Connection conn = com.mycompany.sistemagestioncine.config.Conexion.getConexion();
         java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

        if (filtroBusqueda != null && !filtroBusqueda.trim().isEmpty()) {
            ps.setString(1, "%" + filtroBusqueda.trim() + "%");
        }

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idPelicula = rs.getInt("id_pelicula");
                String titulo = rs.getString("nombre");
                String urlImagen = rs.getString("imagen_url");
                int duracion = rs.getInt("duracion");
                String genero = rs.getString("genero");
                java.sql.Date fechaEstreno = rs.getDate("fecha_estreno");
                
                javax.swing.JPanel tarjeta = new javax.swing.JPanel();
                tarjeta.setLayout(new java.awt.BorderLayout(0, 10)); 
                tarjeta.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
                tarjeta.setBackground(java.awt.Color.WHITE); 
                
                javax.swing.JLabel lblTitulo = new javax.swing.JLabel("<html><center><b>" + titulo + "</b></center></html>", javax.swing.SwingConstants.CENTER);
                lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 13));
                
                javax.swing.JLabel lblPosterTarjeta = new javax.swing.JLabel("Cargando...");
                lblPosterTarjeta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                lblPosterTarjeta.setPreferredSize(new java.awt.Dimension(120, 160)); // Reserva el espacio visual

                // Inicia la descarga del póster en segundo plano para esta tarjeta específica
                cargarImagenTarjeta(urlImagen, lblPosterTarjeta);
                
                javax.swing.JButton btnSeleccionar = new javax.swing.JButton("Ver Horarios");
                btnSeleccionar.setBackground(new java.awt.Color(33, 150, 243));
                btnSeleccionar.setForeground(java.awt.Color.WHITE);
                
                btnSeleccionar.addActionListener(e -> {
                    lblTituloPelicula.setText(titulo);
                    lblDuracionPelicula.setText(duracion + " min");
                    lblGeneroPelicula.setText(genero);
                    lblFechaEstreno.setText(fechaEstreno != null ? "Estreno: " + fechaEstreno.toString() : "Estreno: N/A");
                    cargarPoster(urlImagen);
                    cargarHorariosDePelicula(idPelicula);
                });
                
                tarjeta.add(lblPosterTarjeta, java.awt.BorderLayout.CENTER);
                tarjeta.add(lblTitulo, java.awt.BorderLayout.NORTH);
                tarjeta.add(btnSeleccionar, java.awt.BorderLayout.SOUTH);
                
                panelContenedorTarjetas.add(tarjeta);
            }
        }
    } catch (Exception e) {
        System.err.println("Error al cargar la cartelera: " + e.getMessage());
    }
    
    panelContenedorTarjetas.revalidate();
    panelContenedorTarjetas.repaint();
}
    
   private void cargarHorariosDePelicula(int idPelicula) {
    javax.swing.table.DefaultTableModel modeloFunciones = (javax.swing.table.DefaultTableModel) tblHorariosDisponibles.getModel();
    modeloFunciones.setRowCount(0); 

    String sqlFunciones = "SELECT id_funcion, id_sala, fecha_proyeccion, hora_inicio, hora_fin FROM Funcion WHERE id_pelicula = ? AND estado = 'Programada'";
    
    try (java.sql.Connection conn = com.mycompany.sistemagestioncine.config.Conexion.getConexion();
         java.sql.PreparedStatement ps = conn.prepareStatement(sqlFunciones)) {
         
        ps.setInt(1, idPelicula);
        try (java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Object[] filaHorario = {
                    rs.getInt("id_funcion"),
                    "Sala " + rs.getInt("id_sala"),
                    rs.getDate("fecha_proyeccion"),
                    rs.getTime("hora_inicio"),
                    rs.getTime("hora_fin")
                };
                modeloFunciones.addRow(filaHorario);
            }
        }
    } catch (Exception e) {
        System.err.println("Error al cargar las funciones: " + e.getMessage());
    }
}
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        tblHorariosDisponibles = new javax.swing.JTable();
        btnComprarTicket = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        pnlDetallePelicula = new javax.swing.JPanel();
        lblPoster = new javax.swing.JLabel();
        lblTituloPelicula = new javax.swing.JLabel();
        lblDuracionPelicula = new javax.swing.JLabel();
        lblGeneroPelicula = new javax.swing.JLabel();
        lblFechaEstreno = new javax.swing.JLabel();
        txtBuscarCartelera = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        panelContenedorTarjetas = new javax.swing.JPanel();

        tblHorariosDisponibles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Sala", "Fecha", "Hora Inicio", "Hora Fin"
            }
        ));
        jScrollPane2.setViewportView(tblHorariosDisponibles);

        btnComprarTicket.setText("Seleccionar o comprar");

        jLabel2.setText("Buscador");

        pnlDetallePelicula.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED), null));

        lblTituloPelicula.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lblDuracionPelicula.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lblGeneroPelicula.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        lblFechaEstreno.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout pnlDetallePeliculaLayout = new javax.swing.GroupLayout(pnlDetallePelicula);
        pnlDetallePelicula.setLayout(pnlDetallePeliculaLayout);
        pnlDetallePeliculaLayout.setHorizontalGroup(
            pnlDetallePeliculaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetallePeliculaLayout.createSequentialGroup()
                .addGap(136, 136, 136)
                .addGroup(pnlDetallePeliculaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFechaEstreno, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDuracionPelicula, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblGeneroPelicula, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTituloPelicula, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDetallePeliculaLayout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addComponent(lblPoster, javax.swing.GroupLayout.PREFERRED_SIZE, 437, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        pnlDetallePeliculaLayout.setVerticalGroup(
            pnlDetallePeliculaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetallePeliculaLayout.createSequentialGroup()
                .addContainerGap(8, Short.MAX_VALUE)
                .addComponent(lblPoster, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(lblTituloPelicula)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFechaEstreno)
                .addGap(8, 8, 8)
                .addComponent(lblDuracionPelicula)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblGeneroPelicula)
                .addGap(28, 28, 28))
        );

        txtBuscarCartelera.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarCarteleraKeyReleased(evt);
            }
        });

        panelContenedorTarjetas.setLayout(new java.awt.GridLayout(0, 5, 15, 15));
        jScrollPane3.setViewportView(panelContenedorTarjetas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addGap(17, 17, 17))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnComprarTicket, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(75, 75, 75)
                            .addComponent(txtBuscarCartelera, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 168, Short.MAX_VALUE)
                .addComponent(pnlDetallePelicula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel2)
                    .addContainerGap(1115, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtBuscarCartelera, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlDetallePelicula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(btnComprarTicket)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(111, 111, 111)
                    .addComponent(jLabel2)
                    .addContainerGap(727, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtBuscarCarteleraKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarCarteleraKeyReleased
        // TODO add your handling code here:
        
        String textoBusqueda = txtBuscarCartelera.getText();
        cargarTarjetasPeliculas(textoBusqueda);
    
    }//GEN-LAST:event_txtBuscarCarteleraKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnComprarTicket;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblDuracionPelicula;
    private javax.swing.JLabel lblFechaEstreno;
    private javax.swing.JLabel lblGeneroPelicula;
    private javax.swing.JLabel lblPoster;
    private javax.swing.JLabel lblTituloPelicula;
    private javax.swing.JPanel panelContenedorTarjetas;
    private javax.swing.JPanel pnlDetallePelicula;
    private javax.swing.JTable tblHorariosDisponibles;
    private javax.swing.JTextField txtBuscarCartelera;
    // End of variables declaration//GEN-END:variables
}
