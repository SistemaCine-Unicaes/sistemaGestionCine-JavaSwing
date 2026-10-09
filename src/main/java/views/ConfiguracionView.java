package views;

import java.awt.event.ActionListener;
import javax.swing.*;
import views.estilos.Tema;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Conserva la edición del precio general dentro del MDI. */
public class ConfiguracionView extends JPanel {
    private final JTextField txtPrecio = Tema.campo("", 12);
    private final JButton btnGuardar = Tema.botonPrimario("Guardar");
    private final JButton btnRecargar = Tema.botonSecundario("Recargar");

    public ConfiguracionView() {
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(
                Tema.MARGEN, Tema.MARGEN,
                Tema.MARGEN, Tema.MARGEN));

        // Encabezado de la pantalla.
        JPanel encabezado = new JPanel(new BorderLayout(0, Tema.ESPACIO));
        encabezado.setOpaque(false);

        JLabel titulo = Tema.texto(
                "Configuración",
                Tema.TITULO,
                Tema.TEXTO);

        JLabel descripcion = Tema.texto(
                "Administra el precio general de los boletos.",
                Tema.CUERPO,
                Tema.SECUNDARIO);

        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(descripcion, BorderLayout.CENTER);

        // Tarjeta compartida: fondo blanco, borde y margen de 24.
        JPanel tarjeta = Tema.tarjeta();

        JLabel tituloTarjeta = Tema.texto(
                "Precio de los boletos",
                Tema.SUBTITULO,
                Tema.TEXTO);

        tarjeta.add(tituloTarjeta, BorderLayout.NORTH);

        // Formulario dentro de la tarjeta.
        JPanel formulario = new JPanel();
        formulario.setOpaque(false);

        JLabel etiqueta = Tema.texto(
                "Precio general por boleto ($)",
                Tema.ETIQUETA,
                Tema.TEXTO);

        etiqueta.setLabelFor(txtPrecio);

        // Texto de ayuda que puede ocupar varias líneas.
        JTextArea ayuda = new JTextArea(
                "El precio se aplica a las nuevas ventas de todas las cajas.");

        ayuda.setFont(Tema.CUERPO);
        ayuda.setForeground(Tema.SECUNDARIO);
        ayuda.setOpaque(false);
        ayuda.setEditable(false);
        ayuda.setFocusable(false);
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setRows(2);
        ayuda.setBorder(BorderFactory.createEmptyBorder());

        // Botones existentes: conservan sus eventos.
        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.LEADING, 0, 0));

        acciones.setOpaque(false);
        acciones.add(btnGuardar);
        acciones.add(Box.createHorizontalStrut(Tema.SEPARACION));
        acciones.add(btnRecargar);

        GroupLayout layout = new GroupLayout(formulario);
        formulario.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(etiqueta)
                        .addComponent(txtPrecio, 240, 240, 240)
                        .addComponent(
                                ayuda, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(
                                acciones,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(etiqueta)
                        .addGap(Tema.ESPACIO)
                        .addComponent(
                                txtPrecio,
                                Tema.ALTO_CONTROL,
                                Tema.ALTO_CONTROL,
                                Tema.ALTO_CONTROL)
                        .addGap(Tema.SEPARACION)
                        .addComponent(
                                ayuda,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(Tema.MARGEN)
                        .addComponent(
                                acciones,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
        );

        tarjeta.add(formulario, BorderLayout.CENTER);

        // Agrupa el encabezado y la tarjeta en la parte superior.
        JPanel contenido = new JPanel(new BorderLayout(0, 32));
        contenido.setOpaque(false);
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(tarjeta, BorderLayout.CENTER);

        add(contenido, BorderLayout.NORTH);

        // Se conserva el estado inicial gestionado por el controlador.
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
