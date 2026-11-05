package utils;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import dao.ReportesDAO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReporteCajaPDFTest {
    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1), HASTA = LocalDate.of(2026, 10, 31);

    private static PdfReader pdf(ReportesDAO.Reporte reporte) throws Exception {
        byte[] bytes = ReporteCajaPDF.crear(reporte, "Mensual", DESDE, HASTA, "Héctor");
        assertEquals("%PDF", new String(bytes, 0, 4));
        return new PdfReader(bytes);
    }

    private static String texto(PdfReader lector, int pagina) throws Exception {
        return new PdfTextExtractor(lector).getTextFromPage(pagina);
    }

    @Test void resumenOrdenaCajerosPorRecaudacionYCuadraLosTotales() throws Exception {
        var reporte = new ReportesDAO.Reporte(List.of(
                new ReportesDAO.VentaCajero(3, "Beatriz Ruiz", 1, new BigDecimal("4.50")),
                new ReportesDAO.VentaCajero(7, "Ana López", 2, new BigDecimal("9.00"))), 3, new BigDecimal("13.50"));
        PdfReader lector = pdf(reporte);
        assertEquals(1, lector.getNumberOfPages());
        String texto = texto(lector, 1);
        for (String esperado : List.of("CINE | GESTIÓN", "REPORTE DE CORTE DE CAJA", "Período: 01/10/2026 - 31/10/2026",
                "Corte mensual", "Héctor", "TICKETS VENDIDOS", "$ 13.50", "$ 4.50", "66.7 %", "33.3 %",
                "TOTAL DEL PERÍODO", "3 tickets", "2 cajeros con ventas")) {
            assertTrue(texto.contains(esperado), "Falta «" + esperado + "» en:\n" + texto);
        }
        // El total de páginas se dibuja aparte; el extractor puede pegarlo a "de".
        assertTrue(texto.matches("(?s).*Página 1 de\\s*1.*"), texto);
        assertTrue(texto.indexOf("Ana López (#7)") < texto.indexOf("Beatriz Ruiz (#3)"), "Primero el que más recaudó");
    }

    @Test void periodoSinVentasLoIndica() throws Exception {
        String texto = texto(pdf(new ReportesDAO.Reporte(List.of(), 0, BigDecimal.ZERO.setScale(2))), 1);
        assertTrue(texto.contains("No hay ventas registradas en el período."));
        assertTrue(texto.contains("0 tickets"));
        assertTrue(texto.contains("0 cajeros con ventas"));
    }

    @Test void listaLargaContinuaEnOtraPaginaConEncabezadoYTotalDePaginas() throws Exception {
        List<ReportesDAO.VentaCajero> cajeros = new ArrayList<>();
        for (int i = 1; i <= 40; i++) cajeros.add(new ReportesDAO.VentaCajero(i, "Cajero " + i, i, new BigDecimal(i * 5)));
        PdfReader lector = pdf(new ReportesDAO.Reporte(cajeros, 820, new BigDecimal("4100.00")));
        int paginas = lector.getNumberOfPages();
        assertTrue(paginas >= 2);
        String ultima = texto(lector, paginas);
        assertTrue(ultima.matches("(?s).*Página " + paginas + " de\\s*" + paginas + ".*"), ultima);
        assertTrue(ultima.contains("CAJERO") && ultima.contains("REPORTE DE CORTE DE CAJA"), "Se repiten encabezados");
        assertTrue(ultima.contains("TOTAL DEL PERÍODO"));
    }
}
