package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import views.estilos.Tema;

public class ReporteSalaView extends JPanel {

    private JComboBox<String> cmbSalas;
    private JButton btnFiltrar;
    private JButton btnExportarPDF;
    private JTable tblReporte;

    public ReporteSalaView() {
        // Configuración principal del panel
        setLayout(new BorderLayout(24, 24));
        setBackground(Tema.FONDO);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 32, 24, 32)); // Márgenes de la guía

        // 1. CABECERA: Título y Filtros
        JPanel pnlCabecera = new JPanel(new BorderLayout(16, 16));
        pnlCabecera.setOpaque(false);
        
        // Usamos Tema.texto para crear el título ya estilizado
        JLabel lblTitulo = Tema.texto("Reporte de Ocupación por Sala", Tema.TITULO, Tema.TEXTO);
        pnlCabecera.add(lblTitulo, BorderLayout.NORTH);

        // Panel para agrupar el combobox y el botón de filtrar
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        pnlFiltros.setOpaque(false);
        
        pnlFiltros.add(Tema.texto("Seleccionar Sala:", Tema.ETIQUETA, Tema.TEXTO));
        
        cmbSalas = new JComboBox<>(new String[]{"Todas las salas", "Sala 01", "Sala 02"});
        cmbSalas.setFont(Tema.CUERPO);
        pnlFiltros.add(cmbSalas);

        // Usamos la fábrica de Tema para el botón secundario
        btnFiltrar = Tema.botonSecundario("Filtrar");
        pnlFiltros.add(btnFiltrar);

        pnlCabecera.add(pnlFiltros, BorderLayout.CENTER);
        add(pnlCabecera, BorderLayout.NORTH);

        // 2. CENTRO: Tabla de Resultados
        String[] columnas = {"Sala", "Boletos Vendidos", "Asientos Ocupados", "Recaudación"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);
        tblReporte = new JTable(modelo);
        
        // Tema.tabla() le aplica la cabecera oscura, fila de 44px y devuelve el ScrollPane
        JScrollPane scrollTabla = Tema.tabla(tblReporte);
        add(scrollTabla, BorderLayout.CENTER);

        // 3. PIE: Botón Exportar PDF
        JPanel pnlPie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        pnlPie.setOpaque(false);
        
        // Usamos la fábrica de Tema para el botón principal (Granate)
        btnExportarPDF = Tema.botonPrimario("Exportar a PDF");
        pnlPie.add(btnExportarPDF);
        
        add(pnlPie, BorderLayout.SOUTH);
    }
    
    // Getters para que tu controlador (ReporteSalaController) pueda acceder
    public JButton getBtnExportarPDF() { return btnExportarPDF; }
    public JButton getBtnFiltrar() { return btnFiltrar; }
    public JComboBox<String> getCmbSalas() { return cmbSalas; }
    public JTable getTblReporte() { return tblReporte; }
}