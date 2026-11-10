package utils;

import java.io.InputStream;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReporteSalaPDFTest {

    @Test
    void compilarPlantillaReporteSala() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/Reports/reporte_sala.jrxml")) {
            assertNotNull(in, "La plantilla reporte_sala.jrxml debe existir en classpath");
            JasperReport report = JasperCompileManager.compileReport(in);
            assertNotNull(report, "El reporte debe compilar correctamente");
        }
    }
}

