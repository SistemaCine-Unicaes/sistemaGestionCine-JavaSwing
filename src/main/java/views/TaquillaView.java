package views;

import java.awt.*;
import javax.swing.*;
import views.estilos.Tema;

/** Venta de boletos: selección de función a la izquierda y resumen de compra a la derecha. */
public class TaquillaView extends javax.swing.JPanel {

    public TaquillaView() {
        initComponents();
        aplicarEstilos();
    }

    /** Bordes compuestos y ajustes que el diseñador no representa; el resto vive en el .form. */
    private void aplicarEstilos() {
        tarjeta(pnlSeleccion);
        tarjeta(pnlResumen);
        pnlCabeceraResumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Tema.PRIMARIO), pnlCabeceraResumen.getBorder()));
        scrPagina.getViewport().setBackground(Tema.FONDO);
        scrPagina.getVerticalScrollBar().setUnitIncrement(24);
    }

    private static void tarjeta(JComponent panel) {
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE), panel.getBorder()));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrPagina = new javax.swing.JScrollPane();
        pnlPagina = new javax.swing.JPanel();
        pnlEncabezado = new javax.swing.JPanel();
        lblSeccion = new javax.swing.JLabel();
        lblTitulo = new javax.swing.JLabel();
        lblDescripcion = new javax.swing.JLabel();
        pnlCuerpo = new javax.swing.JPanel();
        pnlSeleccion = new javax.swing.JPanel();
        pnlPasos = new javax.swing.JPanel();
        lblDatosVenta = new javax.swing.JLabel();
        pnlPaso1 = new javax.swing.JPanel();
        pnlCirculo1 = new Circulo("1");
        pnlPasoCampos1 = new javax.swing.JPanel();
        lblPaso1 = new javax.swing.JLabel();
        lblAyuda1 = new javax.swing.JLabel();
        cbPelicula = new javax.swing.JComboBox<>();
        pnlPaso2 = new javax.swing.JPanel();
        pnlCirculo2 = new Circulo("2");
        pnlPasoCampos2 = new javax.swing.JPanel();
        lblPaso2 = new javax.swing.JLabel();
        lblAyuda2 = new javax.swing.JLabel();
        cbFuncion = new javax.swing.JComboBox<>();
        pnlPaso3 = new javax.swing.JPanel();
        pnlCirculo3 = new Circulo("3");
        pnlPasoCampos3 = new javax.swing.JPanel();
        lblPaso3 = new javax.swing.JLabel();
        lblAyuda3 = new javax.swing.JLabel();
        spnCantidad = new javax.swing.JSpinner();
        lblEstado = views.estilos.Tema.mensaje("", views.estilos.Tema.SECUNDARIO);
        pnlResumen = new javax.swing.JPanel();
        pnlCabeceraResumen = new javax.swing.JPanel();
        lblResumenTitulo = new javax.swing.JLabel();
        lblPelicula = new javax.swing.JLabel();
        pnlCuerpoResumen = new javax.swing.JPanel();
        pnlDetalleResumen = new javax.swing.JPanel();
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
        pnlBoletos = new javax.swing.JPanel();
        lblBoletosTitulo = new javax.swing.JLabel();
        lblBoletos = new javax.swing.JLabel();
        sepResumen = new Separador();
        pnlPrecio = new javax.swing.JPanel();
        lblPrecioTitulo = new javax.swing.JLabel();
        lblPrecio = new javax.swing.JLabel();
        pnlTotal = new javax.swing.JPanel();
        lblTotalTitulo = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnContinuar = views.estilos.Tema.botonPrimario("");
        lblNota = new javax.swing.JLabel();

        setBackground(new java.awt.Color(244, 245, 247));
        setLayout(new java.awt.BorderLayout());

        scrPagina.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnlPagina.setBackground(new java.awt.Color(244, 245, 247));
        pnlPagina.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 24, 32));
        pnlPagina.setLayout(new java.awt.BorderLayout(0, 24));

        pnlEncabezado.setOpaque(false);
        pnlEncabezado.setLayout(new javax.swing.BoxLayout(pnlEncabezado, javax.swing.BoxLayout.PAGE_AXIS));

        lblSeccion.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblSeccion.setForeground(new java.awt.Color(180, 35, 60));
        lblSeccion.setText("VENTAS  /  TAQUILLA");
        lblSeccion.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlEncabezado.add(lblSeccion);

        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(24, 34, 53));
        lblTitulo.setText("Venta de boletos");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlEncabezado.add(lblTitulo);

        lblDescripcion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblDescripcion.setForeground(new java.awt.Color(88, 101, 121));
        lblDescripcion.setText("Selecciona la función y la cantidad; en el siguiente paso eliges los asientos.");
        pnlEncabezado.add(lblDescripcion);

        pnlPagina.add(pnlEncabezado, java.awt.BorderLayout.NORTH);

        pnlCuerpo.setOpaque(false);
        pnlCuerpo.setLayout(new java.awt.GridLayout(1, 2, 16, 0));

        pnlSeleccion.setBackground(new java.awt.Color(255, 255, 255));
        pnlSeleccion.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlSeleccion.setLayout(new java.awt.BorderLayout());

        pnlPasos.setOpaque(false);
        pnlPasos.setLayout(new javax.swing.BoxLayout(pnlPasos, javax.swing.BoxLayout.PAGE_AXIS));

        lblDatosVenta.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblDatosVenta.setForeground(new java.awt.Color(24, 34, 53));
        lblDatosVenta.setText("Datos de la venta");
        lblDatosVenta.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 16, 0));
        pnlPasos.add(lblDatosVenta);

        pnlPaso1.setOpaque(false);
        pnlPaso1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 24, 0));
        pnlPaso1.setAlignmentX(0.0F);
        pnlPaso1.setLayout(new java.awt.BorderLayout(16, 0));

        pnlCirculo1.setOpaque(false);
        pnlCirculo1.setPreferredSize(new java.awt.Dimension(32, 32));
        pnlCirculo1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        pnlPaso1.add(pnlCirculo1, java.awt.BorderLayout.WEST);

        pnlPasoCampos1.setOpaque(false);
        pnlPasoCampos1.setLayout(new javax.swing.BoxLayout(pnlPasoCampos1, javax.swing.BoxLayout.PAGE_AXIS));

        lblPaso1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblPaso1.setForeground(new java.awt.Color(24, 34, 53));
        lblPaso1.setText("PELÍCULA");
        lblPaso1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlPasoCampos1.add(lblPaso1);

        lblAyuda1.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblAyuda1.setForeground(new java.awt.Color(88, 101, 121));
        lblAyuda1.setText("Solo aparecen películas con funciones futuras.");
        lblAyuda1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlPasoCampos1.add(lblAyuda1);

        cbPelicula.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cbPelicula.setForeground(new java.awt.Color(24, 34, 53));
        cbPelicula.setMaximumSize(new java.awt.Dimension(32767, 40));
        cbPelicula.setPreferredSize(new java.awt.Dimension(360, 40));
        cbPelicula.setAlignmentX(0.0F);
        pnlPasoCampos1.add(cbPelicula);

        pnlPaso1.add(pnlPasoCampos1, java.awt.BorderLayout.CENTER);

        pnlPasos.add(pnlPaso1);

        pnlPaso2.setOpaque(false);
        pnlPaso2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 24, 0));
        pnlPaso2.setAlignmentX(0.0F);
        pnlPaso2.setLayout(new java.awt.BorderLayout(16, 0));

        pnlCirculo2.setOpaque(false);
        pnlCirculo2.setPreferredSize(new java.awt.Dimension(32, 32));
        pnlCirculo2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        pnlPaso2.add(pnlCirculo2, java.awt.BorderLayout.WEST);

        pnlPasoCampos2.setOpaque(false);
        pnlPasoCampos2.setLayout(new javax.swing.BoxLayout(pnlPasoCampos2, javax.swing.BoxLayout.PAGE_AXIS));

        lblPaso2.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblPaso2.setForeground(new java.awt.Color(24, 34, 53));
        lblPaso2.setText("FUNCIÓN");
        lblPaso2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlPasoCampos2.add(lblPaso2);

        lblAyuda2.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblAyuda2.setForeground(new java.awt.Color(88, 101, 121));
        lblAyuda2.setText("Fecha, hora y sala de la proyección.");
        lblAyuda2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlPasoCampos2.add(lblAyuda2);

        cbFuncion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        cbFuncion.setForeground(new java.awt.Color(24, 34, 53));
        cbFuncion.setMaximumSize(new java.awt.Dimension(32767, 40));
        cbFuncion.setPreferredSize(new java.awt.Dimension(360, 40));
        cbFuncion.setAlignmentX(0.0F);
        pnlPasoCampos2.add(cbFuncion);

        pnlPaso2.add(pnlPasoCampos2, java.awt.BorderLayout.CENTER);

        pnlPasos.add(pnlPaso2);

        pnlPaso3.setOpaque(false);
        pnlPaso3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 24, 0));
        pnlPaso3.setAlignmentX(0.0F);
        pnlPaso3.setLayout(new java.awt.BorderLayout(16, 0));

        pnlCirculo3.setOpaque(false);
        pnlCirculo3.setPreferredSize(new java.awt.Dimension(32, 32));
        pnlCirculo3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        pnlPaso3.add(pnlCirculo3, java.awt.BorderLayout.WEST);

        pnlPasoCampos3.setOpaque(false);
        pnlPasoCampos3.setLayout(new javax.swing.BoxLayout(pnlPasoCampos3, javax.swing.BoxLayout.PAGE_AXIS));

        lblPaso3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblPaso3.setForeground(new java.awt.Color(24, 34, 53));
        lblPaso3.setText("CANTIDAD DE BOLETOS");
        lblPaso3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlPasoCampos3.add(lblPaso3);

        lblAyuda3.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblAyuda3.setForeground(new java.awt.Color(88, 101, 121));
        lblAyuda3.setText("Un asiento por boleto.");
        lblAyuda3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlPasoCampos3.add(lblAyuda3);

        spnCantidad.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        spnCantidad.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spnCantidad.setMaximumSize(new java.awt.Dimension(140, 40));
        spnCantidad.setPreferredSize(new java.awt.Dimension(140, 40));
        spnCantidad.setAlignmentX(0.0F);
        pnlPasoCampos3.add(spnCantidad);

        pnlPaso3.add(pnlPasoCampos3, java.awt.BorderLayout.CENTER);

        pnlPasos.add(pnlPaso3);

        lblEstado.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(88, 101, 121));
        lblEstado.setText("Elige una película y un horario para ver el resumen.");
        pnlPasos.add(lblEstado);

        pnlSeleccion.add(pnlPasos, java.awt.BorderLayout.NORTH);

        pnlCuerpo.add(pnlSeleccion);

        pnlResumen.setBackground(new java.awt.Color(255, 255, 255));
        pnlResumen.setLayout(new java.awt.BorderLayout());

        pnlCabeceraResumen.setBackground(new java.awt.Color(24, 34, 53));
        pnlCabeceraResumen.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 20, 24));
        pnlCabeceraResumen.setLayout(new javax.swing.BoxLayout(pnlCabeceraResumen, javax.swing.BoxLayout.PAGE_AXIS));

        lblResumenTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblResumenTitulo.setForeground(new java.awt.Color(255, 165, 180));
        lblResumenTitulo.setText("RESUMEN DE COMPRA");
        lblResumenTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pnlCabeceraResumen.add(lblResumenTitulo);

        lblPelicula.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblPelicula.setForeground(new java.awt.Color(255, 255, 255));
        lblPelicula.setText("Sin película seleccionada");
        pnlCabeceraResumen.add(lblPelicula);

        pnlResumen.add(pnlCabeceraResumen, java.awt.BorderLayout.NORTH);

        pnlCuerpoResumen.setOpaque(false);
        pnlCuerpoResumen.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        pnlCuerpoResumen.setLayout(new java.awt.BorderLayout());

        pnlDetalleResumen.setOpaque(false);
        pnlDetalleResumen.setLayout(new javax.swing.BoxLayout(pnlDetalleResumen, javax.swing.BoxLayout.PAGE_AXIS));

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

        lblFecha.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
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

        lblHora.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
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

        lblSala.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblSala.setForeground(new java.awt.Color(24, 34, 53));
        lblSala.setText("—");
        pnlSala.add(lblSala);

        pnlDatos.add(pnlSala);

        pnlBoletos.setOpaque(false);
        pnlBoletos.setLayout(new javax.swing.BoxLayout(pnlBoletos, javax.swing.BoxLayout.PAGE_AXIS));

        lblBoletosTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblBoletosTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblBoletosTitulo.setText("BOLETOS");
        lblBoletosTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
        pnlBoletos.add(lblBoletosTitulo);

        lblBoletos.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblBoletos.setForeground(new java.awt.Color(24, 34, 53));
        lblBoletos.setText("—");
        pnlBoletos.add(lblBoletos);

        pnlDatos.add(pnlBoletos);

        pnlDetalleResumen.add(pnlDatos);

        sepResumen.setOpaque(false);
        sepResumen.setMaximumSize(new java.awt.Dimension(32767, 2));
        sepResumen.setPreferredSize(new java.awt.Dimension(10, 2));
        sepResumen.setAlignmentX(0.0F);
        sepResumen.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        pnlDetalleResumen.add(sepResumen);

        pnlPrecio.setOpaque(false);
        pnlPrecio.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 16, 0));
        pnlPrecio.setAlignmentX(0.0F);
        pnlPrecio.setLayout(new java.awt.BorderLayout());

        lblPrecioTitulo.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblPrecioTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblPrecioTitulo.setText("Precio por boleto");
        pnlPrecio.add(lblPrecioTitulo, java.awt.BorderLayout.WEST);

        lblPrecio.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblPrecio.setForeground(new java.awt.Color(24, 34, 53));
        lblPrecio.setText("—");
        pnlPrecio.add(lblPrecio, java.awt.BorderLayout.EAST);

        pnlDetalleResumen.add(pnlPrecio);

        pnlTotal.setOpaque(false);
        pnlTotal.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 24, 0));
        pnlTotal.setAlignmentX(0.0F);
        pnlTotal.setLayout(new java.awt.BorderLayout());

        lblTotalTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblTotalTitulo.setForeground(new java.awt.Color(88, 101, 121));
        lblTotalTitulo.setText("TOTAL A PAGAR");
        pnlTotal.add(lblTotalTitulo, java.awt.BorderLayout.WEST);

        lblTotal.setFont(new java.awt.Font("SansSerif", 1, 36)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(180, 35, 60));
        lblTotal.setText("$0.00");
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlTotal.add(lblTotal, java.awt.BorderLayout.EAST);

        pnlDetalleResumen.add(pnlTotal);

        btnContinuar.setText("Continuar a asientos  →");
        btnContinuar.setMaximumSize(new java.awt.Dimension(32767, 48));
        btnContinuar.setPreferredSize(new java.awt.Dimension(200, 48));
        pnlDetalleResumen.add(btnContinuar);

        lblNota.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblNota.setForeground(new java.awt.Color(88, 101, 121));
        lblNota.setText("El pago se confirma después de elegir los asientos.");
        lblNota.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNota.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        lblNota.setMaximumSize(new java.awt.Dimension(32767, 26));
        pnlDetalleResumen.add(lblNota);

        pnlCuerpoResumen.add(pnlDetalleResumen, java.awt.BorderLayout.NORTH);

        pnlResumen.add(pnlCuerpoResumen, java.awt.BorderLayout.CENTER);

        pnlCuerpo.add(pnlResumen);

        pnlPagina.add(pnlCuerpo, java.awt.BorderLayout.CENTER);

        scrPagina.setViewportView(pnlPagina);

        add(scrPagina, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    public javax.swing.JComboBox<String> getCbPelicula() {
        return cbPelicula;
    }

    public javax.swing.JComboBox<String> getCbFuncion() {
        return cbFuncion;
    }

    public int getCantidadBoletos() {
        try { spnCantidad.commitEdit(); }
        catch (java.text.ParseException e) { throw new IllegalArgumentException("Ingresa una cantidad válida de boletos."); }
        return (Integer) spnCantidad.getValue();
    }

    public void habilitarContinuar(boolean habilitado) { btnContinuar.setEnabled(habilitado); }

    public void setTotalPagar(String total) {
        lblTotal.setText(total);
    }

    /** Actualiza la tarjeta de resumen; con {@code pelicula == null} muestra el estado vacío. */
    public void mostrarResumen(String pelicula, String fecha, String hora, String sala, int cantidad, String precio) {
        boolean vacio = pelicula == null;
        lblPelicula.setText(vacio ? "Sin película seleccionada" : pelicula);
        lblFecha.setText(vacio || fecha == null ? "—" : fecha);
        lblHora.setText(vacio || hora == null ? "—" : hora);
        lblSala.setText(vacio || sala == null ? "—" : sala);
        lblBoletos.setText(cantidad + (cantidad == 1 ? " boleto" : " boletos"));
        lblPrecio.setText(precio == null ? "Sin configurar" : cantidad + " × " + precio);
        if (precio == null) estado("El precio del boleto no está configurado. Avisa al administrador.", Tema.ERROR);
        else if (vacio || fecha == null) estado("No hay funciones disponibles para vender en este momento.", Tema.AVISO);
        else estado("Todo listo. Continúa para elegir " + (cantidad == 1 ? "el asiento." : "los " + cantidad + " asientos."), Tema.EXITO);
    }

    private void estado(String texto, Color color) {
        lblEstado.setText(texto);
        lblEstado.setForeground(color);
        lblEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, color),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
    }

    public void addContinuarListener(java.awt.event.ActionListener listener) {
        btnContinuar.addActionListener(listener);
    }

    public void addPeliculaChangeListener(java.awt.event.ActionListener listener) {
        cbPelicula.addActionListener(listener);
    }

    public void addFuncionChangeListener(java.awt.event.ActionListener listener) {
        cbFuncion.addActionListener(listener);
    }

    public void addCantidadChangeListener(javax.swing.event.ChangeListener listener) {
        spnCantidad.addChangeListener(listener);
    }

    /** Número del paso dentro de un círculo granate. */
    private static class Circulo extends JPanel {
        private final String numero;
        Circulo(String numero) {
            this.numero = numero;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            dibujo.setColor(Tema.PRIMARIO);
            dibujo.fillOval(0, 0, 31, 31);
            dibujo.setColor(Color.WHITE);
            dibujo.setFont(Tema.CUERPO.deriveFont(Font.BOLD));
            FontMetrics m = dibujo.getFontMetrics();
            dibujo.drawString(numero, (32 - m.stringWidth(numero)) / 2, (32 - m.getHeight()) / 2 + m.getAscent());
            dibujo.dispose();
        }
    }

    /** Línea punteada, como la de un boleto impreso. */
    private static class Separador extends JPanel {
        Separador() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D dibujo = (Graphics2D) g.create();
            dibujo.setColor(Tema.BORDE);
            dibujo.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{6, 5}, 0));
            dibujo.drawLine(0, 1, getWidth(), 1);
            dibujo.dispose();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContinuar;
    private javax.swing.JComboBox<String> cbFuncion;
    private javax.swing.JComboBox<String> cbPelicula;
    private javax.swing.JLabel lblAyuda1;
    private javax.swing.JLabel lblAyuda2;
    private javax.swing.JLabel lblAyuda3;
    private javax.swing.JLabel lblBoletos;
    private javax.swing.JLabel lblBoletosTitulo;
    private javax.swing.JLabel lblDatosVenta;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFechaTitulo;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblHoraTitulo;
    private javax.swing.JLabel lblNota;
    private javax.swing.JLabel lblPaso1;
    private javax.swing.JLabel lblPaso2;
    private javax.swing.JLabel lblPaso3;
    private javax.swing.JLabel lblPelicula;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JLabel lblPrecioTitulo;
    private javax.swing.JLabel lblResumenTitulo;
    private javax.swing.JLabel lblSala;
    private javax.swing.JLabel lblSalaTitulo;
    private javax.swing.JLabel lblSeccion;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalTitulo;
    private javax.swing.JPanel pnlBoletos;
    private javax.swing.JPanel pnlCabeceraResumen;
    private javax.swing.JPanel pnlCirculo1;
    private javax.swing.JPanel pnlCirculo2;
    private javax.swing.JPanel pnlCirculo3;
    private javax.swing.JPanel pnlCuerpo;
    private javax.swing.JPanel pnlCuerpoResumen;
    private javax.swing.JPanel pnlDatos;
    private javax.swing.JPanel pnlDetalleResumen;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlFecha;
    private javax.swing.JPanel pnlHora;
    private javax.swing.JPanel pnlPagina;
    private javax.swing.JPanel pnlPaso1;
    private javax.swing.JPanel pnlPaso2;
    private javax.swing.JPanel pnlPaso3;
    private javax.swing.JPanel pnlPasoCampos1;
    private javax.swing.JPanel pnlPasoCampos2;
    private javax.swing.JPanel pnlPasoCampos3;
    private javax.swing.JPanel pnlPasos;
    private javax.swing.JPanel pnlPrecio;
    private javax.swing.JPanel pnlResumen;
    private javax.swing.JPanel pnlSala;
    private javax.swing.JPanel pnlSeleccion;
    private javax.swing.JPanel pnlTotal;
    private javax.swing.JScrollPane scrPagina;
    private javax.swing.JPanel sepResumen;
    private javax.swing.JSpinner spnCantidad;
    // End of variables declaration//GEN-END:variables
}
