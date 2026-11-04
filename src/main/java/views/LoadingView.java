package views;

public class LoadingView extends javax.swing.JDialog {

    public LoadingView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        aplicarEstilos();
    }

    private void aplicarEstilos() {
        javax.swing.JPanel contenido = views.estilos.Tema.tarjeta();
        javax.swing.JPanel texto = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        texto.setOpaque(false);
        texto.add(views.estilos.Tema.texto("CINE", views.estilos.Tema.ETIQUETA, views.estilos.Tema.PRIMARIO), java.awt.BorderLayout.NORTH);
        jLabel1.setText("Preparando todo…");
        jLabel1.setFont(views.estilos.Tema.SUBTITULO); jLabel1.setForeground(views.estilos.Tema.TEXTO);
        texto.add(jLabel1, java.awt.BorderLayout.CENTER);
        texto.add(views.estilos.Tema.texto("Espera un momento, por favor.", views.estilos.Tema.CUERPO,
                views.estilos.Tema.SECUNDARIO), java.awt.BorderLayout.SOUTH);
        contenido.add(texto, java.awt.BorderLayout.NORTH);
        jProgressBar1.setUI(new javax.swing.plaf.basic.BasicProgressBarUI());
        jProgressBar1.setBackground(views.estilos.Tema.FONDO);
        jProgressBar1.setForeground(views.estilos.Tema.PRIMARIO);
        jProgressBar1.setBorderPainted(false);
        jProgressBar1.setPreferredSize(new java.awt.Dimension(320, 6));
        contenido.add(jProgressBar1, java.awt.BorderLayout.SOUTH);
        setContentPane(contenido); pack(); setLocationRelativeTo(getOwner());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setUndecorated(true);

        jLabel1.setText("Cargando, por favor espere...");

        jProgressBar1.setIndeterminate(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
}
