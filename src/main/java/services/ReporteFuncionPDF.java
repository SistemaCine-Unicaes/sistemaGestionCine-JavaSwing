/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import config.Conexion;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 *
 * @author axele
 */
public class ReporteFuncionPDF {
    public static void generar(int idFuncion, File destino) throws Exception {

        config.Sesion.exigirAdministrador();

        String ruta = "/Reports/reporte_funcion.jrxml";

        try (InputStream plantilla =
                ReporteFuncionPDF.class
                        .getResourceAsStream(ruta)) {

            if (plantilla == null) {
                throw new IllegalStateException(
                        "No se encontró la plantilla: " + ruta
                );
            }

            JasperReport reporte =
                    JasperCompileManager.compileReport(
                            plantilla
                    );

            Map<String, Object> parametros =
                    new HashMap<>();

            parametros.put("ID_FUNCION", idFuncion);

            try (Connection conexion =
                    Conexion.getConexion()) {

                JasperPrint documento =
                        JasperFillManager.fillReport(
                                reporte,
                                parametros,
                                conexion
                        );

                if (documento.getPages().isEmpty()) {
                    throw new IllegalStateException(
                            "No hay datos para esta función."
                    );
                }

                JasperExportManager.exportReportToPdfFile(
                        documento,
                        destino.getAbsolutePath()
                );
            }
        }
    }
}
