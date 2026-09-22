package views;

import java.awt.event.ActionListener;
import javax.swing.*;

/** Conserva la edición del precio general dentro del MDI. */
public class ConfiguracionView extends JPanel {
    private final JTextField txtPrecio = new JTextField(12);
    private final JButton btnGuardar = new JButton("Guardar precio");
    private final JButton btnRecargar = new JButton("Recargar");

    public ConfiguracionView() {
        setLayout(new java.awt.BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel formulario = new JPanel();
        GroupLayout layout = new GroupLayout(formulario);
        formulario.setLayout(layout);
        JLabel etiqueta = new JLabel("Precio general por boleto ($):");
        JLabel ayuda = new JLabel("El precio se aplica a las nuevas ventas de todas las cajas.");
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup().addComponent(etiqueta).addGap(18)
                        .addComponent(txtPrecio, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(ayuda)
                .addGroup(layout.createSequentialGroup().addComponent(btnGuardar).addGap(12).addComponent(btnRecargar)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(etiqueta).addComponent(txtPrecio))
                .addGap(12).addComponent(ayuda).addGap(20)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(btnGuardar).addComponent(btnRecargar)));
        JPanel superior = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 0));
        superior.add(formulario);
        add(superior, java.awt.BorderLayout.NORTH);
        habilitarEdicion(false);
    }

    public String getPrecio() { return txtPrecio.getText().trim(); }
    public void setPrecio(String precio) { txtPrecio.setText(precio); }
    public void habilitarEdicion(boolean habilitado) {
        txtPrecio.setEnabled(habilitado); btnGuardar.setEnabled(habilitado);
    }
    public void addGuardarListener(ActionListener listener) { btnGuardar.addActionListener(listener); }
    public void addRecargarListener(ActionListener listener) { btnRecargar.addActionListener(listener); }
}
