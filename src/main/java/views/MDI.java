package views;

public class MDI extends javax.swing.JFrame {
    public enum Modulo {
        PELICULAS("Películas"), SALAS("Salas"), FUNCIONES("Programar función"),
        TAQUILLA("Venta de boletos"), CORTE_CAJA("Corte de caja"), CONFIGURACION("Configuración");

        private final String titulo;
        Modulo(String titulo) { this.titulo = titulo; }
        public String getTitulo() { return titulo; }
    }

    public MDI() {
        config.Sesion.exigirSesion();
        initComponents();
        configurarNavegacion();
        setMinimumSize(new java.awt.Dimension(850, 600));
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    private void configurarNavegacion() {
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu administracion = new javax.swing.JMenu("Administración");
        javax.swing.JMenuItem funciones = new javax.swing.JMenuItem("Programar función");
        javax.swing.JMenuItem corte = new javax.swing.JMenuItem("Corte de caja");
        javax.swing.JMenuItem configuracion = new javax.swing.JMenuItem("Configuración");
        funciones.addActionListener(e -> navegar(Modulo.FUNCIONES));
        corte.addActionListener(e -> navegar(Modulo.CORTE_CAJA));
        configuracion.addActionListener(e -> navegar(Modulo.CONFIGURACION));
        administracion.add(funciones); administracion.add(corte); administracion.add(configuracion);
        barra.add(administracion); setJMenuBar(barra);
        new controlllers.DashboardController(btnMenuSalas, btnMenuPeliculas, btnCorteCaja, btnVenderTickets,
                btnMenuFunciones, btnConfiguracion, funciones, corte, configuracion, administracion).aplicarPermisosPorRol();
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (isDisplayable()) navegar(config.Sesion.esAdministrador() ? Modulo.PELICULAS : Modulo.TAQUILLA);
        });
    }

    private void navegar(Modulo modulo) {
        utils.Tareas.validar(this, () -> {
            if (modulo == Modulo.TAQUILLA) config.Sesion.exigirVenta();
            else config.Sesion.exigirAdministrador();
            mostrarModulo(modulo);
            models.Usuario usuario = config.Sesion.exigirSesion();
            String nombre = java.util.Objects.toString(usuario.getNombre(), "Usuario");
            setTitle("Sistema de gestión de cine · " + modulo.getTitulo() + " · " + nombre + " (" + usuario.getRol() + ")");
        });
    }

    /** Cada módulo se monta antes de cargar datos, para que los diálogos tengan al MDI como propietario. */
    protected void mostrarModulo(Modulo modulo) {
        switch (modulo) {
            case PELICULAS -> {
                PeliculasView vista = new PeliculasView();
                mostrarVistaCentral(vista);
                new controlllers.PeliculasController(vista);
            }
            case SALAS -> {
                SalasView vista = new SalasView();
                mostrarVistaCentral(vista);
                new controlllers.SalasController(vista);
            }
            case FUNCIONES -> {
                FuncionesView vista = new FuncionesView();
                mostrarVistaCentral(vista);
                new controlllers.FuncionController(vista);
            }
            case TAQUILLA -> {
                TaquillaView vista = new TaquillaView();
                mostrarVistaCentral(vista);
                new controlllers.TaquillaController(this, vista);
            }
            case CORTE_CAJA -> {
                CorteCajaView vista = new CorteCajaView();
                mostrarVistaCentral(vista);
                new controlllers.CorteCajaController(vista);
            }
            case CONFIGURACION -> {
                ConfiguracionView vista = new ConfiguracionView();
                mostrarVistaCentral(vista);
                new controlllers.ConfiguracionController(vista);
            }
        }
    }

    public void mostrarVistaCentral(javax.swing.JPanel vista) {
        panelCentral.removeAll();
        panelCentral.add(vista, java.awt.BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        btnMenuPeliculas = new javax.swing.JButton();
        btnMenuSalas = new javax.swing.JButton();
        btnMenuFunciones = new javax.swing.JButton();
        btnVenderTickets = new javax.swing.JButton();
        btnCorteCaja = new javax.swing.JButton();
        btnConfiguracion = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        panelCentral = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnMenuPeliculas.setText("Peliculas");
        btnMenuPeliculas.addActionListener(this::btnMenuPeliculasActionPerformed);

        btnMenuSalas.setText("Salas");
        btnMenuSalas.addActionListener(this::btnMenuSalasActionPerformed);

        btnMenuFunciones.setText("Funciones");
        btnMenuFunciones.addActionListener(this::btnMenuFuncionesActionPerformed);

        btnVenderTickets.setText("Venta Tickets");
        btnVenderTickets.addActionListener(this::btnVenderTicketsActionPerformed);

        btnCorteCaja.setText("Corte de caja");
        btnCorteCaja.addActionListener(this::btnCorteCajaActionPerformed);

        btnConfiguracion.setText("Configuración");
        btnConfiguracion.addActionListener(this::btnConfiguracionActionPerformed);

        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.addActionListener(this::btnCerrarSesionActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMenuPeliculas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMenuSalas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMenuFunciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnVenderTickets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCorteCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnConfiguracion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCerrarSesion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(btnMenuPeliculas)
                .addGap(24, 24, 24)
                .addComponent(btnMenuSalas)
                .addGap(24, 24, 24)
                .addComponent(btnMenuFunciones)
                .addGap(24, 24, 24)
                .addComponent(btnVenderTickets)
                .addGap(24, 24, 24)
                .addComponent(btnCorteCaja)
                .addGap(24, 24, 24)
                .addComponent(btnConfiguracion)
                .addGap(24, 24, 24)
                .addComponent(btnCerrarSesion)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.LINE_START);
        panelCentral.setPreferredSize(new java.awt.Dimension(563, 525));
        panelCentral.setLayout(new java.awt.BorderLayout());
        getContentPane().add(panelCentral, java.awt.BorderLayout.CENTER);
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnMenuPeliculasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuPeliculasActionPerformed
        navegar(Modulo.PELICULAS);
    }//GEN-LAST:event_btnMenuPeliculasActionPerformed

    private void btnMenuSalasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuSalasActionPerformed
        navegar(Modulo.SALAS);
    }//GEN-LAST:event_btnMenuSalasActionPerformed

    private void btnMenuFuncionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuFuncionesActionPerformed
        navegar(Modulo.FUNCIONES);
    }//GEN-LAST:event_btnMenuFuncionesActionPerformed

    private void btnVenderTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVenderTicketsActionPerformed
        navegar(Modulo.TAQUILLA);
    }//GEN-LAST:event_btnVenderTicketsActionPerformed

    private void btnCorteCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCorteCajaActionPerformed
        navegar(Modulo.CORTE_CAJA);
    }//GEN-LAST:event_btnCorteCajaActionPerformed

    private void btnConfiguracionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfiguracionActionPerformed
        navegar(Modulo.CONFIGURACION);
    }//GEN-LAST:event_btnConfiguracionActionPerformed

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Estás seguro que deseas cerrar sesión?", "Cerrar Sesión", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            config.Sesion.cerrarSesion();
            dispose();
            new Login().setVisible(true);
        }
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    public static void main(String args[]) {
        com.mycompany.sistemagestioncine.SistemaGestionCine.main(args);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMenuPeliculas;
    private javax.swing.JButton btnMenuSalas;
    private javax.swing.JButton btnMenuFunciones;
    private javax.swing.JButton btnVenderTickets;
    private javax.swing.JButton btnCorteCaja;
    private javax.swing.JButton btnConfiguracion;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panelCentral;
    // End of variables declaration//GEN-END:variables
}