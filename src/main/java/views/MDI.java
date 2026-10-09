package views;

public class MDI extends javax.swing.JFrame {
    private BarraLateral barraLateral;
    public enum Modulo {
        CARTELERA("Cartelera"), PELICULAS("Películas"), SALAS("Salas"), FUNCIONES("Programar función"),
        TAQUILLA("Venta de boletos"), CORTE_CAJA("Corte de caja"), CONFIGURACION("Configuración"), USUARIOS("Usuarios");

        private final String titulo;
        Modulo(String titulo) { this.titulo = titulo; }
        public String getTitulo() { return titulo; }
        /** Módulos del cajero; los demás son de administración y no se muestran al cajero. */
        public boolean esOperacion() { return this == CARTELERA || this == TAQUILLA; }
    }

    public MDI() {
        config.Sesion.exigirSesion();
        initComponents();
        configurarEstilos();
        configurarNavegacion();
        setMinimumSize(new java.awt.Dimension(480, 360));
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    private void configurarNavegacion() {
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu modulos = new javax.swing.JMenu("Módulos");
        for (Modulo modulo : Modulo.values()) {
            javax.swing.JMenuItem item = new javax.swing.JMenuItem(modulo.getTitulo());
            boolean permitido = config.Sesion.esAdministrador() || modulo.esOperacion();
            item.setEnabled(permitido); item.setVisible(permitido);
            item.addActionListener(e -> navegar(modulo)); modulos.add(item);
        }
        javax.swing.JMenuItem salir = new javax.swing.JMenuItem("Cerrar sesión");
        salir.addActionListener(e -> btnCerrarSesion.doClick()); modulos.addSeparator(); modulos.add(salir);
        barra.add(modulos);
        javax.swing.JButton menu = new javax.swing.JButton("Mostrar / ocultar menú");
        menu.addActionListener(e -> { barraLateral.setVisible(!barraLateral.isVisible()); revalidate(); });
        barra.add(menu);
        javax.swing.JMenu administracion = new javax.swing.JMenu("Administración");
        javax.swing.JMenuItem funciones = new javax.swing.JMenuItem("Programar función");
        javax.swing.JMenuItem corte = new javax.swing.JMenuItem("Corte de caja");
        javax.swing.JMenuItem configuracion = new javax.swing.JMenuItem("Configuración");
        javax.swing.JMenuItem usuarios = new javax.swing.JMenuItem("Usuarios");
        funciones.addActionListener(e -> navegar(Modulo.FUNCIONES));
        corte.addActionListener(e -> navegar(Modulo.CORTE_CAJA));
        configuracion.addActionListener(e -> navegar(Modulo.CONFIGURACION));
        usuarios.addActionListener(e -> navegar(Modulo.USUARIOS));
        administracion.add(funciones); administracion.add(corte); administracion.add(configuracion); administracion.add(usuarios);
        barra.add(administracion, 0); setJMenuBar(barra);
        barra.setBackground(views.estilos.Tema.SUPERFICIE);
        barra.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, views.estilos.Tema.BORDE));
        for (javax.swing.JMenuItem item : java.util.List.of(administracion, funciones, corte, configuracion, usuarios)) {
            item.setFont(views.estilos.Tema.CUERPO); item.setForeground(views.estilos.Tema.TEXTO);
            item.setBackground(views.estilos.Tema.SUPERFICIE);
        }
        new controlllers.DashboardController(btnMenuSalas, btnMenuPeliculas, btnCorteCaja, btnVenderTickets,
                btnMenuFunciones, btnConfiguracion, btnUsuarios, funciones, corte, configuracion, usuarios, administracion).aplicarPermisosPorRol();
        btnMenuCartelera.setEnabled(btnVenderTickets.isEnabled());
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (isDisplayable()) navegar(config.Sesion.esAdministrador() ? Modulo.PELICULAS : Modulo.TAQUILLA);
        });
    }

    private void navegar(Modulo modulo) {
        utils.Tareas.validar(this, () -> {
            if (modulo.esOperacion()) config.Sesion.exigirVenta();
            else config.Sesion.exigirAdministrador();
            barraLateral.seleccionar(modulo);
            mostrarModulo(modulo);
            models.Usuario usuario = config.Sesion.exigirSesion();
            String nombre = java.util.Objects.toString(usuario.getNombre(), "Usuario");
            setTitle("Sistema de gestión de cine · " + modulo.getTitulo() + " · " + nombre + " (" + usuario.getRol() + ")");
        });
    }

    private void configurarEstilos() {
        java.util.Map<Modulo, javax.swing.JButton> botones = new java.util.EnumMap<>(Modulo.class);
        botones.put(Modulo.CARTELERA, btnMenuCartelera); botones.put(Modulo.PELICULAS, btnMenuPeliculas);
        botones.put(Modulo.SALAS, btnMenuSalas); botones.put(Modulo.FUNCIONES, btnMenuFunciones);
        botones.put(Modulo.TAQUILLA, btnVenderTickets); botones.put(Modulo.CORTE_CAJA, btnCorteCaja);
        botones.put(Modulo.CONFIGURACION, btnConfiguracion); botones.put(Modulo.USUARIOS, btnUsuarios);
        getContentPane().remove(jPanel1);
        barraLateral = new BarraLateral(botones, btnCerrarSesion, config.Sesion.exigirSesion());
        getContentPane().add(barraLateral, java.awt.BorderLayout.LINE_START);
        panelCentral.setBackground(views.estilos.Tema.FONDO);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            private Boolean compacto;
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                boolean nuevo = getWidth() < 1050;
                if (compacto == null || compacto != nuevo) {
                    compacto = nuevo; barraLateral.setVisible(!nuevo); revalidate();
                }
            }
        });
    }

    /** Cada módulo se monta antes de cargar datos, para que los diálogos tengan al MDI como propietario. */
    protected void mostrarModulo(Modulo modulo) {
        switch (modulo) {
            case CARTELERA -> {
                CarteleraView vista = new CarteleraView();
                mostrarVistaCentral(vista);
                new controlllers.CarteleraController(this, vista);
            }
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
            case USUARIOS -> {
                UsuariosView vista = new UsuariosView();
                mostrarVistaCentral(vista);
                new controlllers.UsuariosController(vista);
            }
        }
    }

    public void abrirHorarios(models.Pelicula pelicula) {
        config.Sesion.exigirVenta();
        barraLateral.seleccionar(Modulo.CARTELERA);
        setTitle("Sistema de gestión de cine · Horarios · " + pelicula.getNombre());
        mostrarHorarios(pelicula);
    }

    protected void mostrarHorarios(models.Pelicula pelicula) {
        HorariosPeliculaView vista = new HorariosPeliculaView(pelicula);
        mostrarVistaCentral(vista);
        new controlllers.HorariosPeliculaController(this, vista, pelicula.getIdPelicula());
    }

    public void volverACartelera() { navegar(Modulo.CARTELERA); }

    public void abrirTaquilla(int idFuncion) {
        config.Sesion.exigirVenta();
        barraLateral.seleccionar(Modulo.TAQUILLA);
        TaquillaView vista = new TaquillaView();
        mostrarVistaCentral(vista);
        models.Usuario usuario = config.Sesion.exigirSesion();
        setTitle("Sistema de gestión de cine · Venta de boletos · "
                + java.util.Objects.toString(usuario.getNombre(), "Usuario") + " (" + usuario.getRol() + ")");
        new controlllers.TaquillaController(this, vista, idFuncion);
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
        btnMenuCartelera = new javax.swing.JButton();
        btnMenuPeliculas = new javax.swing.JButton();
        btnMenuSalas = new javax.swing.JButton();
        btnMenuFunciones = new javax.swing.JButton();
        btnVenderTickets = new javax.swing.JButton();
        btnCorteCaja = new javax.swing.JButton();
        btnConfiguracion = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        panelCentral = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnMenuCartelera.setText("Cartelera");
        btnMenuCartelera.addActionListener(this::btnMenuCarteleraActionPerformed);

        btnMenuPeliculas.setText("Películas");
        btnMenuPeliculas.addActionListener(this::btnMenuPeliculasActionPerformed);

        btnMenuSalas.setText("Salas");
        btnMenuSalas.addActionListener(this::btnMenuSalasActionPerformed);

        btnMenuFunciones.setText("Funciones");
        btnMenuFunciones.addActionListener(this::btnMenuFuncionesActionPerformed);

        btnVenderTickets.setText("Venta de boletos");
        btnVenderTickets.addActionListener(this::btnVenderTicketsActionPerformed);

        btnCorteCaja.setText("Corte de caja");
        btnCorteCaja.addActionListener(this::btnCorteCajaActionPerformed);

        btnConfiguracion.setText("Configuración");
        btnConfiguracion.addActionListener(this::btnConfiguracionActionPerformed);

        btnUsuarios.setText("Usuarios");
        btnUsuarios.addActionListener(this::btnUsuariosActionPerformed);

        btnCerrarSesion.setText("Cerrar Sesión");
        btnCerrarSesion.addActionListener(this::btnCerrarSesionActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMenuCartelera, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMenuPeliculas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMenuSalas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMenuFunciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnVenderTickets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCorteCaja, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnConfiguracion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCerrarSesion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(btnMenuCartelera)
                .addGap(24, 24, 24)
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
                .addComponent(btnUsuarios)
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

    private void btnMenuCarteleraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMenuCarteleraActionPerformed
        navegar(Modulo.CARTELERA);
    }//GEN-LAST:event_btnMenuCarteleraActionPerformed

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

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosActionPerformed
        navegar(Modulo.USUARIOS);
    }//GEN-LAST:event_btnUsuariosActionPerformed

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
    private javax.swing.JButton btnMenuCartelera;
    private javax.swing.JButton btnMenuPeliculas;
    private javax.swing.JButton btnMenuSalas;
    private javax.swing.JButton btnMenuFunciones;
    private javax.swing.JButton btnVenderTickets;
    private javax.swing.JButton btnCorteCaja;
    private javax.swing.JButton btnConfiguracion;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panelCentral;
    // End of variables declaration//GEN-END:variables
}
