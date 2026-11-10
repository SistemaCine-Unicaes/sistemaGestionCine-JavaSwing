package views;

import dao.CajaDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import models.Caja;
import models.Usuario;
import views.estilos.Tema;

/**
 * Diálogo modal para asignar y abrir una caja preestablecida al cajero al iniciar sesión.
 */
public class AsignarCajaDialog extends JDialog {
    private final JComboBox<Caja> cbCajas;
    private final JButton btnConfirmar;
    private final JButton btnCancelar;
    private final JLabel lblMensaje;
    private boolean confirmado = false;
    private final Usuario usuario;
    private final CajaDAO cajaDAO;

    public AsignarCajaDialog(Frame parent, Usuario usuario) {
        super(parent, "Asignación de Caja · " + usuario.getNombre(), true);
        this.usuario = usuario;
        this.cajaDAO = new CajaDAO();

        setLayout(new BorderLayout(0, 20));
        getContentPane().setBackground(Tema.FONDO);

        JPanel pnlContenido = new JPanel();
        pnlContenido.setLayout(new BoxLayout(pnlContenido, BoxLayout.PAGE_AXIS));
        pnlContenido.setOpaque(false);
        pnlContenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // 1. Cabecera
        JLabel lblTitulo = new JLabel("Apertura de Turno en Caja");
        lblTitulo.setFont(Tema.TITULO);
        lblTitulo.setForeground(Tema.PRIMARIO);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlContenido.add(lblTitulo);
        pnlContenido.add(Box.createVerticalStrut(6));

        JLabel lblSub = new JLabel("Hola, " + usuario.getNombre() + ". Selecciona la caja asignada para tus ventas:");
        lblSub.setFont(Tema.CUERPO);
        lblSub.setForeground(Tema.SECUNDARIO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlContenido.add(lblSub);
        pnlContenido.add(Box.createVerticalStrut(20));

        // 2. Tarjeta con selector
        JPanel pnlTarjeta = Tema.tarjeta();
        pnlTarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel pnlCampo = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlCampo.setOpaque(false);
        JLabel lblCampo = new JLabel("Caja de atención disponible:");
        lblCampo.setFont(Tema.ETIQUETA);
        lblCampo.setForeground(Tema.TEXTO);
        pnlCampo.add(lblCampo);

        cbCajas = new JComboBox<>();
        cbCajas.setFont(Tema.CUERPO);
        cbCajas.setPreferredSize(new Dimension(320, 38));
        pnlCampo.add(cbCajas);
        pnlTarjeta.add(pnlCampo, BorderLayout.CENTER);

        pnlContenido.add(pnlTarjeta);
        pnlContenido.add(Box.createVerticalStrut(12));

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(Tema.ETIQUETA);
        lblMensaje.setForeground(Tema.ERROR);
        lblMensaje.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlContenido.add(lblMensaje);

        add(pnlContenido, BorderLayout.CENTER);

        // 3. Botones de acción
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        pnlBotones.setBackground(Tema.SUPERFICIE);
        pnlBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE));

        btnCancelar = Tema.botonSecundario("Cancelar");
        btnConfirmar = Tema.botonPrimario("Abrir Caja e Ingresar");

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnConfirmar);
        add(pnlBotones, BorderLayout.SOUTH);

        // Cargar cajas
        cargarCajas();

        btnConfirmar.addActionListener(e -> confirmarApertura());
        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void cargarCajas() {
        cbCajas.removeAllItems();
        try {
            List<Caja> disponibles = cajaDAO.listarDisponibles(usuario.getIdUsuario());
            for (Caja c : disponibles) {
                cbCajas.addItem(c);
            }
            if (disponibles.isEmpty()) {
                lblMensaje.setText("No hay cajas disponibles en este momento.");
                btnConfirmar.setEnabled(false);
            } else {
                btnConfirmar.setEnabled(true);
            }
        } catch (Exception e) {
            lblMensaje.setText("Error al consultar cajas: " + e.getMessage());
            btnConfirmar.setEnabled(false);
        }
    }

    private void confirmarApertura() {
        Caja seleccionada = (Caja) cbCajas.getSelectedItem();
        if (seleccionada == null) {
            lblMensaje.setText("Debes seleccionar una caja.");
            return;
        }

        try {
            cajaDAO.abrirCaja(seleccionada.getIdCaja(), usuario.getIdUsuario());
            seleccionada.setEstado("OCUPADA");
            seleccionada.setIdUsuarioActual(usuario.getIdUsuario());
            config.Sesion.setCajaActual(seleccionada);
            confirmado = true;
            dispose();
        } catch (Exception ex) {
            lblMensaje.setText("No se pudo abrir la caja: " + ex.getMessage());
        }
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}
