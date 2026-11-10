package utils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dao.ReporteCajaDetalladoDAO;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Reporte PDF de ventas y uso por caja física.
 * Utiliza el estilo institucional unificado del sistema (EstilosReportePDF).
 */
public final class ReportePorCajaPDF {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA_MIN = DateTimeFormatter.ofPattern("HH:mm");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(Locale.US);

    private ReportePorCajaPDF() {}

    public static void generar(ReporteCajaDetalladoDAO.ReporteCajas reporte, String filtroCaja,
                               String periodoTexto, String usuario, Path destino) throws IOException, DocumentException {
        Files.write(destino, crear(reporte, filtroCaja, periodoTexto, usuario));
    }

    public static byte[] crear(ReporteCajaDetalladoDAO.ReporteCajas reporte, String filtroCaja,
                               String periodoTexto, String usuario) throws IOException, DocumentException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 35, 35, 85, 35);
        PdfWriter writer = PdfWriter.getInstance(documento, salida);

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of(
                config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador")));
        String fechaGenerado = "Generado: " + FECHA_HORA.format(ahora);

        writer.setPageEvent(new EstilosReportePDF.EventoPaginaReporte(
                "REPORTE DE VENTAS Y USO POR CAJA", "ADMINISTRACIÓN", fechaGenerado, true));

        documento.open();
        escribir(documento, reporte, filtroCaja, periodoTexto, usuario);
        documento.close();
        return salida.toByteArray();
    }

    private static void escribir(Document doc, ReporteCajaDetalladoDAO.ReporteCajas reporte,
                                 String filtroCaja, String periodoTexto, String usuario) throws DocumentException {
        // 1. Título y metadatos de sección
        doc.add(EstilosReportePDF.tituloSeccion("Auditoría de recaudación por caja física"));

        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setSpacingAfter(6);

        String desc = "Caja: " + (filtroCaja != null ? filtroCaja : "Todas las cajas")
                + " · Generado por: " + (usuario != null ? usuario : "Sistema");
        PdfPCell celdaIzq = new PdfPCell(new Phrase(desc, EstilosReportePDF.fuente(9, Font.NORMAL, EstilosReportePDF.GRIS)));
        celdaIzq.setBorder(Rectangle.NO_BORDER);
        celdaIzq.setPadding(0);

        PdfPCell celdaDer = new PdfPCell(new Phrase("Período: " + (periodoTexto != null ? periodoTexto : "General"),
                EstilosReportePDF.fuente(9.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        celdaDer.setBorder(Rectangle.NO_BORDER);
        celdaDer.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaDer.setPadding(0);

        info.addCell(celdaIzq);
        info.addCell(celdaDer);
        doc.add(info);

        // 2. Tarjetas KPI
        var kpis = List.of(
                new EstilosReportePDF.TarjetaKPI("TOTAL RECAUDADO", dinero(reporte.totalRecaudado())),
                new EstilosReportePDF.TarjetaKPI("TICKETS VENDIDOS", String.valueOf(reporte.totalTickets())),
                new EstilosReportePDF.TarjetaKPI("CAJAS OPERADAS", String.valueOf(reporte.cajasOperadas())),
                new EstilosReportePDF.TarjetaKPI("USUARIOS / OPERADORES", String.valueOf(reporte.usuariosDistintos()))
        );
        doc.add(EstilosReportePDF.crearTarjetasKPI(kpis));

        // 3. Tabla de detalle de cajas y cajeros
        Paragraph sub = EstilosReportePDF.tituloSeccion("Desglose de uso y transacciones por caja");
        sub.setSpacingBefore(8);
        doc.add(sub);

        float[] anchos = new float[]{2.2f, 2.2f, 1.2f, 1.0f, 1.4f, 1.1f, 1.1f};
        String[] cabeceras = new String[]{"CAJA", "OPERADOR / CAJERO", "ROL", "TICKETS", "RECAUDADO", "1ª VENTA", "ÚLT. VENTA"};
        int[] alineaciones = new int[]{Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_CENTER, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_CENTER, Element.ALIGN_CENTER};

        PdfPTable tabla = EstilosReportePDF.crearTabla(anchos, cabeceras, alineaciones);

        Font fuenteNormal = EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL);
        Font fuenteNegrita = EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL);
        Font fuenteGris = EstilosReportePDF.fuente(8.0f, Font.NORMAL, EstilosReportePDF.GRIS);

        List<ReporteCajaDetalladoDAO.UsoCaja> filas = reporte.filas();
        if (filas == null || filas.isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("No se encontraron transacciones registradas para los filtros indicados.",
                    EstilosReportePDF.fuente(9, Font.ITALIC, EstilosReportePDF.GRIS)));
            vacia.setColspan(7);
            vacia.setPadding(14);
            vacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            vacia.setBorderColor(EstilosReportePDF.BORDE);
            tabla.addCell(vacia);
        } else {
            for (int i = 0; i < filas.size(); i++) {
                ReporteCajaDetalladoDAO.UsoCaja fila = filas.get(i);
                java.awt.Color fondo = (i % 2 == 1) ? EstilosReportePDF.FILA_PAR : java.awt.Color.WHITE;

                tabla.addCell(EstilosReportePDF.celdaDato(fila.nombreCaja(), Element.ALIGN_LEFT, fondo, fuenteNormal));
                tabla.addCell(EstilosReportePDF.celdaDato(fila.nombreUsuario(), Element.ALIGN_LEFT, fondo, fuenteNormal));
                tabla.addCell(EstilosReportePDF.celdaDato(fila.rol(), Element.ALIGN_CENTER, fondo, fuenteGris));
                tabla.addCell(EstilosReportePDF.celdaDato(String.valueOf(fila.ticketsVendidos()), Element.ALIGN_RIGHT, fondo, fuenteNormal));
                tabla.addCell(EstilosReportePDF.celdaDato(dinero(fila.totalRecaudado()), Element.ALIGN_RIGHT, fondo, fuenteNegrita));
                tabla.addCell(EstilosReportePDF.celdaDato(formatoHora(fila.primeraVenta()), Element.ALIGN_CENTER, fondo, fuenteGris));
                tabla.addCell(EstilosReportePDF.celdaDato(formatoHora(fila.ultimaVenta()), Element.ALIGN_CENTER, fondo, fuenteGris));
            }
        }
        doc.add(tabla);

        // 4. Tarjeta verde de totales finales
        doc.add(EstilosReportePDF.cajaResumen(
                "TOTAL GENERAL CONSOLIDADO",
                "Tickets vendidos: " + reporte.totalTickets() + "  |  Cajas con ventas: " + reporte.cajasOperadas(),
                dinero(reporte.totalRecaudado())
        ));
    }

    private static String dinero(BigDecimal monto) {
        return monto == null ? "$0.00" : MONEDA.format(monto);
    }

    private static String formatoHora(Timestamp ts) {
        if (ts == null) return "-";
        return FECHA_HORA.format(ts.toLocalDateTime());
    }

    public static void exportarInteractivo(Component padre, ReporteCajaDetalladoDAO.ReporteCajas reporte,
                                          String filtroCaja, String periodoTexto, String usuario) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Reporte por Caja (PDF)");
        chooser.setSelectedFile(new File("Reporte_Por_Caja_" + System.currentTimeMillis() + ".pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)", "pdf"));

        int res = chooser.showSaveDialog(padre);
        if (res != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = chooser.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".pdf")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".pdf");
        }

        try {
            generar(reporte, filtroCaja, periodoTexto, usuario, destino.toPath());
            int abrir = JOptionPane.showConfirmDialog(padre,
                    "Reporte exportado exitosamente:\n" + destino.getAbsolutePath() + "\n\n¿Deseas abrir el archivo ahora?",
                    "Reporte generado", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (abrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(destino);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(padre, "Error al generar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
