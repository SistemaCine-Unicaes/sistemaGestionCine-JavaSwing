package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import models.Pelicula;
import models.Sala;
import views.estilos.Tema;

/** Formulario de programación alojado en el panel central del MDI. */
public class FuncionesView extends JPanel {
    private final JComboBox<String> cbPelicula = new JComboBox<>();
    private final JComboBox<String> cbSala = new JComboBox<>();
    private final JTextField txtFecha = Tema.campo("", 18);
    private final JTextField txtHora = Tema.campo("18:00", 18);
    private final JButton btnProgramar = Tema.botonPrimario("Programar función");
    private final JButton btnRecargar = Tema.botonSecundario("Actualizar opciones");

    public FuncionesView() {
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(
                Tema.MARGEN, Tema.MARGEN,
                Tema.MARGEN, Tema.MARGEN));

        // Encabezado de la pantalla.
        JPanel encabezado = new JPanel(
                new BorderLayout(0, Tema.ESPACIO));

        encabezado.setOpaque(false);

        JLabel titulo = Tema.texto(
                "Programación de funciones",
                Tema.TITULO,
                Tema.TEXTO);

        JLabel descripcion = Tema.texto(
                "Define la película, la sala y el horario.",
                Tema.CUERPO,
                Tema.SECUNDARIO);

        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(descripcion, BorderLayout.CENTER);

        // Tarjeta con el estilo compartido.
        JPanel tarjeta = Tema.tarjeta();

        tarjeta.add(
                Tema.texto(
                        "Datos de la función",
                        Tema.SUBTITULO,
                        Tema.TEXTO),
                BorderLayout.NORTH);

        // Dos columnas: película/sala y fecha/hora.
        JPanel campos = new JPanel(
                new GridLayout(
                        2, 2,
                        Tema.SEPARACION,
                        Tema.SEPARACION));

        campos.setOpaque(false);

        campos.add(crearGrupo("Película", cbPelicula));
        campos.add(crearGrupo("Sala", cbSala));
        campos.add(crearGrupo("Fecha (dd/mm/aaaa)", txtFecha));
        campos.add(crearGrupo("Hora (HH:mm)", txtHora));

        // Acciones existentes.
        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.LEADING, 0, 0));

        acciones.setOpaque(false);
        acciones.add(btnProgramar);
        acciones.add(Box.createHorizontalStrut(Tema.SEPARACION));
        acciones.add(btnRecargar);

        // Separación entre los campos y las acciones.
        JPanel formulario = new JPanel(
                new BorderLayout(0, Tema.MARGEN));

        formulario.setOpaque(false);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(acciones, BorderLayout.SOUTH);

        tarjeta.add(formulario, BorderLayout.CENTER);

        // Misma separación de bloques que en Configuración.
        JPanel contenido = new JPanel(new BorderLayout(0, 32));
        contenido.setOpaque(false);
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(tarjeta, BorderLayout.CENTER);

        add(contenido, BorderLayout.NORTH);

        // El controlador habilita el botón cuando hay opciones disponibles.
        habilitarProgramar(false);
    }

    private JPanel crearGrupo(String texto, JComponent campo) {
        JPanel grupo = new JPanel(new BorderLayout(0, Tema.ESPACIO));

        grupo.setOpaque(false);

        JLabel etiqueta = Tema.texto(
                texto,
                Tema.ETIQUETA,
                Tema.TEXTO);

        etiqueta.setLabelFor(campo);

        // Mantiene la altura preferida del control.
        // Los JComboBox conservan su altura y apariencia predeterminadas.
        JPanel contenedorCampo = new JPanel(new BorderLayout());
        contenedorCampo.setOpaque(false);
        contenedorCampo.add(campo, BorderLayout.NORTH);

        grupo.add(etiqueta, BorderLayout.NORTH);
        grupo.add(contenedorCampo, BorderLayout.CENTER);

        return grupo;
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
