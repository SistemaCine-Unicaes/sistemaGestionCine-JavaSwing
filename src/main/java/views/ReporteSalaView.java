package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import views.estilos.Tema;

public class ReporteSalaView extends JPanel {

    private JComboBox<String> cmbSalas;
    private JButton btnFiltrar;
    private JButton btnExportarPDF;
    private JTable tblReporte;
    private JLabel lblTotalGeneral;

    public static final String[] COLUMNAS = {
        "Sala", "Función", "Película", "Fecha / Hora", "Estado", "Boletos", "Recaudación"
    };

    public ReporteSalaView() {
        // Configuración principal del panel
        setLayout(new BorderLayout(24, 20));
        setBackground(Tema.FONDO);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // 1. CABECERA: Título y Filtros
        JPanel pnlCabecera = new JPanel(new BorderLayout(16, 16));
        pnlCabecera.setOpaque(false);

        JLabel lblTitulo = Tema.texto("Reporte de Ocupación y Funciones por Sala", Tema.TITULO, Tema.TEXTO);
        pnlCabecera.add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlFiltros.setOpaque(false);

        pnlFiltros.add(Tema.texto("Seleccionar Sala:", Tema.ETIQUETA, Tema.TEXTO));

        cmbSalas = new JComboBox<>();
        cmbSalas.addItem("Todas las salas");
        cmbSalas.setFont(Tema.CUERPO);
        pnlFiltros.add(cmbSalas);

        btnFiltrar = Tema.botonSecundario("Filtrar");
        pnlFiltros.add(btnFiltrar);

        pnlCabecera.add(pnlFiltros, BorderLayout.CENTER);
        add(pnlCabecera, BorderLayout.NORTH);

        // 2. CENTRO: Tabla de Resultados
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

        tblReporte.getColumnModel().getColumn(1).setCellRenderer(centro); // Función #
        tblReporte.getColumnModel().getColumn(3).setCellRenderer(centro); // Fecha/hora
        tblReporte.getColumnModel().getColumn(4).setCellRenderer(centro); // Estado
        tblReporte.getColumnModel().getColumn(5).setCellRenderer(derecha); // Boletos
        tblReporte.getColumnModel().getColumn(6).setCellRenderer(derecha); // Recaudación

        tblReporte.getColumnModel().getColumn(0).setPreferredWidth(160); // Sala
        tblReporte.getColumnModel().getColumn(1).setPreferredWidth(80);  // Función
        tblReporte.getColumnModel().getColumn(2).setPreferredWidth(200); // Película
        tblReporte.getColumnModel().getColumn(3).setPreferredWidth(140); // Fecha / Hora
        tblReporte.getColumnModel().getColumn(4).setPreferredWidth(100); // Estado
        tblReporte.getColumnModel().getColumn(5).setPreferredWidth(80);  // Boletos
        tblReporte.getColumnModel().getColumn(6).setPreferredWidth(110); // Recaudación

        JScrollPane scrollTabla = Tema.tabla(tblReporte);
        add(scrollTabla, BorderLayout.CENTER);

        // 3. PIE: Totales y Botón Exportar PDF
        JPanel pnlPie = new JPanel(new BorderLayout(16, 0));
        pnlPie.setOpaque(false);

        lblTotalGeneral = Tema.texto("Boletos vendidos: 0  |  Recaudación total: $0.00", Tema.CUERPO.deriveFont(Font.BOLD), Tema.TEXTO);
        pnlPie.add(lblTotalGeneral, BorderLayout.WEST);

        btnExportarPDF = Tema.botonPrimario("Exportar a PDF");
        pnlPie.add(btnExportarPDF, BorderLayout.EAST);

        add(pnlPie, BorderLayout.SOUTH);
    }

    public JButton getBtnExportarPDF() { return btnExportarPDF; }
    public JButton getBtnFiltrar() { return btnFiltrar; }
    public JComboBox<String> getCmbSalas() { return cmbSalas; }
    public JTable getTblReporte() { return tblReporte; }
    public JLabel getLblTotalGeneral() { return lblTotalGeneral; }
}
