package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import views.estilos.Tema;

/** Configuración de precios por categoría (Adulto, Niño y Adulto Mayor) dentro del MDI. */
public class ConfiguracionView extends JPanel {
    private final JTextField txtPrecioAdulto = Tema.campo("", 12);
    private final JTextField txtPrecioNino = Tema.campo("", 12);
    private final JTextField txtPrecioAdultoMayor = Tema.campo("", 12);
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
                "Administra los precios de los boletos por categoría.",
                Tema.CUERPO,
                Tema.SECUNDARIO);

        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(descripcion, BorderLayout.CENTER);

        // Tarjeta compartida: fondo blanco, borde y margen.
        JPanel tarjeta = Tema.tarjeta();

        JLabel tituloTarjeta = Tema.texto(
                "Tarifas de boletos",
                Tema.SUBTITULO,
                Tema.TEXTO);

        tarjeta.add(tituloTarjeta, BorderLayout.NORTH);

        // Formulario dentro de la tarjeta.
        JPanel formulario = new JPanel();
        formulario.setOpaque(false);

        JLabel lblAdulto = Tema.texto("Precio Adulto / General ($)", Tema.ETIQUETA, Tema.TEXTO);
        lblAdulto.setLabelFor(txtPrecioAdulto);

        JLabel lblNino = Tema.texto("Precio Niño ($)", Tema.ETIQUETA, Tema.TEXTO);
        lblNino.setLabelFor(txtPrecioNino);

        JLabel lblAdultoMayor = Tema.texto("Precio Adulto Mayor ($)", Tema.ETIQUETA, Tema.TEXTO);
        lblAdultoMayor.setLabelFor(txtPrecioAdultoMayor);

        // Texto de ayuda.
        JTextArea ayuda = new JTextArea(
                "Los precios configurados se aplicarán automáticamente a las nuevas ventas en todas las cajas.");
        ayuda.setFont(Tema.CUERPO);
        ayuda.setForeground(Tema.SECUNDARIO);
        ayuda.setOpaque(false);
        ayuda.setEditable(false);
        ayuda.setFocusable(false);
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setRows(2);
        ayuda.setBorder(BorderFactory.createEmptyBorder());

        // Botones de acción.
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        acciones.setOpaque(false);
        acciones.add(btnGuardar);
        acciones.add(Box.createHorizontalStrut(Tema.SEPARACION));
        acciones.add(btnRecargar);

        GroupLayout layout = new GroupLayout(formulario);
        formulario.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblAdulto)
                        .addComponent(txtPrecioAdulto, 240, 240, 240)
                        .addComponent(lblNino)
                        .addComponent(txtPrecioNino, 240, 240, 240)
                        .addComponent(lblAdultoMayor)
                        .addComponent(txtPrecioAdultoMayor, 240, 240, 240)
                        .addComponent(ayuda, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(acciones, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(lblAdulto)
                        .addGap(Tema.ESPACIO)
                        .addComponent(txtPrecioAdulto, Tema.ALTO_CONTROL, Tema.ALTO_CONTROL, Tema.ALTO_CONTROL)
                        .addGap(Tema.SEPARACION)
                        .addComponent(lblNino)
                        .addGap(Tema.ESPACIO)
                        .addComponent(txtPrecioNino, Tema.ALTO_CONTROL, Tema.ALTO_CONTROL, Tema.ALTO_CONTROL)
                        .addGap(Tema.SEPARACION)
                        .addComponent(lblAdultoMayor)
                        .addGap(Tema.ESPACIO)
                        .addComponent(txtPrecioAdultoMayor, Tema.ALTO_CONTROL, Tema.ALTO_CONTROL, Tema.ALTO_CONTROL)
                        .addGap(Tema.SEPARACION)
                        .addComponent(ayuda, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(Tema.MARGEN)
                        .addComponent(acciones, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        tarjeta.add(formulario, BorderLayout.CENTER);

        // Agrupación general
        JPanel contenido = new JPanel(new BorderLayout(0, 32));
        contenido.setOpaque(false);
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(tarjeta, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder());
        views.estilos.Pagina pagina = new views.estilos.Pagina();
        pagina.add(contenido, BorderLayout.NORTH); add(pagina.conScroll(), BorderLayout.CENTER);

        habilitarEdicion(false);
    }

    // Getters y Setters para las tres tarifas
    public String getPrecioAdulto() { return txtPrecioAdulto.getText().trim(); }
    public void setPrecioAdulto(String precio) { txtPrecioAdulto.setText(precio); }

    public String getPrecioNino() { return txtPrecioNino.getText().trim(); }
    public void setPrecioNino(String precio) { txtPrecioNino.setText(precio); }

    public String getPrecioAdultoMayor() { return txtPrecioAdultoMayor.getText().trim(); }
    public void setPrecioAdultoMayor(String precio) { txtPrecioAdultoMayor.setText(precio); }

    // Compatibilidad temporal
    public String getPrecio() { return getPrecioAdulto(); }
    public void setPrecio(String precio) { setPrecioAdulto(precio); }

    public void habilitarEdicion(boolean habilitado) {
        txtPrecioAdulto.setEnabled(habilitado);
        txtPrecioNino.setEnabled(habilitado);
        txtPrecioAdultoMayor.setEnabled(habilitado);
        btnGuardar.setEnabled(habilitado);
    }

    public void addGuardarListener(ActionListener listener) { btnGuardar.addActionListener(listener); }
    public void addRecargarListener(ActionListener listener) { btnRecargar.addActionListener(listener); }
}