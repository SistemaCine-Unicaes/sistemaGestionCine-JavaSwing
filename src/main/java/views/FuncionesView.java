package views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import models.Pelicula;
import models.Sala;

/** Formulario de programación alojado en el panel central del MDI. */
public class FuncionesView extends JPanel {
    private final JComboBox<String> cbPelicula = new JComboBox<>();
    private final JComboBox<String> cbSala = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(18);
    private final JTextField txtHora = new JTextField("18:00", 18);
    private final JButton btnProgramar = new JButton("Programar función");
    private final JButton btnRecargar = new JButton("Actualizar opciones");

    public FuncionesView() {
        setLayout(new java.awt.BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JPanel formulario = new JPanel(new GridBagLayout());
        agregarFila(formulario, 0, "Película:", cbPelicula);
        agregarFila(formulario, 1, "Sala:", cbSala);
        agregarFila(formulario, 2, "Fecha (dd/mm/aaaa):", txtFecha);
        agregarFila(formulario, 3, "Hora (HH:mm):", txtHora);
        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 0));
        acciones.add(btnProgramar);
        acciones.add(Box.createHorizontalStrut(12));
        acciones.add(btnRecargar);
        GridBagConstraints celda = new GridBagConstraints();
        celda.gridx = 0; celda.gridy = 4; celda.gridwidth = 2;
        celda.anchor = GridBagConstraints.WEST; celda.insets = new Insets(20, 0, 0, 0);
        formulario.add(acciones, celda);
        JPanel superior = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 0));
        superior.add(formulario);
        add(superior, java.awt.BorderLayout.NORTH);
        habilitarProgramar(false);
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints celda = new GridBagConstraints();
        celda.gridx = 0; celda.gridy = fila; celda.anchor = GridBagConstraints.WEST;
        celda.insets = new Insets(0, 0, 12, 18);
        panel.add(new JLabel(etiqueta), celda);
        celda.gridx = 1; celda.fill = GridBagConstraints.HORIZONTAL; celda.weightx = 1;
        celda.insets = new Insets(0, 0, 12, 0);
        panel.add(campo, celda);
    }

    public void mostrarOpciones(List<Pelicula> peliculas, List<Sala> salas) {
        cbPelicula.removeAllItems(); cbSala.removeAllItems();
        for (Pelicula p : peliculas) cbPelicula.addItem(p.getNombre() + " (#" + p.getIdPelicula() + ")");
        for (Sala s : salas) cbSala.addItem("Sala " + s.getIdSala());
    }

    public int getPeliculaSeleccionada() { return cbPelicula.getSelectedIndex(); }
    public int getSalaSeleccionada() { return cbSala.getSelectedIndex(); }
    public String getFecha() { return txtFecha.getText().trim(); }
    public void setFecha(String fecha) { txtFecha.setText(fecha); }
    public String getHora() { return txtHora.getText().trim(); }
    public void habilitarProgramar(boolean habilitado) { btnProgramar.setEnabled(habilitado); }
    public void addProgramarListener(ActionListener listener) { btnProgramar.addActionListener(listener); }
    public void addRecargarListener(ActionListener listener) { btnRecargar.addActionListener(listener); }
}
