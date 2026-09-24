package views;

import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;
import javax.swing.*;
import views.estilos.Tema;

/** Recibo de la venta presentado como un boleto de cine. */
public class TicketReciboView extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TicketReciboView.class.getName());

    /** Una línea del recibo: un boleto por asiento. */
    public record Linea(int ticket, String asiento, String monto) {}

    /** Todo lo que se imprime en el boleto, ya formateado para mostrar. */
    public record Datos(String pelicula, String fecha, String hora, int sala, int funcion,
            String cajero, String compra, List<Linea> lineas, String total) {
        public Datos { lineas = List.copyOf(lineas); }

        /** Versión en texto plano del recibo (lectores de pantalla y pruebas). */
        public String comoTexto() {
            StringBuilder texto = new StringBuilder("RECIBO DE VENTA\n\n");
            texto.append("Película: ").append(pelicula).append('\n');
            texto.append("Función: ").append(funcion).append(" · Sala: ").append(sala).append('\n');
            texto.append("Fecha: ").append(fecha).append(" · Hora: ").append(hora).append('\n');
            texto.append("Cajero: ").append(cajero).append("\n\n");
            for (Linea l : lineas) texto.append("Ticket #").append(l.ticket()).append("  Asiento ").append(l.asiento())
                    .append("  $").append(l.monto()).append('\n');
            texto.append("\nCompra: ").append(compra).append('\n');
            return texto.append("TOTAL: $").append(total).append('\n').toString();
        }
    }

    public TicketReciboView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        aplicarEstilos();
        btnImprimir.addActionListener(e -> imprimir());
        btnCerrar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnCerrar);
    }

    /** Bordes compuestos y ajustes del desplazamiento que el diseñador no representa. */
    private void aplicarEstilos() {
        pnlCabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Tema.PRIMARIO), pnlCabecera.getBorder()));
        scrBoleto.getViewport().setBackground(Tema.FONDO);
        scrBoleto.getVerticalScrollBar().setUnitIncrement(24);
        scrBoleto.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setMinimumSize(new Dimension(460, 520));
    }

    /** Llena el boleto y ajusta el tamaño del diálogo a la pantalla. */
    public void mostrarTicket(Datos datos) {
        int cantidad = datos.lineas().size();
        lblExito.setText("Venta registrada · " + cantidad + (cantidad == 1 ? " boleto" : " boletos") + " · Total $" + datos.total());
        lblPelicula.setText("<html><body style='width:340px'>" + html(datos.pelicula()) + "</body></html>");
        lblFecha.setText(datos.fecha());
        lblHora.setText(datos.hora());
        lblSala.setText(String.format("%02d", datos.sala()));
        lblFuncion.setText("#" + datos.funcion());
        pnlAsientos.removeAll();
        pnlLineas.removeAll();
        for (Linea l : datos.lineas()) {
            pnlAsientos.add(ficha(l.asiento()));
            pnlLineas.add(renglon(Tema.texto("Ticket #" + l.ticket() + "  ·  Asiento " + l.asiento(), Tema.CUERPO, Tema.SECUNDARIO),
                    Tema.texto("$" + l.monto(), Tema.CUERPO, Tema.TEXTO)));
        }
        lblTotal.setText("$" + datos.total());
        ((CodigoBarras) pnlCodigo).semilla = datos.lineas().stream().mapToInt(Linea::ticket).sum() * 31L + datos.funcion();
        lblAtendio.setText("Atendió: " + datos.cajero() + "   ·   " + datos.compra());
        getAccessibleContext().setAccessibleDescription(datos.comoTexto());
        pack();
        int alto = GraphicsEnvironment.isHeadless() ? getHeight()
                : Math.min(getHeight(), GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - 40);
        setSize(Math.max(getWidth(), 480), alto);
    }

    private static JLabel ficha(String asiento) {
        JLabel ficha = Tema.texto(asiento, Tema.CUERPO.deriveFont(Font.BOLD), Tema.PRIMARIO);
        ficha.setOpaque(true);
        ficha.setBackground(new Color(0xFB, 0xE9, 0xED));
        ficha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, Tema.ESPACIO, Tema.ESPACIO, Tema.SUPERFICIE),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.PRIMARIO),
                        BorderFactory.createEmptyBorder(4, 12, 4, 12))));
        return ficha;
    }

    private static JPanel renglon(JLabel izquierda, JLabel derecha) {
        JPanel panel = new JPanel(new BorderLayout(Tema.SEPARACION, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        panel.add(izquierda, BorderLayout.WEST);
        panel.add(derecha, BorderLayout.EAST);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private static String html(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void imprimir() {
        PrinterJob trabajo = PrinterJob.getPrinterJob();
        trabajo.setJobName("Ticket de compra");
        trabajo.setPrintable((grafico, formato, pagina) -> {
            if (pagina > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g = (Graphics2D) grafico;
            g.translate(formato.getImageableX(), formato.getImageableY());
            double escala = Math.min(1, Math.min(formato.getImageableWidth() / pnlBoleto.getWidth(),
                    formato.getImageableHeight() / pnlBoleto.getHeight()));
            g.scale(escala, escala);
            pnlBoleto.printAll(g);
            return Printable.PAGE_EXISTS;
        });
        if (!trabajo.printDialog()) return;
        try { trabajo.print(); }
        catch (PrinterException ex) { utils.Tareas.error(this, "No se pudo imprimir el recibo: " + ex.getMessage()); }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRaiz = new javax.swing.JPanel();
        lblExito = views.estilos.Tema.mensaje("", views.estilos.Tema.EXITO);
        scrBoleto = new javax.swing.JScrollPane();
        pnlBoleto = new Boleto();
        pnlCabecera = new javax.swing.JPanel();
        lblMarca = new javax.swing.JLabel();
        lblPelicula = new javax.swing.JLabel();
        pnlDetalle = new javax.swing.JPanel();
        pnlDatos = new javax.swing.JPanel();
        pnlFecha = new javax.swing.JPanel();
        lblFechaTitulo = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        pnlHora = new javax.swing.JPanel();
        lblHoraTitulo = new javax.swing.JLabel();
        lblHora = new javax.swing.JLabel();
        pnlSala = new javax.swing.JPanel();
        lblSalaTitulo = new javax.swing.JLabel();
        lblSala = new javax.swing.JLabel();
        pnlFuncion = new javax.swing.JPanel();
        lblFuncionTitulo = new javax.swing.JLabel();
        lblFuncion = new javax.swing.JLabel();
        lblAsientosTitulo = new javax.swing.JLabel();
        pnlAsientos = new Fichas();
        pnlPerforacion = new Perforacion();
        pnlPie = new javax.swing.JPanel();
        pnlLineas = new javax.swing.JPanel();
        pnlTotal = new javax.swing.JPanel();
        lblTotalTitulo = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        pnlCodigo = new CodigoBarras();
        lblAtendio = new javax.swing.JLabel();
        lblGracias = new javax.swing.JLabel();
        pnlAcciones = new javax.swing.JPanel();
        btnImprimir = views.estilos.Tema.botonSecundario("");
        btnCerrar = views.estilos.Tema.botonPrimario("");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ticket de compra");

        pnlRaiz.setBackground(new java.awt.Color(244, 245, 247));
        pnlRaiz.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlRaiz.setLayout(new java.awt.BorderLayout(0, 16));

        lblExito.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblExito.setForeground(new java.awt.Color(24, 115, 76));
        lblExito.setText("Venta registrada correctamente.");
        pnlRaiz.add(lblExito, java.awt.BorderLayout.NORTH);

        scrBoleto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlBoleto.setOpaque(false);
        pnlBoleto.setLayout(new javax.swing.BoxLayout(pnlBoleto, javax.swing.BoxLayout.PAGE_AXIS));

        pnlCabecera.setBackground(new java.awt.Color(24, 34, 53));
        pnlCabecera.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 20, 24));
        pnlCabecera.setAlignmentX(0.0F);
        pnlCabecera.setLayout(new javax.swing.BoxLayout(pnlCabecera, javax.swing.BoxLayout.PAGE_AXIS));

        lblMarca.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblMarca.setForeground(new java.awt.Color(255, 165, 180));
        lblMarca.setText("C I N E   /   BOLETO DE ENTRADA");
        lblMarca.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlCabecera.add(lblMarca);

        lblPelicula.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        lblPelicula.setForeground(new java.awt.Color(255, 255, 255));
        lblPelicula.setText("Película");
        pnlCabecera.add(lblPelicula);

        pnlBoleto.add(pnlCabecera);

        pnlDetalle.setOpaque(false);
        pnlDetalle.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 8, 24));
        pnlDetalle.setAlignmentX(0.0F);
        pnlDetalle.setLayout(new javax.swing.BoxLayout(pnlDetalle, javax.swing.BoxLayout.PAGE_AXIS));

        pnlDatos.setOpaque(false);
        pnlDatos.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 24, 0));
        pnlDatos.setAlignmentX(0.0F);
        pnlDatos.setLayout(new java.awt.GridLayout(2, 2, 16, 16));

        pnlFecha.setOpaque(false);
        pnlFecha.setLayout(new javax.swing.BoxLayout(pnlFecha, javax.swing.BoxLayout.PAGE_AXIS));

        lblFechaTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFechaTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblFechaTitulo.setText("FECHA");
        lblFechaTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlFecha.add(lblFechaTitulo);

        lblFecha.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblFecha.setForeground(new java.awt.Color(24, 34, 53));
        lblFecha.setText("—");
        pnlFecha.add(lblFecha);

        pnlDatos.add(pnlFecha);

        pnlHora.setOpaque(false);
        pnlHora.setLayout(new javax.swing.BoxLayout(pnlHora, javax.swing.BoxLayout.PAGE_AXIS));

        lblHoraTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblHoraTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblHoraTitulo.setText("HORA");
        lblHoraTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlHora.add(lblHoraTitulo);

        lblHora.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblHora.setForeground(new java.awt.Color(24, 34, 53));
        lblHora.setText("—");
        pnlHora.add(lblHora);

        pnlDatos.add(pnlHora);

        pnlSala.setOpaque(false);
        pnlSala.setLayout(new javax.swing.BoxLayout(pnlSala, javax.swing.BoxLayout.PAGE_AXIS));

        lblSalaTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblSalaTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblSalaTitulo.setText("SALA");
        lblSalaTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlSala.add(lblSalaTitulo);

        lblSala.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblSala.setForeground(new java.awt.Color(24, 34, 53));
        lblSala.setText("—");
        pnlSala.add(lblSala);

        pnlDatos.add(pnlSala);

        pnlFuncion.setOpaque(false);
        pnlFuncion.setLayout(new javax.swing.BoxLayout(pnlFuncion, javax.swing.BoxLayout.PAGE_AXIS));

        lblFuncionTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFuncionTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblFuncionTitulo.setText("FUNCIÓN");
        lblFuncionTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlFuncion.add(lblFuncionTitulo);

        lblFuncion.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblFuncion.setForeground(new java.awt.Color(24, 34, 53));
        lblFuncion.setText("—");
        pnlFuncion.add(lblFuncion);

        pnlDatos.add(pnlFuncion);

        pnlDetalle.add(pnlDatos);

        lblAsientosTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblAsientosTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblAsientosTitulo.setText("ASIENTOS");
        lblAsientosTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlDetalle.add(lblAsientosTitulo);

        pnlAsientos.setOpaque(false);
        pnlAsientos.setAlignmentX(0.0F);
        pnlAsientos.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 0));
        pnlDetalle.add(pnlAsientos);

        pnlBoleto.add(pnlDetalle);

        pnlPerforacion.setOpaque(false);
        pnlPerforacion.setMaximumSize(new java.awt.Dimension(32767, 24));
        pnlPerforacion.setPreferredSize(new java.awt.Dimension(440, 24));
        pnlPerforacion.setAlignmentX(0.0F);
        pnlPerforacion.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        pnlBoleto.add(pnlPerforacion);

        pnlPie.setOpaque(false);
        pnlPie.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 24, 24, 24));
        pnlPie.setAlignmentX(0.0F);
        pnlPie.setLayout(new javax.swing.BoxLayout(pnlPie, javax.swing.BoxLayout.PAGE_AXIS));

        pnlLineas.setOpaque(false);
        pnlLineas.setAlignmentX(0.0F);
        pnlLineas.setLayout(new javax.swing.BoxLayout(pnlLineas, javax.swing.BoxLayout.PAGE_AXIS));
        pnlPie.add(pnlLineas);

        pnlTotal.setOpaque(false);
        pnlTotal.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 24, 0));
        pnlTotal.setAlignmentX(0.0F);
        pnlTotal.setLayout(new java.awt.BorderLayout(16, 0));

        lblTotalTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTotalTitulo.setForeground(new java.awt.Color(24, 34, 53));
        lblTotalTitulo.setText("TOTAL PAGADO");
        pnlTotal.add(lblTotalTitulo, java.awt.BorderLayout.WEST);

        lblTotal.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(180, 35, 60));
        lblTotal.setText("$0.00");
        pnlTotal.add(lblTotal, java.awt.BorderLayout.EAST);

        pnlPie.add(pnlTotal);

        pnlCodigo.setOpaque(false);
        pnlCodigo.setMaximumSize(new java.awt.Dimension(32767, 48));
        pnlCodigo.setPreferredSize(new java.awt.Dimension(300, 48));
        pnlCodigo.setAlignmentX(0.0F);
        pnlCodigo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        pnlPie.add(pnlCodigo);

        lblAtendio.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblAtendio.setForeground(new java.awt.Color(88, 101, 121));
        lblAtendio.setText("Atendió: —");
        lblAtendio.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAtendio.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 4, 0));
        lblAtendio.setMaximumSize(new java.awt.Dimension(32767, 30));
        pnlPie.add(lblAtendio);

        lblGracias.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblGracias.setForeground(new java.awt.Color(88, 101, 121));
        lblGracias.setText("Presenta este boleto en la entrada de la sala. ¡Disfruta la función!");
        lblGracias.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGracias.setMaximumSize(new java.awt.Dimension(32767, 20));
        pnlPie.add(lblGracias);

        pnlBoleto.add(pnlPie);

        scrBoleto.setViewportView(pnlBoleto);

        pnlRaiz.add(scrBoleto, java.awt.BorderLayout.CENTER);

        pnlAcciones.setOpaque(false);
        pnlAcciones.setLayout(new java.awt.GridLayout(1, 2, 16, 0));

        btnImprimir.setText("Imprimir");
        pnlAcciones.add(btnImprimir);

        btnCerrar.setText("Finalizar venta");
        pnlAcciones.add(btnCerrar);

        pnlRaiz.add(pnlAcciones, java.awt.BorderLayout.SOUTH);

        getContentPane().add(pnlRaiz, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /** Fondo del boleto con muescas laterales a la altura de la perforación. */
    private static class Boleto extends JPanel {
        private static final int MUESCA = 12;
        @Override protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), y = h / 2;
            for (Component hijo : getComponents()) if (hijo instanceof Perforacion p) y = p.getY() + p.getHeight() / 2;
            dibujo.setColor(Tema.SUPERFICIE);
            dibujo.fillRect(0, 0, w, h);
            dibujo.setColor(Tema.BORDE);
            dibujo.drawRect(0, 0, w - 1, h - 1);
            dibujo.setColor(Tema.FONDO);
            dibujo.fillOval(-MUESCA, y - MUESCA, MUESCA * 2, MUESCA * 2);
            dibujo.fillOval(w - MUESCA, y - MUESCA, MUESCA * 2, MUESCA * 2);
            dibujo.setColor(Tema.BORDE);
            dibujo.drawArc(-MUESCA, y - MUESCA, MUESCA * 2, MUESCA * 2, -90, 180);
            dibujo.drawArc(w - MUESCA - 1, y - MUESCA, MUESCA * 2, MUESCA * 2, 90, 180);
            dibujo.dispose();
        }
    }

    /** Línea discontinua entre el talón y el detalle del cobro. */
    private static class Perforacion extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setColor(Tema.BORDE);
            dibujo.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{8, 6}, 0));
            dibujo.drawLine(Tema.MARGEN, getHeight() / 2, getWidth() - Tema.MARGEN, getHeight() / 2);
            dibujo.dispose();
        }
    }

    /** Fichas de asientos: calcula su alto para que FlowLayout pueda usar varias filas. */
    private static class Fichas extends JPanel {
        @Override public Dimension getPreferredSize() {
            int ancho = 392, x = 0, filas = 1;
            for (Component c : getComponents()) {
                int w = c.getPreferredSize().width;
                if (x > 0 && x + w > ancho) { filas++; x = 0; }
                x += w;
            }
            return new Dimension(ancho, getComponentCount() == 0 ? 0 : filas * 40);
        }
    }

    /** Código de barras decorativo, estable para la misma venta. No es un formato legible por escáner. */
    private static class CodigoBarras extends JPanel {
        long semilla;
        @Override protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setColor(Tema.TEXTO);
            java.util.Random azar = new java.util.Random(semilla);
            int ancho = Math.min(getWidth(), 300), x = (getWidth() - ancho) / 2, fin = x + ancho;
            while (x < fin) {
                int barra = 1 + azar.nextInt(3);
                dibujo.fillRect(x, 0, Math.min(barra, fin - x), getHeight());
                x += barra + 1 + azar.nextInt(3);
            }
            dibujo.dispose();
        }
    }

    /** Vista previa con datos ficticios: no necesita base de datos ni sesión. */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            TicketReciboView dialog = new TicketReciboView(new javax.swing.JFrame(), true);
            dialog.mostrarTicket(new Datos("Viaje a las estrellas", "01/10/2026", "18:00", 2, 7, "Cajero de prueba",
                    "23/09/2026 10:15", List.of(new Linea(51, "B-1", "4.50"), new Linea(52, "B-2", "4.50"),
                            new Linea(53, "B-3", "4.50")), "13.50"));
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            System.exit(0);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JLabel lblAsientosTitulo;
    private javax.swing.JLabel lblAtendio;
    private javax.swing.JLabel lblExito;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaTitulo;
    private javax.swing.JLabel lblFuncion;
    private javax.swing.JLabel lblFuncionTitulo;
    private javax.swing.JLabel lblGracias;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblHoraTitulo;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblPelicula;
    private javax.swing.JLabel lblSala;
    private javax.swing.JLabel lblSalaTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JPanel pnlAcciones;
    private javax.swing.JPanel pnlAsientos;
    private javax.swing.JPanel pnlBoleto;
    private javax.swing.JPanel pnlCabecera;
    private javax.swing.JPanel pnlCodigo;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JPanel pnlFecha;
    private javax.swing.JPanel pnlFuncion;
    private javax.swing.JPanel pnlHora;
    private javax.swing.JPanel pnlLineas;
    private javax.swing.JPanel pnlPerforacion;
    private javax.swing.JPanel pnlPie;
    private javax.swing.JPanel pnlRaiz;
    private javax.swing.JPanel pnlSala;
    private javax.swing.JPanel pnlTotal;
    private javax.swing.JScrollPane scrBoleto;
    // End of variables declaration//GEN-END:variables
}
