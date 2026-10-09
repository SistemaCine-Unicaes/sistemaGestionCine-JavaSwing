package views;

import javax.swing.JToggleButton;
import views.estilos.Tema;

public class MapaAsientosView extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MapaAsientosView.class.getName());

    /**
     * Creates new form MapaAsientosView
     */
    public MapaAsientosView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        configurarControles();
    }
    
    private final java.util.Map<Integer, JToggleButton> botones = new java.util.LinkedHashMap<>();
    private final javax.swing.JButton confirmar = new javax.swing.JButton("Confirmar compra");
    private int cantidadRequerida;
    private java.util.List<models.Asiento> asientosActuales = java.util.List.of();
    private java.util.Set<Integer> vendidosActuales = java.util.Set.of();
    private final javax.swing.JTextArea ayudaSeleccion = new javax.swing.JTextArea();

    private void configurarControles() {
        getContentPane().removeAll();
        panelCuadricula = new PlanoAsientosPanel();
        // Fondo general de la ventana
        getContentPane().setBackground(Tema.FONDO);
        setLayout(new java.awt.BorderLayout(8, 8));
        
        // Indicador superior de la Pantalla
        javax.swing.JLabel lblPantalla = new javax.swing.JLabel("P A N T A L L A", javax.swing.SwingConstants.CENTER);
        lblPantalla.setFont(Tema.CUERPO.deriveFont(java.awt.Font.BOLD, 16f));
        lblPantalla.setForeground(Tema.TEXTO);
        lblPantalla.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createEmptyBorder(16, 0, 0, 0), // Margen superior
            javax.swing.BorderFactory.createMatteBorder(0, 0, 4, 0, Tema.PRIMARIO) // Línea inferior granate
        ));
        add(lblPantalla, java.awt.BorderLayout.NORTH);

        // Cuadrícula central
        panelCuadricula.setBackground(Tema.FONDO);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(panelCuadricula);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder()); // Quitar borde 3D
        scroll.setPreferredSize(new java.awt.Dimension(640, 415));
        add(scroll, java.awt.BorderLayout.CENTER);

        // Panel inferior de acciones (Reestructurado para alojar el label de capacidad)
        javax.swing.JPanel acciones = new javax.swing.JPanel(new java.awt.BorderLayout());
        acciones.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        // Panel inferior de acciones
        javax.swing.JPanel acciones = new javax.swing.JPanel();
        acciones.setLayout(new views.estilos.RejillaAdaptable(2, 180, 8));
        acciones.setBackground(Tema.FONDO);
        
        // Contenedor para los botones alineados a la derecha
        javax.swing.JPanel panelBotones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        panelBotones.setBackground(Tema.FONDO);
        
        javax.swing.JButton cancelar = new javax.swing.JButton("Cancelar");
        cancelar.addActionListener(e -> dispose());
        
        // Estilo plano para los botones inferiores
        javax.swing.JButton[] btns = {confirmar, cancelar};
        for (javax.swing.JButton btn : btns) {
            btn.setFont(Tema.CUERPO.deriveFont(java.awt.Font.BOLD));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 24, 10, 24));
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
        confirmar.setBackground(Tema.PRIMARIO); // Granate
        cancelar.setBackground(Tema.SECUNDARIO); // Color secundario

        panelBotones.add(confirmar); 
        panelBotones.add(cancelar);
        
        // Configurar el label de capacidad y moverlo al panel de acciones para protegerlo
        lblCapacidad.setFont(Tema.CUERPO.deriveFont(java.awt.Font.BOLD, 14f));
        lblCapacidad.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        
        acciones.add(lblCapacidad, java.awt.BorderLayout.WEST);
        acciones.add(panelBotones, java.awt.BorderLayout.EAST);
        
        add(acciones, java.awt.BorderLayout.SOUTH);
        acciones.add(confirmar); 
        acciones.add(cancelar);
        ayudaSeleccion.setEditable(false); ayudaSeleccion.setFocusable(false);
        ayudaSeleccion.setLineWrap(true); ayudaSeleccion.setWrapStyleWord(true);
        ayudaSeleccion.setFont(Tema.CUERPO); ayudaSeleccion.setRows(3);
        ayudaSeleccion.setBackground(Tema.FONDO);
        ayudaSeleccion.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 4, 12));
        javax.swing.JPanel pie = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        javax.swing.JScrollPane scrollAyuda = new javax.swing.JScrollPane(ayudaSeleccion);
        scrollAyuda.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scrollAyuda.setPreferredSize(new java.awt.Dimension(0, 84));
        scrollAyuda.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pie.setBackground(Tema.FONDO); pie.add(scrollAyuda, java.awt.BorderLayout.NORTH);
        pie.add(acciones, java.awt.BorderLayout.SOUTH);
        add(pie, java.awt.BorderLayout.SOUTH);
        
        confirmar.setEnabled(false);
        pack();
        views.estilos.Adaptable.limitarVentana(this, 900, 640);
    }

    public void mostrarAsientos(java.util.List<models.Asiento> asientos, java.util.Set<Integer> vendidos, int cantidad) {
        cantidadRequerida = cantidad;
        botones.clear(); 
        panelCuadricula.removeAll();
        
        // Variables para el cálculo de capacidad
        int totalAsientos = asientos.size();
        int asientosOcupados = 0;

        panelCuadricula.setLayout(new java.awt.GridBagLayout());
        asientosActuales = java.util.List.copyOf(asientos);
        vendidosActuales = java.util.Set.copyOf(vendidos);
        botones.clear(); panelCuadricula.removeAll();
        java.util.Map<String, Integer> filas = new java.util.LinkedHashMap<>();
        
        for (models.Asiento asiento : asientos) {
            int fila = filas.computeIfAbsent(asiento.getFila(), key -> filas.size());
            JToggleButton boton = new JToggleButton(asiento.getFila() + "-" + asiento.getNumero());
            boton.setToolTipText(asiento.getTipoDeAsiento() + " · " + asiento.getEstado());
            
            boolean vendido = vendidos.contains(asiento.getIdAsiento()) || "Vendido".equals(asiento.getEstado());
            boolean disponible = "Disponible".equals(asiento.getEstado()) && !vendido;
            boton.setEnabled(disponible);
            
            // Conteo de asientos ocupados para la métrica
            if (vendido) {
                asientosOcupados++;
            }
            
            // Diseño base de la butaca (plano y cuadrado)
            boton.setFont(Tema.ETIQUETA);
            boton.setFocusPainted(false);
            boton.setPreferredSize(new java.awt.Dimension(55, 45));
            boton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 225, 232)));
            
            // Aplicar colores de la guía
            if (vendido) {
                boton.setBackground(Tema.TEXTO); // Ocupado (Azul oscuro)
                boton.setForeground(java.awt.Color.WHITE);
            } else if (disponible) {
                boton.setBackground(java.awt.Color.WHITE); // Disponible (Blanco)
                boton.setForeground(Tema.TEXTO);
            } else {
                boton.setBackground(java.awt.Color.LIGHT_GRAY); // Mantenimiento
                boton.setForeground(java.awt.Color.DARK_GRAY);
            }

            boton.addActionListener(e -> {
                // Prevenir seleccionar más de los permitidos
                if (boton.isSelected() && getAsientosSeleccionados().size() > cantidadRequerida) {
                    boton.setSelected(false);
                    utils.Tareas.error(this, "Solo puedes seleccionar " + cantidadRequerida + " asientos.");
                    return;
                }
                
                // Alternar color al hacer clic
                if (boton.isSelected()) {
                    boton.setBackground(Tema.PRIMARIO); // Seleccionado (Granate)
                    boton.setForeground(java.awt.Color.WHITE);
                } else {
                    boton.setBackground(java.awt.Color.WHITE); // Vuelve a disponible
                    boton.setForeground(Tema.TEXTO);
                }
                
                actualizarSeleccion();
            });
            
            ((PlanoAsientosPanel)panelCuadricula).colocar(boton, asiento, fila);
            botones.put(asiento.getIdAsiento(), boton);
        }
        
        // Cálculo del porcentaje y actualización del Label
        double porcentaje = totalAsientos > 0 ? ((double) asientosOcupados / totalAsientos) * 100 : 0;
        lblCapacidad.setText(String.format("Ocupación: %.0f%%", porcentaje));
        
        if (porcentaje >= 90) {
            lblCapacidad.setForeground(java.awt.Color.RED);
        } else if (porcentaje >= 50) {
            lblCapacidad.setForeground(java.awt.Color.ORANGE);
        } else {
            lblCapacidad.setForeground(new java.awt.Color(46, 204, 113)); // Verde para salas vacías
        }

        confirmar.setEnabled(false);
        panelCuadricula.revalidate(); 
        panelCuadricula.repaint();
        actualizarSeleccion();
        panelCuadricula.revalidate(); panelCuadricula.repaint();
    }

    private void actualizarSeleccion() {
        java.util.List<Integer> seleccionados = getAsientosSeleccionados();
        java.util.List<models.Asiento> huecos = services.AsientosContiguos.huecosNuevos(
                asientosActuales, vendidosActuales, seleccionados);
        confirmar.setEnabled(seleccionados.size() == cantidadRequerida && huecos.isEmpty());
        ayudaSeleccion.setForeground(huecos.isEmpty() ? Tema.SECUNDARIO : Tema.ERROR);
        ayudaSeleccion.setText(huecos.isEmpty()
                ? seleccionados.size() + " de " + cantidadRequerida + " asientos seleccionados. Evita dejar un asiento libre entre asientos vendidos o seleccionados."
                : services.AsientosContiguos.mensaje(huecos));
        ayudaSeleccion.setCaretPosition(0);
    }

    public java.util.List<Integer> getAsientosSeleccionados() {
        return botones.entrySet().stream().filter(e -> e.getValue().isSelected())
                .map(java.util.Map.Entry::getKey).toList();
    }

    public void addConfirmarListener(java.awt.event.ActionListener listener) { confirmar.addActionListener(listener); }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCuadricula = new javax.swing.JPanel();
        lblCapacidad = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelCuadricula.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblCapacidad.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCapacidad.setText("Ocupación: 0%");

        javax.swing.GroupLayout panelCuadriculaLayout = new javax.swing.GroupLayout(panelCuadricula);
        panelCuadricula.setLayout(panelCuadriculaLayout);
        panelCuadriculaLayout.setHorizontalGroup(
            panelCuadriculaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCuadriculaLayout.createSequentialGroup()
                .addContainerGap(456, Short.MAX_VALUE)
                .addComponent(lblCapacidad, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        panelCuadriculaLayout.setVerticalGroup(
            panelCuadriculaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCuadriculaLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(lblCapacidad)
                .addContainerGap(368, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCuadricula, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCuadricula, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MapaAsientosView dialog = new MapaAsientosView(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCapacidad;
    private javax.swing.JPanel panelCuadricula;
    // End of variables declaration//GEN-END:variables
}
