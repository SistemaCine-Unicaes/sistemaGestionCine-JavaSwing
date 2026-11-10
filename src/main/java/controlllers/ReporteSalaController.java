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
        cargarComboSalas();
        initEventos();
        cargarDatosTabla("Todas las salas");
    }

    private void cargarComboSalas() {
        try {
            List<String> salas = dao.obtenerNombresSalas();
            vista.getCmbSalas().removeAllItems();
            vista.getCmbSalas().addItem("Todas las salas");
            for (String s : salas) {
                vista.getCmbSalas().addItem(s);
            }
        } catch (Exception ignored) {}
    }

    private void initEventos() {
        // Evento para el botón Filtrar
        vista.getBtnFiltrar().addActionListener((ActionEvent e) -> {
            Object seleccionado = vista.getCmbSalas().getSelectedItem();
            String salaSeleccionada = seleccionado != null ? seleccionado.toString() : "Todas las salas";
            cargarDatosTabla(salaSeleccionada);
        });

        // Evento para exportar a PDF
        vista.getBtnExportarPDF().addActionListener((ActionEvent e) -> {
            Object seleccionado = vista.getCmbSalas().getSelectedItem();
            String salaSeleccionada = seleccionado != null ? seleccionado.toString() : "Todas las salas";
            try (java.sql.Connection con = Conexion.getConexion()) {
                new ReporteSalaPDF().generar(salaSeleccionada, con);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vista, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void cargarDatosTabla(String filtro) {
        try {
            List<Object[]> datos = dao.obtenerOcupacion(filtro);
            DefaultTableModel modelo = (DefaultTableModel) vista.getTblReporte().getModel();
            modelo.setRowCount(0); // Limpia la tabla actual
            
            long totalBoletos = 0;
            double totalRecaudacion = 0.0;

            for (Object[] fila : datos) {
                modelo.addRow(fila);
                if (fila.length >= 7) {
                    if (fila[5] instanceof Number n) {
                        totalBoletos += n.longValue();
                    }
                    if (fila[6] != null) {
                        String str = fila[6].toString().replace("$", "").replace(",", "").trim();
                        try {
                            totalRecaudacion += Double.parseDouble(str);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            if (vista.getLblTotalGeneral() != null) {
                vista.getLblTotalGeneral().setText(String.format(
                        "Boletos vendidos: %d  |  Recaudación total: $%,.2f  |  Funciones listadas: %d",
                        totalBoletos, totalRecaudacion, datos.size()
                ));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al cargar la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
