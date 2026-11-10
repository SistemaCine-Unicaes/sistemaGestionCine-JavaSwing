package utils;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import dao.ReportePeliculaDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import models.Pelicula;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportePeliculaPDFTest {

    @TempDir
    Path tempDir;

    @Test
    void generarReportePeliculaTieneEstiloInstitucional() throws Exception {
        Pelicula peli = new Pelicula(1, "Inception", "Dom Cobb es un ladrón capaz de adentrarse en los sueños.",
                148, "Ciencia Ficción", "Christopher Nolan", Date.valueOf("2010-07-16"), "Normal", null, "Cartelera");

        ReportePeliculaDAO.Funcion func = new ReportePeliculaDAO.Funcion(10, Date.valueOf(LocalDate.now()),
                Time.valueOf("18:00:00"), Time.valueOf("20:30:00"), "PROGRAMADA", 1, 2, new BigDecimal("10.00"));

        ReportePeliculaDAO.Ticket t1 = new ReportePeliculaDAO.Ticket(101, 10, new Timestamp(System.currentTimeMillis()),
                Date.valueOf(LocalDate.now()), Time.valueOf("18:00:00"), 1, "A-1", "Normal", 1, "Admin", new BigDecimal("5.00"));
        ReportePeliculaDAO.Ticket t2 = new ReportePeliculaDAO.Ticket(102, 10, new Timestamp(System.currentTimeMillis()),
                Date.valueOf(LocalDate.now()), Time.valueOf("18:00:00"), 1, "A-2", "Normal", 1, "Admin", new BigDecimal("5.00"));

        ReportePeliculaDAO.Reporte reporte = new ReportePeliculaDAO.Reporte(peli, List.of(func), List.of(t1, t2), 2, new BigDecimal("10.00"));

        Path pdfFile = tempDir.resolve("reporte_pelicula.pdf");
        ReportePeliculaPDF.generar(reporte, pdfFile);

        assertTrue(Files.exists(pdfFile));
        assertTrue(Files.size(pdfFile) > 0);

        PdfReader reader = new PdfReader(Files.readAllBytes(pdfFile));
        assertTrue(reader.getNumberOfPages() >= 1);

        String texto = texto(reader, 1);
        assertTrue(texto.contains("CINE | GESTIÓN"));
        assertTrue(texto.contains("REPORTE DE PELÍCULA"));
        assertTrue(texto.contains("ADMINISTRACIÓN"));
        assertTrue(texto.contains("Inception"));
        assertTrue(texto.contains("Christopher Nolan"));
        assertTrue(texto.contains("RESUMEN DE TAQUILLA"));
    }

    private static String texto(PdfReader lector, int pagina) throws IOException {
        return new PdfTextExtractor(lector).getTextFromPage(pagina);
    }
}
