package views;

import java.awt.*;
import javax.swing.*;
import views.estilos.Tema;

public class Login extends javax.swing.JFrame {
    private final JCheckBox mostrarPassword = new JCheckBox("Mostrar contraseña");

    public Login() {
        initComponents();
        configurarEstilos();
        getRootPane().setDefaultButton(btnIngresar);
        new controlllers.LoginController(this);
    }

    public void limpiarPassword() { txtPassword.setText(""); mostrarPassword.setSelected(false); }

    private void configurarEstilos() {
        setTitle("Cine · Iniciar sesión");
        Tema.aplicarCampo(txtUsername); Tema.aplicarCampo(txtPassword);
        Tema.aplicarBoton(btnIngresar, true); btnIngresar.setText("Entrar al sistema");
        txtUsername.setColumns(20); txtPassword.setColumns(20); txtPassword.setEchoChar('•');
        mostrarPassword.setOpaque(false); mostrarPassword.setFont(Tema.CUERPO);
        mostrarPassword.setForeground(Tema.SECUNDARIO);
        mostrarPassword.addItemListener(e -> txtPassword.setEchoChar(mostrarPassword.isSelected() ? (char) 0 : '•'));
        JPanel pagina = new JPanel(new GridBagLayout()); pagina.setBackground(Tema.FONDO);
        pagina.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JPanel tarjeta = new JPanel(new BorderLayout()) {
            private Component portada;
            @Override public void doLayout() {
                boolean amplia = getWidth() >= 712;
                if (portada == null && getComponentCount() == 2) portada = getComponent(0);
                if (portada != null) {
                    if (amplia) {
                        portada.setPreferredSize(new Dimension(getWidth()/2,520));
                        if (portada.getParent()==null) add(portada,BorderLayout.WEST);
                    } else if (portada.getParent()==this) remove(portada);
                }
                super.doLayout();
            }
        };
        tarjeta.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        tarjeta.setPreferredSize(new Dimension(840, 520));
        JPanel identidad = new Portada();
        identidad.setLayout(new BorderLayout());
        identidad.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        JPanel marca = new JPanel(); marca.setOpaque(false);
        marca.setLayout(new BoxLayout(marca, BoxLayout.PAGE_AXIS));
        marca.add(Tema.texto("CINE", Tema.TITULO.deriveFont(44f), Color.WHITE));
        marca.add(Box.createVerticalStrut(8));
        marca.add(Tema.texto("Sistema de gestión", Tema.CUERPO, new Color(223, 225, 232)));
        marca.add(Box.createVerticalStrut(44));
        marca.add(Tema.texto("Detrás de cada", Tema.TITULO.deriveFont(26f), Color.WHITE));
        marca.add(Tema.texto("gran función.", Tema.TITULO.deriveFont(26f), Color.WHITE));
        identidad.add(marca, BorderLayout.NORTH);
        identidad.add(Tema.texto("PELÍCULAS  ·  SALAS  ·  BOLETOS", Tema.ETIQUETA, new Color(223, 225, 232)), BorderLayout.SOUTH);
        tarjeta.add(identidad,BorderLayout.WEST);

        JPanel acceso = new JPanel(new GridBagLayout()); acceso.setBackground(Tema.SUPERFICIE);
        acceso.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        JPanel formulario = new JPanel(); formulario.setOpaque(false);
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.PAGE_AXIS));
        agregar(formulario, Tema.texto("BIENVENIDO", Tema.ETIQUETA, Tema.PRIMARIO), 10);
        agregar(formulario, Tema.texto("Iniciar sesión", Tema.TITULO, Tema.TEXTO), 10);
        agregar(formulario, Tema.texto("Tu cine, en un solo lugar.", Tema.CUERPO, Tema.SECUNDARIO), 28);
        jLabel1.setText("Usuario"); jLabel1.setFont(Tema.ETIQUETA); jLabel1.setForeground(Tema.TEXTO); jLabel1.setLabelFor(txtUsername);
        jLabel2.setText("Contraseña"); jLabel2.setFont(Tema.ETIQUETA); jLabel2.setForeground(Tema.TEXTO); jLabel2.setLabelFor(txtPassword);
        agregar(formulario, jLabel1, 8); agregar(formulario, txtUsername, 18);
        agregar(formulario, jLabel2, 8); agregar(formulario, txtPassword, 12);
        agregar(formulario, mostrarPassword, 24); agregar(formulario, btnIngresar, 18);
        agregar(formulario, Tema.texto("Acceso para el personal del cine.", Tema.ETIQUETA, Tema.SECUNDARIO), 0);
        GridBagConstraints centro = new GridBagConstraints(); centro.fill = GridBagConstraints.HORIZONTAL; centro.weightx = 1;
        acceso.add(formulario, centro); tarjeta.add(acceso,BorderLayout.CENTER);
        pagina.add(tarjeta, centro);
        JScrollPane scroll = new JScrollPane();
        views.estilos.Adaptable.envolver(scroll, pagina); setContentPane(scroll);
        views.estilos.Adaptable.limitarVentana(this, 920, 640);
    }

    private static void agregar(JPanel panel, JComponent control, int espacio) {
        control.setAlignmentX(Component.LEFT_ALIGNMENT);
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, control.getPreferredSize().height));
        panel.add(control); if (espacio > 0) panel.add(Box.createVerticalStrut(espacio));
    }

    /** Motivo de una pantalla y sus butacas, dibujado sin archivos de imagen. */
    private static final class Portada extends JPanel {
        Portada() { setBackground(Tema.TEXTO); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int x = 40, y = getHeight() - 215, ancho = getWidth() - 80;
            g.setPaint(new GradientPaint(x, y, Tema.PRIMARIO, x + ancho, y + 90, new Color(109, 30, 52)));
            g.fillRoundRect(x, y, ancho, 76, 12, 12);
            g.setColor(new Color(255, 255, 255, 180));
            g.fillRoundRect(x + 18, y + 20, Math.max(30, ancho - 36), 3, 3, 3);
            g.setColor(new Color(255, 255, 255, 60));
            for (int fila = 0; fila < 2; fila++) {
                for (int asiento = 0; asiento < 6; asiento++) {
                    int anchoAsiento = Math.max(12, (ancho - 40) / 6);
                    int ax = x + asiento * (anchoAsiento + 7);
                    int ay = y + 100 + fila * 28;
                    g.fillRoundRect(ax, ay, anchoAsiento, 17, 6, 6);
                }
            }
            g.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnIngresar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Usuario:");

        jLabel2.setText("Contraseña:");

        btnIngresar.setText("Ingresar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtUsername)
                    .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(btnIngresar))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addComponent(btnIngresar)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public String getUsername() {
        return txtUsername.getText();
    }

    public char[] getPassword() {
        return txtPassword.getPassword();
    }

    public void addIngresarListener(java.awt.event.ActionListener listenForIngresarButton) {
        btnIngresar.addActionListener(listenForIngresarButton);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIngresar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
