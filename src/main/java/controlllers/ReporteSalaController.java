package controlllers;

import config.Conexion;
import dao.ReporteSalaDAO;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import utils.ReporteSalaPDF;
import views.ReporteSalaView;

public class ReporteSalaController {
    
    private final ReporteSalaView vista;
    private final ReporteSalaDAO dao;

    public ReporteSalaController(ReporteSalaView vista) {
        this.vista = vista;
        this.dao = new ReporteSalaDAO();
        initEventos();
        cargarDatosTabla("Todas las salas");
    }

    private void initEventos() {
        // Evento para el botón Filtrar
        vista.getBtnFiltrar().addActionListener((ActionEvent e) -> {
            String salaSeleccionada = vista.getCmbSalas().getSelectedItem().toString();
            cargarDatosTabla(salaSeleccionada);
        });

        // Evento para exportar a PDF
        vista.getBtnExportarPDF().addActionListener((ActionEvent e) -> {
            String salaSeleccionada = vista.getCmbSalas().getSelectedItem().toString();
            try (java.sql.Connection con = Conexion.getConexion()) {
                new ReporteSalaPDF().generar(salaSeleccionada, con);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vista, "Error de conexión al exportar: " + ex.getMessage());
            }
        });
    }

    private void cargarDatosTabla(String filtro) {
        try {
            List<Object[]> datos = dao.obtenerOcupacion(filtro);
            DefaultTableModel modelo = (DefaultTableModel) vista.getTblReporte().getModel();
            modelo.setRowCount(0); // Limpia la tabla actual
            
            for (Object[] fila : datos) {
                modelo.addRow(fila);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al cargar la tabla: " + e.getMessage());
        }
    }
}