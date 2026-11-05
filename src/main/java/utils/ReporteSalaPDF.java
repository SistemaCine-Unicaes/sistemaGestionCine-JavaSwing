package utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class ReporteSalaPDF {

    public void generar(String parametroSala, Connection conexion) {
        try {
            // Lee el archivo .jrxml de tu carpeta de recursos
            java.io.InputStream archivo = getClass().getResourceAsStream("/Reports/reporte_sala.jrxml");
            if (archivo == null) {
                JOptionPane.showMessageDialog(null, "No se encontró la plantilla reporte_sala.jrxml en resources/Reports/");
                return;
            }

            // Compila y llena el reporte
            JasperReport reporte = JasperCompileManager.compileReport(archivo);
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("sala_filtro", parametroSala); // Parámetro enviado a JasperReports

            JasperPrint print = JasperFillManager.fillReport(reporte, parametros, conexion);

            // Exporta a la raíz del proyecto y lo muestra en pantalla
            JasperExportManager.exportReportToPdfFile(print, "Reporte_Ocupacion_Salas.pdf");
            JasperViewer.viewReport(print, false);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}