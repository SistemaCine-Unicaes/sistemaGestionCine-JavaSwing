package views;

import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import javax.swing.*;
import models.Funcion;
import models.Pelicula;
import views.estilos.Pagina;
import views.estilos.Tema;

/** Selección de fecha y horario de una sola película, agrupados por sala. */
public class HorariosPeliculaView extends JPanel {
    private static final Locale ESPANOL = Locale.forLanguageTag("es-SV");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' uuuu", ESPANOL);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final Pelicula pelicula;
    private final JButton volver = Tema.botonSecundario("← Volver a cartelera");
    private final JButton actualizar = Tema.botonSecundario("Actualizar horarios");
    private final JLabel estado = Tema.mensaje("Cargando funciones…", Tema.SECUNDARIO);
    private final JLabel fechaTitulo = Tema.texto("Horarios disponibles", Tema.SUBTITULO, Tema.TEXTO);
    private final JPanel fechas = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
    private final JPanel salas = columna();
    private List<Funcion> funciones = List.of();
    private LocalDate fechaSeleccionada;
    private IntConsumer comprarListener = id -> {};

    public HorariosPeliculaView(Pelicula pelicula) {
        this.pelicula = pelicula;
        setLayout(new BorderLayout());
        Pagina pagina = new Pagina();
        JPanel contenido = columna();
        JPanel navegacion = new JPanel(new views.estilos.RejillaAdaptable(2, 200, 12));
        navegacion.setOpaque(false); navegacion.add(volver, BorderLayout.WEST); navegacion.add(actualizar, BorderLayout.EAST);
        pagina.add(navegacion, BorderLayout.NORTH);

        JPanel ficha = Tema.tarjeta();
        ficha.add(new PosterView(pelicula.getImagenUrl(), 110, 155), BorderLayout.WEST);
        JPanel datos = new JPanel(new BorderLayout(0, 12)); datos.setOpaque(false);
        JPanel titulo = columna();
        titulo.add(Tema.texto("CARTELERA  /  FUNCIONES", Tema.ETIQUETA, Tema.PRIMARIO));
        titulo.add(Box.createVerticalStrut(8));
        JTextArea nombre = texto(pelicula.getNombre(), Tema.TITULO, Tema.TEXTO);
        nombre.getAccessibleContext().setAccessibleName("Película seleccionada");
        titulo.add(nombre);
        titulo.add(Box.createVerticalStrut(8));
        titulo.add(Tema.texto(pelicula.getDuracion() + " min · " + valor(pelicula.getGenero()), Tema.CUERPO, Tema.SECUNDARIO));
        datos.add(titulo, BorderLayout.NORTH);
        JTextArea sinopsis = texto(valor(pelicula.getSinopsis()), Tema.CUERPO, Tema.SECUNDARIO);
        sinopsis.setRows(3);
        JScrollPane scrollSinopsis = new JScrollPane(sinopsis); scrollSinopsis.setBorder(BorderFactory.createEmptyBorder());
        scrollSinopsis.getViewport().setBackground(Tema.SUPERFICIE);
        datos.add(scrollSinopsis, BorderLayout.CENTER);
        ficha.add(datos, BorderLayout.CENTER);
        bloque(contenido, ficha);

        JPanel selector = Tema.tarjeta();
        selector.add(Tema.texto("Selecciona la fecha", Tema.SUBTITULO, Tema.TEXTO), BorderLayout.NORTH);
        fechas.setOpaque(false);
        JScrollPane scrollFechas = new JScrollPane(fechas);
        scrollFechas.setBorder(BorderFactory.createEmptyBorder());
        scrollFechas.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollFechas.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollFechas.setPreferredSize(new Dimension(400, 76));
        scrollFechas.getViewport().setBackground(Tema.SUPERFICIE);
        selector.add(scrollFechas, BorderLayout.CENTER);
        bloque(contenido, selector);
        bloque(contenido, estado);
        bloque(contenido, fechaTitulo);
        salas.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenido.add(salas);
        JPanel superior = new JPanel(new BorderLayout()); superior.setOpaque(false);
        superior.add(contenido, BorderLayout.NORTH);
        pagina.add(superior, BorderLayout.CENTER);
        add(pagina.conScroll(), BorderLayout.CENTER);
    }

    private static JPanel columna() {
        JPanel panel = new JPanel(); panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS)); return panel;
    }
    private static void bloque(JPanel padre, JComponent hijo) {
        hijo.setAlignmentX(Component.LEFT_ALIGNMENT); padre.add(hijo); padre.add(Box.createVerticalStrut(20));
    }
    private static JTextArea texto(String valor, Font fuente, Color color) {
        JTextArea texto = new JTextArea(valor); texto.setEditable(false); texto.setOpaque(false);
        texto.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
        texto.setBorder(BorderFactory.createEmptyBorder()); texto.setAlignmentX(Component.LEFT_ALIGNMENT);
        texto.setLineWrap(true); texto.setWrapStyleWord(true); texto.setFont(fuente); texto.setForeground(color);
        return texto;
    }
    private static String valor(String texto) { return texto == null || texto.isBlank() ? "Sin información" : texto; }

    public void mostrarFunciones(List<Funcion> disponibles) {
        funciones = disponibles.stream().filter(f -> f.getIdPelicula() == pelicula.getIdPelicula())
                .sorted(Comparator.comparing(Funcion::getFechaProyeccion).thenComparing(Funcion::getHoraInicio)
                        .thenComparingInt(Funcion::getIdSala).thenComparingInt(Funcion::getIdFuncion)).toList();
        List<LocalDate> dias = funciones.stream().map(f -> f.getFechaProyeccion().toLocalDate()).distinct().toList();
        if (!dias.contains(fechaSeleccionada)) fechaSeleccionada = dias.isEmpty() ? null : dias.getFirst();
        fechas.removeAll();
        ButtonGroup grupo = new ButtonGroup();
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador")));
        for (LocalDate dia : dias) {
            String etiqueta = (dia.equals(hoy) ? "Hoy" : dia.format(DateTimeFormatter.ofPattern("EEE", ESPANOL)))
                    + " · " + dia.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));
            JButton boton = Tema.botonSeleccionable(etiqueta);
            boton.setPreferredSize(new Dimension(165, 56));
            boton.getAccessibleContext().setAccessibleName("Funciones del " + dia.format(FECHA));
            grupo.add(boton);
            boton.setSelected(dia.equals(fechaSeleccionada));
            boton.addActionListener(e -> { boton.setSelected(true); fechaSeleccionada = dia; mostrarSalas(); });
            fechas.add(boton);
        }
        mostrarSalas(); fechas.revalidate(); fechas.repaint();
    }

    private void mostrarSalas() {
        salas.removeAll();
        fechaTitulo.setText(fechaSeleccionada == null ? "Horarios disponibles" : fechaSeleccionada.format(FECHA));
        estado.setForeground(Tema.SECUNDARIO);
        estado.setText(funciones.isEmpty() ? "Esta película todavía no tiene funciones próximas disponibles."
                : "Selecciona un horario para continuar con la compra de boletos.");
        var grupos = funciones.stream().filter(f -> f.getFechaProyeccion().toLocalDate().equals(fechaSeleccionada))
                .collect(Collectors.groupingBy(Funcion::getIdSala, TreeMap::new, Collectors.toList()));
        grupos.forEach((idSala, lista) -> {
            JPanel tarjeta = Tema.tarjeta();
            tarjeta.add(Tema.texto("Sala " + idSala, Tema.SUBTITULO, Tema.TEXTO), BorderLayout.NORTH);
            JPanel horarios = new JPanel(new views.estilos.RejillaAdaptable(3, 125, 12)); horarios.setOpaque(false);
            for (Funcion f : lista) {
                JPanel opcion = new JPanel(new BorderLayout(0, 6)); opcion.setOpaque(false);
                JButton hora = Tema.botonSecundario(f.getHoraInicio().toLocalTime().format(HORA));
                hora.setPreferredSize(new Dimension(110, 52));
                hora.putClientProperty("idFuncion", f.getIdFuncion());
                hora.getAccessibleContext().setAccessibleName("Sala " + idSala + ", " + fechaSeleccionada.format(FECHA) + ", " + hora.getText());
                hora.addActionListener(e -> comprarListener.accept(f.getIdFuncion()));
                opcion.add(hora, BorderLayout.NORTH);
                String fin = "Finaliza " + f.getHoraFin().toLocalTime().format(HORA)
                        + (f.getHoraFin().before(f.getHoraInicio()) ? " (+1 día)" : "");
                opcion.add(Tema.texto(fin, Tema.ETIQUETA, Tema.SECUNDARIO), BorderLayout.SOUTH);
                horarios.add(opcion);
            }
            tarjeta.add(horarios, BorderLayout.CENTER); bloque(salas, tarjeta);
        });
        salas.revalidate(); salas.repaint();
    }

    public void setCargando(boolean cargando) {
        actualizar.setEnabled(!cargando);
        if (cargando) {
            funciones = List.of(); fechas.removeAll(); salas.removeAll();
            estado.setText("Cargando funciones…"); estado.setForeground(Tema.SECUNDARIO);
            revalidate(); repaint();
        }
    }
    public void mostrarErrorCarga() {
        estado.setForeground(Tema.ERROR);
        estado.setText("No se pudieron cargar las funciones. Pulsa «Actualizar horarios» para reintentar.");
    }
    public void addVolverListener(ActionListener listener) { volver.addActionListener(listener); }
    public void addActualizarListener(ActionListener listener) { actualizar.addActionListener(listener); }
    public void setComprarListener(IntConsumer listener) { comprarListener = listener; }
}
