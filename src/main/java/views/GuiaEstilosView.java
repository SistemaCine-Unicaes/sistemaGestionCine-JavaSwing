package views;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import views.estilos.Tema;

/** Catálogo visual independiente. Todos sus datos e interacciones son de demostración. */
public class GuiaEstilosView extends JPanel {
    private final CardLayout paginas = new CardLayout();
    private final JPanel contenido = new JPanel(paginas);
    private final JLabel estado = Tema.texto("GUÍA DEL EQUIPO  /  01", Tema.ETIQUETA, Tema.SECUNDARIO);

    public GuiaEstilosView() {
        super(new BorderLayout());
        setBackground(Tema.FONDO);
        add(cabecera(), BorderLayout.NORTH);
        contenido.add(desplazable(componentes()), "Componentes");
        contenido.add(desplazable(cartelera()), "Pantalla ejemplo");
        contenido.add(desplazable(equipo()), "Trabajo en equipo");
        add(contenido, BorderLayout.CENTER);
        JLabel pie = Tema.texto("CINE / Guía visual v1.0     ·     Datos de ejemplo     ·     Sin conexión a la base de datos",
                Tema.ETIQUETA, Tema.SECUNDARIO);
        pie.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        add(pie, BorderLayout.SOUTH);
    }

    private JPanel cabecera() {
        JPanel cabecera = new JPanel(new BorderLayout(24, 16));
        cabecera.setBackground(Tema.TEXTO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 32));
        JPanel titulos = columna();
        titulos.add(Tema.texto("C I N E   /   DISEÑO COMPARTIDO", Tema.ETIQUETA, new Color(255, 165, 180)));
        titulos.add(Box.createVerticalStrut(8));
        titulos.add(Tema.texto("Un mismo estilo. Todas las pantallas.", Tema.TITULO, Color.WHITE));
        titulos.add(Box.createVerticalStrut(8));
        titulos.add(Tema.texto("La referencia visual para construir el sistema entre los cinco.", Tema.CUERPO, new Color(210, 218, 230)));
        cabecera.add(titulos, BorderLayout.CENTER);
        JPanel navegacion = fila();
        ButtonGroup grupo = new ButtonGroup();
        for (String nombre : new String[]{"Componentes", "Pantalla ejemplo", "Trabajo en equipo"}) {
            JToggleButton boton = new JToggleButton(nombre);
            boton.setFont(Tema.CUERPO.deriveFont(Font.BOLD));
            boton.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            boton.setForeground(Color.WHITE);
            boton.setBackground(Tema.SECUNDARIO);
            boton.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.addItemListener(e -> boton.setBackground(boton.isSelected() ? Tema.PRIMARIO : Tema.SECUNDARIO));
            boton.addActionListener(e -> paginas.show(contenido, nombre));
            grupo.add(boton);
            navegacion.add(boton);
            if (nombre.equals("Componentes")) boton.setSelected(true);
        }
        cabecera.add(navegacion, BorderLayout.SOUTH);
        return cabecera;
    }

    private JPanel componentes() {
        JPanel pagina = pagina();
        pagina.add(estado);
        pagina.add(Box.createVerticalStrut(8));
        pagina.add(Tema.texto("Los elementos de nuestra interfaz", Tema.TITULO, Tema.TEXTO));
        pagina.add(Box.createVerticalStrut(8));
        pagina.add(parrafo("Usen estas medidas y componentes en login, administración y venta de boletos."));
        JPanel rejilla = new JPanel(new GridLayout(0, 2, 16, 16));
        rejilla.setOpaque(false);
        rejilla.add(paleta());
        rejilla.add(tipografia());
        rejilla.add(botones());
        rejilla.add(formulario());
        rejilla.add(mensajes());
        rejilla.add(espaciado());
        agregar(pagina, rejilla);
        agregar(pagina, tablaEjemplo());
        return pagina;
    }

    private JPanel paleta() {
        JPanel cuerpo = columna();
        JPanel colores = new JPanel(new GridLayout(2, 3, 12, 16));
        colores.setOpaque(false);
        Color[] valores = {Tema.PRIMARIO, Tema.TEXTO, Tema.FONDO, Tema.EXITO, Tema.AVISO, Tema.ERROR};
        String[] nombres = {"Acción principal", "Texto / cabecera", "Fondo", "Éxito", "Advertencia", "Error"};
        for (int i = 0; i < valores.length; i++) {
            JPanel muestra = columna();
            JPanel bloque = new JPanel();
            bloque.setBackground(valores[i]);
            bloque.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
            bloque.setPreferredSize(new Dimension(80, 32));
            bloque.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            muestra.add(bloque);
            muestra.add(Box.createVerticalStrut(8));
            muestra.add(Tema.texto(nombres[i], Tema.ETIQUETA, Tema.TEXTO));
            muestra.add(Tema.texto(String.format("#%06X", valores[i].getRGB() & 0xFFFFFF), Tema.CUERPO, Tema.SECUNDARIO));
            colores.add(muestra);
        }
        cuerpo.add(colores);
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(parrafo("Superficie #FFFFFF · Borde #DCE1E8 · Texto secundario #586579"));
        return seccion("01 / Paleta de color", cuerpo);
    }

    private JPanel tipografia() {
        JPanel cuerpo = columna();
        cuerpo.add(Tema.texto("Una noche de cine", Tema.TITULO, Tema.TEXTO));
        cuerpo.add(parrafo("Título · 28 px · Negrita"));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(Tema.texto("Películas en cartelera", Tema.SUBTITULO, Tema.TEXTO));
        cuerpo.add(parrafo("Sección · 18 px · Negrita"));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(Tema.texto("Elige tu película y tu horario.", Tema.CUERPO, Tema.TEXTO));
        cuerpo.add(parrafo("Texto · 14 px / Etiqueta · 12 px en negrita"));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(parrafo("Familia SansSerif: disponible en Java y adaptable al sistema operativo."));
        return seccion("02 / Tipografía", cuerpo);
    }

    private JPanel botones() {
        JPanel cuerpo = columna();
        JPanel acciones = fila();
        JButton guardar = Tema.botonPrimario("Guardar");
        JButton cancelar = Tema.botonSecundario("Cancelar");
        JButton deshabilitado = Tema.botonSecundario("No disponible");
        deshabilitado.setEnabled(false);
        JLabel respuesta = Tema.mensaje("Demostración: prueba los botones.", Tema.SECUNDARIO);
        guardar.addActionListener(e -> { respuesta.setText("Éxito: ejemplo guardado en esta pantalla."); respuesta.setForeground(Tema.EXITO); });
        cancelar.addActionListener(e -> { respuesta.setText("Demostración: acción cancelada."); respuesta.setForeground(Tema.SECUNDARIO); });
        acciones.add(guardar); acciones.add(cancelar);
        cuerpo.add(acciones);
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(deshabilitado);
        cuerpo.add(Box.createVerticalStrut(16));
        cuerpo.add(respuesta);
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(parrafo("Una acción principal por sección. Usa Tab para revisar el foco y Espacio para activar."));
        return seccion("03 / Botones y estados", cuerpo);
    }

    private JPanel formulario() {
        JPanel cuerpo = columna();
        JTextField titulo = Tema.campo("Viaje a las estrellas", 18);
        JLabel etiqueta = Tema.texto("Título de la película", Tema.ETIQUETA, Tema.TEXTO);
        etiqueta.setLabelFor(titulo);
        cuerpo.add(etiqueta);
        cuerpo.add(Box.createVerticalStrut(8));
        titulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, Tema.ALTO_CONTROL));
        cuerpo.add(titulo);
        cuerpo.add(Box.createVerticalStrut(16));
        JTextField duracion = Tema.campo("118", 8);
        JLabel labelDuracion = Tema.texto("Duración (minutos)", Tema.ETIQUETA, Tema.TEXTO);
        labelDuracion.setLabelFor(duracion);
        cuerpo.add(labelDuracion);
        cuerpo.add(Box.createVerticalStrut(8));
        duracion.setMaximumSize(new Dimension(Integer.MAX_VALUE, Tema.ALTO_CONTROL));
        cuerpo.add(duracion);
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(parrafo("Etiqueta siempre visible. Campos de 40 px de alto y borde de foco en color principal."));
        return seccion("04 / Campos de formulario", cuerpo);
    }

    private JPanel mensajes() {
        JPanel cuerpo = columna();
        cuerpo.add(Tema.mensaje("Éxito · Película guardada correctamente.", Tema.EXITO));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(Tema.mensaje("Aviso · Selecciona una función para continuar.", Tema.AVISO));
        cuerpo.add(Box.createVerticalStrut(8));
        cuerpo.add(Tema.mensaje("Error · Revisa la duración de la película.", Tema.ERROR));
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(parrafo("Indiquen el estado con palabras y color; expliquen cómo resolver los errores."));
        return seccion("05 / Mensajes", cuerpo);
    }

    private JPanel espaciado() {
        JPanel cuerpo = columna();
        for (int medida : new int[]{8, 16, 24, 32}) {
            JPanel renglon = fila();
            JLabel numero = Tema.texto(medida + " px", Tema.ETIQUETA, Tema.TEXTO);
            numero.setPreferredSize(new Dimension(48, 24));
            renglon.add(numero);
            JPanel barra = new JPanel();
            barra.setBackground(Tema.PRIMARIO);
            barra.setPreferredSize(new Dimension(medida * 5, 8));
            renglon.add(barra);
            cuerpo.add(renglon);
        }
        cuerpo.add(Box.createVerticalStrut(12));
        cuerpo.add(parrafo("8: etiqueta y campo · 16: entre controles y tarjetas · 24: margen interior · 32: separación de bloques."));
        return seccion("06 / Espaciado", cuerpo);
    }

    private JPanel tablaEjemplo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        JTable tabla = tabla(new String[]{"Película", "Sala", "Horario", "Estado"}, new Object[][]{
                {"Viaje a las estrellas", "Sala 01", "16:30", "Disponible"},
                {"La última función", "Sala 02", "18:00", "Pocos asientos"},
                {"Un verano juntos", "Sala 03", "20:15", "Agotada"}
        });
        JScrollPane scroll = Tema.tabla(tabla);
        scroll.setPreferredSize(new Dimension(600, 172));
        cuerpo.add(scroll, BorderLayout.CENTER);
        cuerpo.add(parrafo("Filas de 44 px. Selecciona una fila para ver el estado activo. Datos ficticios."), BorderLayout.SOUTH);
        return seccion("07 / Tablas", cuerpo);
    }

    private JPanel cartelera() {
        JPanel pagina = pagina();
        pagina.add(Tema.texto("ASÍ SE VE APLICADO", Tema.ETIQUETA, Tema.PRIMARIO));
        pagina.add(Box.createVerticalStrut(8));
        pagina.add(Tema.texto("Cartelera", Tema.TITULO, Tema.TEXTO));
        pagina.add(Box.createVerticalStrut(8));
        pagina.add(parrafo("Una muestra de composición: título, ayuda, tarjetas y una acción clara por película."));
        JPanel peliculas = new JPanel(new GridLayout(1, 3, 16, 0));
        peliculas.setOpaque(false);
        JLabel seleccion = Tema.mensaje("Selecciona un horario para ver el resumen de ejemplo.", Tema.SECUNDARIO);
        String[] nombres = {"Viaje a las estrellas", "La última función", "Un verano juntos"};
        String[] detalles = {"Ciencia ficción · 118 min · +12", "Suspenso · 102 min · +15", "Comedia · 95 min · Todo público"};
        Color[] colores = {new Color(30, 57, 89), new Color(84, 36, 57), new Color(32, 85, 79)};
        for (int i = 0; i < nombres.length; i++) {
            JPanel tarjeta = Tema.tarjeta();
            JPanel cuerpo = columna();
            final String nombre = nombres[i];
            cuerpo.add(new Poster(colores[i], "0" + (i + 1)));
            cuerpo.add(Box.createVerticalStrut(16));
            cuerpo.add(Tema.texto(nombre, Tema.SUBTITULO, Tema.TEXTO));
            cuerpo.add(Box.createVerticalStrut(8));
            cuerpo.add(parrafo(detalles[i]));
            cuerpo.add(Box.createVerticalStrut(16));
            JButton horario = Tema.botonPrimario(new String[]{"16:30 · Ver función", "18:00 · Ver función", "20:15 · Ver función"}[i]);
            horario.addActionListener(e -> { seleccion.setText("Ejemplo seleccionado: " + nombre + ". Continúa al mapa en el sistema real."); seleccion.setForeground(Tema.EXITO); });
            cuerpo.add(horario);
            tarjeta.add(cuerpo);
            peliculas.add(tarjeta);
        }
        agregar(pagina, peliculas);
        agregar(pagina, seleccion);
        agregar(pagina, seccion("El mismo lenguaje visual en cada módulo", parrafo(
                "Login: formulario y botón principal. CRUD: tabla y campos. Salas: tarjetas y estados. "
                + "Venta: resumen y confirmación. Ticket: jerarquía clara de película, horario y asientos.")));
        return pagina;
    }

    private JPanel equipo() {
        JPanel pagina = pagina();
        pagina.add(Tema.texto("CINCO PERSONAS / UNA REFERENCIA", Tema.ETIQUETA, Tema.PRIMARIO));
        pagina.add(Box.createVerticalStrut(8));
        pagina.add(Tema.texto("Acuerdos para trabajar juntos", Tema.TITULO, Tema.TEXTO));
        JTable responsables = tabla(new String[]{"Responsable", "Pantallas", "Archivos de referencia"}, new Object[][]{
                {"Persona 1", "Acceso, navegación y carga", "Login / MDI / LoadingView"},
                {"Persona 2", "Películas y cartelera", "PeliculasView / futura CarteleraView"},
                {"Persona 3", "Salas y selección de asientos", "SalasView / MapaAsientosView"},
                {"Persona 4", "Funciones y configuración", "FuncionesView / ConfiguracionView"},
                {"Persona 5", "Ventas, recibo y caja", "TaquillaView / TicketReciboView / CorteCajaView"}
        });
        responsables.getColumnModel().getColumn(0).setPreferredWidth(100);
        responsables.getColumnModel().getColumn(1).setPreferredWidth(260);
        responsables.getColumnModel().getColumn(2).setPreferredWidth(400);
        JScrollPane scroll = Tema.tabla(responsables);
        scroll.setPreferredSize(new Dimension(800, 260));
        agregar(pagina, seccion("Responsabilidad de archivos", scroll));
        agregar(pagina, seccion("Regla de integración", parrafo(
                "Cada persona modifica sus vistas. La persona 1 integra la navegación en MDI y coordina los cambios "
                + "en Tema.java. Usen los estilos compartidos y mantengan sincronizados los archivos .java y .form "
                + "cuando trabajen con el diseñador de NetBeans.")));
        JPanel revision = columna();
        revision.add(parrafo("Marquen durante la revisión conjunta. Esta lista es temporal y se reinicia al cerrar la guía."));
        revision.add(Box.createVerticalStrut(12));
        String[] pasos = {
                "Login: etiquetas, foco de teclado y mensajes legibles.",
                "Cartelera: tarjetas, títulos y horarios consistentes (pantalla pendiente de implementar).",
                "Venta → asientos → ticket: selección clara, importes y resumen legibles.",
                "Administración: revisar películas, salas, funciones, configuración y corte de caja.",
                "Comprobar márgenes, botones y tablas al cambiar el tamaño de la ventana."
        };
        for (String paso : pasos) {
            JCheckBox check = new JCheckBox(paso);
            check.setOpaque(false);
            check.setFont(Tema.CUERPO);
            check.setForeground(Tema.TEXTO);
            check.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            revision.add(check);
        }
        agregar(pagina, seccion("Lista de revisión", revision));
        return pagina;
    }

    private static JTable tabla(String[] columnas, Object[][] filas) {
        return new JTable(new DefaultTableModel(filas, columnas) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        });
    }

    private static JPanel seccion(String titulo, JComponent contenido) {
        JPanel tarjeta = Tema.tarjeta();
        tarjeta.add(Tema.texto(titulo, Tema.SUBTITULO, Tema.TEXTO), BorderLayout.NORTH);
        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }

    private static JTextArea parrafo(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setFont(Tema.CUERPO);
        area.setForeground(Tema.SECUNDARIO);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(20);
        // Reservar espacio para la ayuda incluso antes del primer cálculo del ancho.
        area.setRows(texto.length() > 140 ? 3 : 2);
        return area;
    }

    private static JPanel columna() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JPanel fila() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    private static JPanel pagina() {
        JPanel panel = columna();
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        return panel;
    }

    private static void agregar(JPanel pagina, JComponent componente) {
        pagina.add(Box.createVerticalStrut(24));
        pagina.add(componente);
    }

    private static JScrollPane desplazable(JPanel pagina) {
        // Alinear todos los hijos de BoxLayout al inicio evita desplazamientos laterales.
        alinear(pagina);
        JPanel superior = new JPanel(new BorderLayout());
        superior.setBackground(Tema.FONDO);
        superior.add(pagina, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(superior);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    private static void alinear(Container padre) {
        for (Component hijo : padre.getComponents()) {
            if (hijo instanceof JComponent componente) componente.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (hijo instanceof JPanel panel) alinear(panel);
        }
    }

    /** Ilustración geométrica: no necesita imágenes ni recursos externos. */
    private static class Poster extends JPanel {
        private final Color color;
        private final String numero;
        Poster(Color color, String numero) {
            this.color = color;
            this.numero = numero;
            setPreferredSize(new Dimension(200, 210));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
            getAccessibleContext().setAccessibleName("Póster ilustrativo " + numero);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            dibujo.setPaint(new GradientPaint(0, 0, color, getWidth(), getHeight(), Tema.TEXTO));
            dibujo.fillRect(0, 0, getWidth(), getHeight());
            dibujo.setColor(new Color(255, 255, 255, 25));
            for (int i = 0; i < 4; i++) dibujo.drawOval(getWidth() / 2 - 60 - i * 22, 24 - i * 22, 160 + i * 44, 160 + i * 44);
            dibujo.setColor(Color.WHITE);
            dibujo.setFont(Tema.TITULO.deriveFont(64f));
            dibujo.drawString(numero, 24, 142);
            dibujo.setFont(Tema.ETIQUETA);
            dibujo.drawString("C I N E  /  CARTELERA", 24, 180);
            dibujo.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Cine · Guía de estilos del equipo");
            ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            ventana.setContentPane(new GuiaEstilosView());
            ventana.setMinimumSize(new Dimension(1000, 680));
            ventana.setSize(1180, 850);
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }
}
