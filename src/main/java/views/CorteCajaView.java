package views;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import views.estilos.Tema;

/** Cierre de caja: filtros de periodo, indicadores, ventas por cajero y totales. */
public class CorteCajaView extends javax.swing.JPanel {
    public static final int COL_CAJERO = 0, COL_TICKETS = 1, COL_TOTAL = 2, COL_PARTICIPACION = 3;

    public CorteCajaView() {
        initComponents();
        aplicarEstilos();
    }

    /** Bordes compuestos, botones segmentados y renderizadores de tabla que el diseñador no representa. */
    private void aplicarEstilos() {
        scrPagina.getViewport().setBackground(Tema.FONDO);
        scrPagina.getVerticalScrollBar().setUnitIncrement(24);
        tarjeta(pnlFiltros, Tema.BORDE);
        tarjeta(pnlDetalle, Tema.BORDE);
        indicador(pnlIndIngresos, Tema.TEXTO, Tema.PRIMARIO);
        indicador(pnlIndTickets, Tema.BORDE, Tema.TEXTO);
        indicador(pnlIndPromedio, Tema.BORDE, Tema.EXITO);
        indicador(pnlIndCajeros, Tema.BORDE, Tema.AVISO);
        pnlTotales.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Tema.TEXTO), pnlTotales.getBorder()));
        pnlSegmentos.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        for (JToggleButton boton : new JToggleButton[]{tglDiario, tglMensual, tglAnual}) {
            boton.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
            boton.addItemListener(e -> segmento(boton));
            segmento(boton);
        }
        lblVacio.setVisible(false);

        // Tema.tabla envuelve la tabla en otro JScrollPane: se devuelve a su contenedor del formulario.
        Tema.tabla(tblVentasCajeros);
        scrTabla.setViewportView(tblVentasCajeros);
        scrTabla.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        scrTabla.getViewport().setBackground(Tema.SUPERFICIE);
        TableCellRenderer base = tblVentasCajeros.getDefaultRenderer(Object.class);
        // Conserva cebra y selección de Tema; alinea las cifras a la derecha.
        tblVentasCajeros.setDefaultRenderer(Object.class, (t, v, sel, foco, f, c) -> {
            Component celda = base.getTableCellRendererComponent(t, v, sel, foco, f, c);
            if (celda instanceof JLabel etiqueta) {
                etiqueta.setHorizontalAlignment(c == COL_CAJERO ? SwingConstants.LEADING : SwingConstants.TRAILING);
                etiqueta.setFont(c == COL_TOTAL ? Tema.CUERPO.deriveFont(Font.BOLD) : Tema.CUERPO);
            }
            return celda;
        });
        tblVentasCajeros.getColumnModel().getColumn(COL_PARTICIPACION).setCellRenderer(new Participacion(base));
        tblVentasCajeros.getColumnModel().getColumn(COL_CAJERO).setPreferredWidth(260);
        tblVentasCajeros.getColumnModel().getColumn(COL_PARTICIPACION).setPreferredWidth(220);
        TableCellRenderer cabecera = tblVentasCajeros.getTableHeader().getDefaultRenderer();
        tblVentasCajeros.getTableHeader().setDefaultRenderer((t, v, sel, foco, f, c) -> {
            Component celda = cabecera.getTableCellRendererComponent(t, v, sel, foco, f, c);
            if (celda instanceof JLabel etiqueta) {
                etiqueta.setHorizontalAlignment(c == COL_CAJERO ? SwingConstants.LEADING : SwingConstants.TRAILING);
                etiqueta.setText(String.valueOf(v).toUpperCase());
            }
            return celda;
        });
    }

    private static void tarjeta(JComponent panel, Color borde) {
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borde), panel.getBorder()));
    }

    private static void indicador(JComponent panel, Color borde, Color acento) {
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde), BorderFactory.createMatteBorder(4, 0, 0, 0, acento)), panel.getBorder()));
    }

    private static void segmento(JToggleButton boton) {
        boton.setBackground(boton.isSelected() ? Tema.TEXTO : Tema.SUPERFICIE);
        boton.setForeground(boton.isSelected() ? Color.WHITE : Tema.SECUNDARIO);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpTipo = new javax.swing.ButtonGroup();
        scrPagina = new javax.swing.JScrollPane();
        pnlPagina = new javax.swing.JPanel();
        pnlSuperior = new javax.swing.JPanel();
        pnlEncabezado = new javax.swing.JPanel();
        lblSeccion = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblPeriodo = new javax.swing.JLabel();
        pnlFiltros = new javax.swing.JPanel();
        pnlFiltroFecha = new javax.swing.JPanel();
        lblFiltroFecha = new javax.swing.JLabel();
        txtFecha = views.estilos.Tema.campo("", 10);
        pnlFiltroTipo = new javax.swing.JPanel();
        lblFiltroTipo = new javax.swing.JLabel();
        pnlSegmentos = new javax.swing.JPanel();
        tglDiario = new javax.swing.JToggleButton();
        tglMensual = new javax.swing.JToggleButton();
        tglAnual = new javax.swing.JToggleButton();
        pnlFiltroAccion = new javax.swing.JPanel();
        lblFiltroAccion = new javax.swing.JLabel();
        btnGenerarReporte = views.estilos.Tema.botonPrimario("");
        pnlIndicadores = new javax.swing.JPanel();
        pnlIndIngresos = new javax.swing.JPanel();
        lblIndIngresosTitulo = new javax.swing.JLabel();
        lblIngresos = new javax.swing.JLabel();
        lblIndIngresosAyuda = new javax.swing.JLabel();
        pnlIndTickets = new javax.swing.JPanel();
        lblIndTicketsTitulo = new javax.swing.JLabel();
        lblTickets = new javax.swing.JLabel();
        lblIndTicketsAyuda = new javax.swing.JLabel();
        pnlIndPromedio = new javax.swing.JPanel();
        lblIndPromedioTitulo = new javax.swing.JLabel();
        lblPromedio = new javax.swing.JLabel();
        lblIndPromedioAyuda = new javax.swing.JLabel();
        pnlIndCajeros = new javax.swing.JPanel();
        lblIndCajerosTitulo = new javax.swing.JLabel();
        lblCajeros = new javax.swing.JLabel();
        lblIndCajerosAyuda = new javax.swing.JLabel();
        pnlDetalle = new javax.swing.JPanel();
        pnlDetalleTitulo = new javax.swing.JPanel();
        lblDetalleTitulo = new javax.swing.JLabel();
        lblDetalleAyuda = new javax.swing.JLabel();
        pnlTabla = new javax.swing.JPanel();
        lblVacio = views.estilos.Tema.mensaje("", views.estilos.Tema.AVISO);
        scrTabla = new javax.swing.JScrollPane();
        tblVentasCajeros = new javax.swing.JTable();
        pnlTotales = new javax.swing.JPanel();
        lblTotalesTitulo = new javax.swing.JLabel();
        pnlCifras = new javax.swing.JPanel();
        lblTicketsTexto = new javax.swing.JLabel();
        lblTotalTickets = new javax.swing.JLabel();
        lblRecaudadoTexto = new javax.swing.JLabel();
        lblTotalRecaudado = new javax.swing.JLabel();

        setBackground(new java.awt.Color(244, 245, 247));
        setLayout(new java.awt.BorderLayout());

        scrPagina.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlPagina.setBackground(new java.awt.Color(244, 245, 247));
        pnlPagina.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 24, 32));
        pnlPagina.setLayout(new java.awt.BorderLayout(0, 24));

        pnlSuperior.setOpaque(false);
        pnlSuperior.setLayout(new java.awt.BorderLayout(0, 24));

        pnlEncabezado.setOpaque(false);
        pnlEncabezado.setLayout(new javax.swing.BoxLayout(pnlEncabezado, javax.swing.BoxLayout.PAGE_AXIS));

        lblSeccion.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblSeccion.setForeground(new java.awt.Color(180, 35, 60));
        lblSeccion.setText("ADMINISTRACIÓN  /  CIERRE DE CAJA");
        lblSeccion.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlEncabezado.add(lblSeccion);

        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 34, 53));
        lblTitulo.setText("Corte de caja");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlEncabezado.add(lblTitulo);

        lblPeriodo.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblPeriodo.setForeground(new java.awt.Color(88, 101, 121));
        lblPeriodo.setText("Periodo: —");
        pnlEncabezado.add(lblPeriodo);

        pnlSuperior.add(pnlEncabezado, java.awt.BorderLayout.NORTH);

        pnlFiltros.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltros.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 8, 16, 8));
        pnlFiltros.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 16, 0));

        pnlFiltroFecha.setOpaque(false);
        pnlFiltroFecha.setLayout(new javax.swing.BoxLayout(pnlFiltroFecha, javax.swing.BoxLayout.PAGE_AXIS));

        lblFiltroFecha.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFiltroFecha.setForeground(new java.awt.Color(88, 101, 121));
        lblFiltroFecha.setText("FECHA DE REFERENCIA");
        lblFiltroFecha.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlFiltroFecha.add(lblFiltroFecha);

        txtFecha.setToolTipText("Formato dd/mm/aaaa");
        txtFecha.setPreferredSize(new java.awt.Dimension(150, 40));
        txtFecha.setAlignmentX(0.0F);
        pnlFiltroFecha.add(txtFecha);

        pnlFiltros.add(pnlFiltroFecha);

        pnlFiltroTipo.setOpaque(false);
        pnlFiltroTipo.setLayout(new javax.swing.BoxLayout(pnlFiltroTipo, javax.swing.BoxLayout.PAGE_AXIS));

        lblFiltroTipo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFiltroTipo.setForeground(new java.awt.Color(88, 101, 121));
        lblFiltroTipo.setText("TIPO DE CORTE");
        lblFiltroTipo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlFiltroTipo.add(lblFiltroTipo);

        pnlSegmentos.setBackground(new java.awt.Color(255, 255, 255));
        pnlSegmentos.setAlignmentX(0.0F);
        pnlSegmentos.setLayout(new java.awt.GridLayout(1, 3, 0, 0));

        grpTipo.add(tglDiario);
        tglDiario.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        tglDiario.setSelected(true);
        tglDiario.setText("Diario");
        tglDiario.setPreferredSize(new java.awt.Dimension(110, 38));
        pnlSegmentos.add(tglDiario);

        grpTipo.add(tglMensual);
        tglMensual.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        tglMensual.setText("Mensual");
        tglMensual.setPreferredSize(new java.awt.Dimension(110, 38));
        pnlSegmentos.add(tglMensual);

        grpTipo.add(tglAnual);
        tglAnual.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        tglAnual.setText("Anual");
        tglAnual.setPreferredSize(new java.awt.Dimension(110, 38));
        pnlSegmentos.add(tglAnual);

        pnlFiltroTipo.add(pnlSegmentos);

        pnlFiltros.add(pnlFiltroTipo);

        pnlFiltroAccion.setOpaque(false);
        pnlFiltroAccion.setLayout(new javax.swing.BoxLayout(pnlFiltroAccion, javax.swing.BoxLayout.PAGE_AXIS));

        lblFiltroAccion.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblFiltroAccion.setForeground(new java.awt.Color(88, 101, 121));
        lblFiltroAccion.setText(" ");
        lblFiltroAccion.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlFiltroAccion.add(lblFiltroAccion);

        btnGenerarReporte.setText("Generar reporte");
        btnGenerarReporte.setPreferredSize(new java.awt.Dimension(170, 40));
        pnlFiltroAccion.add(btnGenerarReporte);

        pnlFiltros.add(pnlFiltroAccion);

        pnlSuperior.add(pnlFiltros, java.awt.BorderLayout.CENTER);

        pnlIndicadores.setOpaque(false);
        pnlIndicadores.setLayout(new java.awt.GridLayout(1, 4, 16, 0));

        pnlIndIngresos.setBackground(new java.awt.Color(24, 34, 53));
        pnlIndIngresos.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 20, 20));
        pnlIndIngresos.setLayout(new javax.swing.BoxLayout(pnlIndIngresos, javax.swing.BoxLayout.PAGE_AXIS));

        lblIndIngresosTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblIndIngresosTitulo.setForeground(new java.awt.Color(255, 165, 180));
        lblIndIngresosTitulo.setText("TOTAL INGRESOS");
        lblIndIngresosTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlIndIngresos.add(lblIndIngresosTitulo);

        lblIngresos.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblIngresos.setForeground(new java.awt.Color(255, 255, 255));
        lblIngresos.setText("$0.00");
        lblIngresos.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlIndIngresos.add(lblIngresos);

        lblIndIngresosAyuda.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblIndIngresosAyuda.setForeground(new java.awt.Color(210, 218, 230));
        lblIndIngresosAyuda.setText("Recaudado en el periodo");
        pnlIndIngresos.add(lblIndIngresosAyuda);

        pnlIndicadores.add(pnlIndIngresos);

        pnlIndTickets.setBackground(new java.awt.Color(255, 255, 255));
        pnlIndTickets.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 20, 20));
        pnlIndTickets.setLayout(new javax.swing.BoxLayout(pnlIndTickets, javax.swing.BoxLayout.PAGE_AXIS));

        lblIndTicketsTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblIndTicketsTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblIndTicketsTitulo.setText("TICKETS VENDIDOS");
        lblIndTicketsTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlIndTickets.add(lblIndTicketsTitulo);

        lblTickets.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblTickets.setForeground(new java.awt.Color(24, 34, 53));
        lblTickets.setText("0");
        lblTickets.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlIndTickets.add(lblTickets);

        lblIndTicketsAyuda.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblIndTicketsAyuda.setForeground(new java.awt.Color(88, 101, 121));
        lblIndTicketsAyuda.setText("Boletos emitidos");
        pnlIndTickets.add(lblIndTicketsAyuda);

        pnlIndicadores.add(pnlIndTickets);

        pnlIndPromedio.setBackground(new java.awt.Color(255, 255, 255));
        pnlIndPromedio.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 20, 20));
        pnlIndPromedio.setLayout(new javax.swing.BoxLayout(pnlIndPromedio, javax.swing.BoxLayout.PAGE_AXIS));

        lblIndPromedioTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblIndPromedioTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblIndPromedioTitulo.setText("PROMEDIO POR TICKET");
        lblIndPromedioTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlIndPromedio.add(lblIndPromedioTitulo);

        lblPromedio.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblPromedio.setForeground(new java.awt.Color(24, 34, 53));
        lblPromedio.setText("$0.00");
        lblPromedio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlIndPromedio.add(lblPromedio);

        lblIndPromedioAyuda.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblIndPromedioAyuda.setForeground(new java.awt.Color(88, 101, 121));
        lblIndPromedioAyuda.setText("Ingreso ÷ tickets");
        pnlIndPromedio.add(lblIndPromedioAyuda);

        pnlIndicadores.add(pnlIndPromedio);

        pnlIndCajeros.setBackground(new java.awt.Color(255, 255, 255));
        pnlIndCajeros.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 20, 20));
        pnlIndCajeros.setLayout(new javax.swing.BoxLayout(pnlIndCajeros, javax.swing.BoxLayout.PAGE_AXIS));

        lblIndCajerosTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblIndCajerosTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblIndCajerosTitulo.setText("CAJEROS CON VENTAS");
        lblIndCajerosTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlIndCajeros.add(lblIndCajerosTitulo);

        lblCajeros.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblCajeros.setForeground(new java.awt.Color(24, 34, 53));
        lblCajeros.setText("0");
        lblCajeros.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlIndCajeros.add(lblCajeros);

        lblIndCajerosAyuda.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblIndCajerosAyuda.setForeground(new java.awt.Color(88, 101, 121));
        lblIndCajerosAyuda.setText("Usuarios que vendieron");
        pnlIndCajeros.add(lblIndCajerosAyuda);

        pnlIndicadores.add(pnlIndCajeros);

        pnlSuperior.add(pnlIndicadores, java.awt.BorderLayout.SOUTH);

        pnlPagina.add(pnlSuperior, java.awt.BorderLayout.NORTH);

        pnlDetalle.setBackground(new java.awt.Color(255, 255, 255));
        pnlDetalle.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlDetalle.setLayout(new java.awt.BorderLayout(0, 16));

        pnlDetalleTitulo.setOpaque(false);
        pnlDetalleTitulo.setLayout(new java.awt.BorderLayout());

        lblDetalleTitulo.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblDetalleTitulo.setForeground(new java.awt.Color(24, 34, 53));
        lblDetalleTitulo.setText("Ventas por cajero");
        pnlDetalleTitulo.add(lblDetalleTitulo, java.awt.BorderLayout.WEST);

        lblDetalleAyuda.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblDetalleAyuda.setForeground(new java.awt.Color(88, 101, 121));
        lblDetalleAyuda.setText("Ordenado de mayor a menor recaudación");
        pnlDetalleTitulo.add(lblDetalleAyuda, java.awt.BorderLayout.EAST);

        pnlDetalle.add(pnlDetalleTitulo, java.awt.BorderLayout.NORTH);

        pnlTabla.setOpaque(false);
        pnlTabla.setLayout(new java.awt.BorderLayout(0, 16));

        lblVacio.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblVacio.setForeground(new java.awt.Color(138, 88, 0));
        lblVacio.setText("No hay ventas registradas en el periodo seleccionado.");
        pnlTabla.add(lblVacio, java.awt.BorderLayout.NORTH);

        scrTabla.setPreferredSize(new java.awt.Dimension(600, 258));

        tblVentasCajeros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cajero", "Tickets vendidos", "Total recaudado", "Participación"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrTabla.setViewportView(tblVentasCajeros);

        pnlTabla.add(scrTabla, java.awt.BorderLayout.CENTER);

        pnlDetalle.add(pnlTabla, java.awt.BorderLayout.CENTER);

        pnlTotales.setBackground(new java.awt.Color(244, 245, 247));
        pnlTotales.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        pnlTotales.setLayout(new java.awt.BorderLayout(16, 0));

        lblTotalesTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTotalesTitulo.setForeground(new java.awt.Color(24, 34, 53));
        lblTotalesTitulo.setText("TOTAL DEL CORTE");
        pnlTotales.add(lblTotalesTitulo, java.awt.BorderLayout.WEST);

        pnlCifras.setOpaque(false);
        pnlCifras.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 24, 0));

        lblTicketsTexto.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblTicketsTexto.setForeground(new java.awt.Color(88, 101, 121));
        lblTicketsTexto.setText("Tickets");
        pnlCifras.add(lblTicketsTexto);

        lblTotalTickets.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblTotalTickets.setForeground(new java.awt.Color(24, 34, 53));
        lblTotalTickets.setText("0");
        pnlCifras.add(lblTotalTickets);

        lblRecaudadoTexto.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblRecaudadoTexto.setForeground(new java.awt.Color(88, 101, 121));
        lblRecaudadoTexto.setText("Recaudado");
        pnlCifras.add(lblRecaudadoTexto);

        lblTotalRecaudado.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblTotalRecaudado.setForeground(new java.awt.Color(180, 35, 60));
        lblTotalRecaudado.setText("$0.00");
        pnlCifras.add(lblTotalRecaudado);

        pnlTotales.add(pnlCifras, java.awt.BorderLayout.EAST);

        pnlDetalle.add(pnlTotales, java.awt.BorderLayout.SOUTH);

        pnlPagina.add(pnlDetalle, java.awt.BorderLayout.CENTER);

        scrPagina.setViewportView(pnlPagina);

        add(scrPagina, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    public String getFecha() {
        return txtFecha.getText();
    }

    public void setFecha(String fecha) {
        txtFecha.setText(fecha);
    }

    public String getTipoReporte() {
        return tglMensual.isSelected() ? "Mensual" : tglAnual.isSelected() ? "Anual" : "Diario";
    }

    public void setPeriodo(String texto) {
        lblPeriodo.setText(texto);
    }

    public void setTicketsVendidos(String tickets) {
        lblTickets.setText(tickets);
        lblTotalTickets.setText(tickets);
    }

    public void setIngresos(String total, String promedio, int cajeros) {
        lblIngresos.setText(total);
        lblTotalRecaudado.setText(total);
        lblPromedio.setText(promedio);
        lblCajeros.setText(String.valueOf(cajeros));
        lblVacio.setVisible(cajeros == 0);
    }

    /** Filas: cajero, tickets, total recaudado (texto) y participación en porcentaje (número 0–100). */
    public javax.swing.JTable getTablaVentas() {
        return tblVentasCajeros;
    }

    public void addGenerarReporteListener(java.awt.event.ActionListener listener) {
        btnGenerarReporte.addActionListener(listener);
        txtFecha.addActionListener(listener);
    }

    /** Barra horizontal con el porcentaje del total que aportó cada cajero. */
    private static class Participacion extends JComponent implements TableCellRenderer {
        private final TableCellRenderer base;
        private Color fondo;
        private double porcentaje;
        Participacion(TableCellRenderer base) { this.base = base; }

        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
            fondo = base.getTableCellRendererComponent(t, "", sel, foco, f, c).getBackground();
            porcentaje = v instanceof Number n ? n.doubleValue() : 0;
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            dibujo.setColor(fondo);
            dibujo.fillRect(0, 0, getWidth(), getHeight());
            dibujo.setColor(Tema.BORDE);
            dibujo.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            String texto = String.format("%.1f %%", porcentaje);
            dibujo.setFont(Tema.CUERPO);
            FontMetrics m = dibujo.getFontMetrics();
            int anchoTexto = 64, x = 12, alto = 8, y = (getHeight() - alto) / 2;
            int anchoBarra = Math.max(0, getWidth() - anchoTexto - x - 12);
            dibujo.fillRoundRect(x, y, anchoBarra, alto, alto, alto);
            dibujo.setColor(Tema.PRIMARIO);
            dibujo.fillRoundRect(x, y, (int) Math.round(anchoBarra * Math.min(100, porcentaje) / 100), alto, alto, alto);
            dibujo.setColor(Tema.TEXTO);
            dibujo.drawString(texto, getWidth() - 12 - m.stringWidth(texto), (getHeight() - m.getHeight()) / 2 + m.getAscent());
            dibujo.dispose();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerarReporte;
    private javax.swing.ButtonGroup grpTipo;
    private javax.swing.JLabel lblCajeros;
    private javax.swing.JLabel lblDetalleAyuda;
    private javax.swing.JLabel lblDetalleTitulo;
    private javax.swing.JLabel lblFiltroAccion;
    private javax.swing.JLabel lblFiltroFecha;
    private javax.swing.JLabel lblFiltroTipo;
    private javax.swing.JLabel lblIndCajerosAyuda;
    private javax.swing.JLabel lblIndCajerosTitulo;
    private javax.swing.JLabel lblIndIngresosAyuda;
    private javax.swing.JLabel lblIndIngresosTitulo;
    private javax.swing.JLabel lblIndPromedioAyuda;
    private javax.swing.JLabel lblIndPromedioTitulo;
    private javax.swing.JLabel lblIndTicketsAyuda;
    private javax.swing.JLabel lblIndTicketsTitulo;
    private javax.swing.JLabel lblIngresos;
    private javax.swing.JLabel lblPeriodo;
    private javax.swing.JLabel lblPromedio;
    private javax.swing.JLabel lblRecaudadoTexto;
    private javax.swing.JLabel lblSeccion;
    private javax.swing.JLabel lblTickets;
    private javax.swing.JLabel lblTicketsTexto;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotalRecaudado;
    private javax.swing.JLabel lblTotalTickets;
    private javax.swing.JLabel lblTotalesTitulo;
    private javax.swing.JLabel lblVacio;
    private javax.swing.JPanel pnlCifras;
    private javax.swing.JPanel pnlDetalle;
    private javax.swing.JPanel pnlDetalleTitulo;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFiltroAccion;
    private javax.swing.JPanel pnlFiltroFecha;
    private javax.swing.JPanel pnlFiltroTipo;
    private javax.swing.JPanel pnlFiltros;
    private javax.swing.JPanel pnlIndCajeros;
    private javax.swing.JPanel pnlIndIngresos;
    private javax.swing.JPanel pnlIndPromedio;
    private javax.swing.JPanel pnlIndTickets;
    private javax.swing.JPanel pnlIndicadores;
    private javax.swing.JPanel pnlPagina;
    private javax.swing.JPanel pnlSegmentos;
    private javax.swing.JPanel pnlSuperior;
    private javax.swing.JPanel pnlTabla;
    private javax.swing.JPanel pnlTotales;
    private javax.swing.JScrollPane scrPagina;
    private javax.swing.JScrollPane scrTabla;
    private javax.swing.JTable tblVentasCajeros;
    private javax.swing.JToggleButton tglAnual;
    private javax.swing.JToggleButton tglDiario;
    private javax.swing.JToggleButton tglMensual;
    private javax.swing.JTextField txtFecha;
    // End of variables declaration//GEN-END:variables
}
