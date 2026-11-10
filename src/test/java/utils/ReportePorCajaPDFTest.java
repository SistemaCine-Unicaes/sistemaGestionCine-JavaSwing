package utils;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import dao.ReporteCajaDetalladoDAO;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportePorCajaPDFTest {

    private static PdfReader pdf(ReporteCajaDetalladoDAO.ReporteCajas reporte, String filtroCaja, String periodo) throws Exception {
        byte[] bytes = ReportePorCajaPDF.crear(reporte, filtroCaja, periodo, "Administrador");
        assertEquals("%PDF", new String(bytes, 0, 4));
        return new PdfReader(bytes);
    }

    private static String texto(PdfReader lector, int pagina) throws Exception {
        return new PdfTextExtractor(lector).getTextFromPage(pagina);
    }

    @Test
    void generaReportePorCajaConDatos() throws Exception {
        Timestamp t1 = Timestamp.valueOf(LocalDateTime.of(2026, 11, 1, 10, 0));
        Timestamp t2 = Timestamp.valueOf(LocalDateTime.of(2026, 11, 1, 14, 30));

        var filas = List.of(
                new ReporteCajaDetalladoDAO.UsoCaja(1, "Caja 01 - Taquilla Principal", 2, "Carlos Lopez", "Cajero", 25, new BigDecimal("125.00"), t1, t2),
                new ReporteCajaDetalladoDAO.UsoCaja(2, "Caja 02 - Taquilla Central", 3, "Maria Gonzalez", "Cajero", 10, new BigDecimal("50.00"), t1, t2)
        );
        var reporte = new ReporteCajaDetalladoDAO.ReporteCajas(filas, 35, new BigDecimal("175.00"), 2, 2);

        PdfReader lector = pdf(reporte, "Todas las cajas", "Noviembre 2026");
        assertEquals(1, lector.getNumberOfPages());
        String t = texto(lector, 1);

        assertTrue(t.contains("CINE | GESTI"));
        assertTrue(t.contains("REPORTE DE VENTAS Y USO POR CAJA"));
        assertTrue(t.contains("Caja 01"));
        assertTrue(t.contains("Caja 02 - Taquilla Central"));
        assertTrue(t.contains("Carlos"));
        assertTrue(t.contains("TOTAL GENERAL CONSOLIDADO"));
        assertTrue(t.contains("35"));
    }

    @Test
    void generaReportePorCajaSinDatos() throws Exception {
        var reporte = new ReporteCajaDetalladoDAO.ReporteCajas(List.of(), 0, BigDecimal.ZERO, 0, 0);
        PdfReader lector = pdf(reporte, "Caja 01", "Hoy");
        assertEquals(1, lector.getNumberOfPages());
        String t = texto(lector, 1);

        assertTrue(t.contains("No se encontraron transacciones"));
        assertTrue(t.contains("Tickets vendidos: 0"));
    }
}
