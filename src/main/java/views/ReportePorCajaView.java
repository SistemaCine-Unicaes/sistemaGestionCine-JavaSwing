package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import views.estilos.Tema;

/**
 * Vista de administración para el reporte de ventas y uso por caja física.
 * Permite filtrar por caja y rango de fechas (desde / hasta), mostrando los operadores
 * que la utilizaron y las ventas recaudadas.
 */
public class ReportePorCajaView extends JPanel {

    private JComboBox<String> cmbCajas;
    private JComboBox<String> cmbPeriodo;
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JButton btnFiltrar;
    private JButton btnExportarPDF;
    private JTable tblReporte;
    private JLabel lblResumenTotales;

    public static final String[] COLUMNAS = {
        "Caja", "Cajero / Operador", "Rol", "Tickets", "Recaudación", "Primera Venta", "Última Venta"
    };

    public ReportePorCajaView() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // 1. CABECERA
        JPanel pnlCabecera = new JPanel();
        pnlCabecera.setLayout(new BoxLayout(pnlCabecera, BoxLayout.PAGE_AXIS));
        pnlCabecera.setOpaque(false);

        JLabel lblTitulo = Tema.texto("Reporte de Ventas y Uso por Caja", Tema.TITULO, Tema.TEXTO);
        JLabel lblSub = Tema.texto("Auditoría de cajeros, tickets emitidos e ingresos por cada punto de venta físico.",
                Tema.CUERPO, Tema.SECUNDARIO);

        pnlCabecera.add(lblTitulo);
        pnlCabecera.add(Box.createVerticalStrut(4));
        pnlCabecera.add(lblSub);
        pnlCabecera.add(Box.createVerticalStrut(16));

        // Panel de Filtros (Solo Selectores ComboBox)
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        pnlFiltros.setOpaque(false);

        pnlFiltros.add(Tema.texto("Caja:", Tema.ETIQUETA, Tema.TEXTO));
        cmbCajas = new JComboBox<>();
        cmbCajas.setFont(Tema.CUERPO);
        cmbCajas.addItem("Todas las cajas");
        pnlFiltros.add(cmbCajas);

        pnlFiltros.add(Box.createHorizontalStrut(12));
        pnlFiltros.add(Tema.texto("Período:", Tema.ETIQUETA, Tema.TEXTO));
        cmbPeriodo = new JComboBox<>(new String[]{
            "Últimos 30 días", "Hoy", "Este mes", "Último mes", "Próximos 30 días", "Todo el historial"
        });
        cmbPeriodo.setFont(Tema.CUERPO);
        pnlFiltros.add(cmbPeriodo);

        // Componentes inicializados para compatibilidad de getters
        txtFechaDesde = Tema.campo("", 10);
        txtFechaHasta = Tema.campo("", 10);
        btnFiltrar = Tema.botonSecundario("Filtrar");

        pnlCabecera.add(pnlFiltros);
        add(pnlCabecera, BorderLayout.NORTH);

        // 2. TABLA CENTRAL
        DefaultTableModel modelo = new DefaultTableModel(null, COLUMNAS) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblReporte = new JTable(modelo);

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(JLabel.CENTER);

        tblReporte.getColumnModel().getColumn(2).setCellRenderer(centro); // Rol
        tblReporte.getColumnModel().getColumn(3).setCellRenderer(derecha); // Tickets
        tblReporte.getColumnModel().getColumn(4).setCellRenderer(derecha); // Recaudación
        tblReporte.getColumnModel().getColumn(5).setCellRenderer(centro);  // 1a venta
        tblReporte.getColumnModel().getColumn(6).setCellRenderer(centro);  // Ultima venta

        tblReporte.getColumnModel().getColumn(0).setPreferredWidth(170);
        tblReporte.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblReporte.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblReporte.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblReporte.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblReporte.getColumnModel().getColumn(5).setPreferredWidth(130);
        tblReporte.getColumnModel().getColumn(6).setPreferredWidth(130);

        JScrollPane scrollTabla = Tema.tabla(tblReporte);
        add(scrollTabla, BorderLayout.CENTER);

        // 3. PIE DE PÁGINA
        JPanel pnlPie = new JPanel(new BorderLayout(16, 0));
        pnlPie.setOpaque(false);

        lblResumenTotales = Tema.texto("Cargando información...", Tema.CUERPO.deriveFont(Font.BOLD), Tema.TEXTO);
        pnlPie.add(lblResumenTotales, BorderLayout.WEST);

        btnExportarPDF = Tema.botonPrimario("Exportar a PDF");
        pnlPie.add(btnExportarPDF, BorderLayout.EAST);

        add(pnlPie, BorderLayout.SOUTH);
    }

    public JComboBox<String> getCmbCajas() { return cmbCajas; }
    public JComboBox<String> getCmbPeriodo() { return cmbPeriodo; }
    public JTextField getTxtFechaDesde() { return txtFechaDesde; }
    public JTextField getTxtFechaHasta() { return txtFechaHasta; }
    public JButton getBtnFiltrar() { return btnFiltrar; }
    public JButton getBtnExportarPDF() { return btnExportarPDF; }
    public JTable getTblReporte() { return tblReporte; }
    public JLabel getLblResumenTotales() { return lblResumenTotales; }

    public void setCajaSeleccionada(String nombreCaja) {
        if (nombreCaja == null) return;
        for (int i = 0; i < cmbCajas.getItemCount(); i++) {
            String item = cmbCajas.getItemAt(i);
            if (item.equalsIgnoreCase(nombreCaja) || item.contains(nombreCaja)) {
                cmbCajas.setSelectedIndex(i);
                break;
            }
        }
    }
}
