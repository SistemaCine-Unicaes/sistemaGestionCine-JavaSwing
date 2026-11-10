package utils;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class ReporteSalaPDF {

    public void generar(String parametroSala, Connection conexion) {
        generar(parametroSala, conexion, null);
    }

    public void generar(String parametroSala, Connection conexion, File destino) {
        try {
            InputStream archivo = getClass().getResourceAsStream("/Reports/reporte_sala.jrxml");
            if (archivo == null) {
                JOptionPane.showMessageDialog(null, "No se encontró la plantilla reporte_sala.jrxml en resources/Reports/");
                return;
            }

            JasperReport reporte = JasperCompileManager.compileReport(archivo);
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("sala_filtro", parametroSala != null ? parametroSala : "Todas las salas");

            JasperPrint print = JasperFillManager.fillReport(reporte, parametros, conexion);

            if (print.getPages().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se encontraron datos para generar el reporte.",
                        "Sin datos", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            File archivoDestino = destino;
            if (archivoDestino == null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Guardar reporte de salas (PDF)");
                chooser.setSelectedFile(new File("Reporte_Salas_" + System.currentTimeMillis() + ".pdf"));
                chooser.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf"));
                int userSelection = chooser.showSaveDialog(null);
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                archivoDestino = chooser.getSelectedFile();
                if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
                    archivoDestino = new File(archivoDestino.getParentFile(), archivoDestino.getName() + ".pdf");
                }
            }

            JasperExportManager.exportReportToPdfFile(print, archivoDestino.getAbsolutePath());

            int opcion = JOptionPane.showConfirmDialog(null,
                    "Reporte exportado exitosamente en:\n" + archivoDestino.getAbsolutePath() + "\n\n¿Deseas abrir el archivo ahora?",
                    "Reporte generado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (opcion == JOptionPane.YES_OPTION) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(archivoDestino);
                } else {
                    JasperViewer.viewReport(print, false);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
