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
import dao.ReportesDAO;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Reporte PDF del corte de caja con el estilo visual compartido del sistema de cine,
 * alineado con el diseño institucional de funciones.
 */
public final class ReporteCajaPDF {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReporteCajaPDF() {}

    /** @param hasta último día incluido en el corte. */
    public static void generar(ReportesDAO.Reporte reporte, String tipo, LocalDate desde, LocalDate hasta,
            String usuario, Path destino) throws IOException, DocumentException {
        Files.write(destino, crear(reporte, tipo, desde, hasta, usuario));
    }

    static byte[] crear(ReportesDAO.Reporte reporte, String tipo, LocalDate desde, LocalDate hasta, String usuario)
            throws IOException, DocumentException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        // A4 estándar vertical (595 x 842 pt), con márgenes de 35 a los lados y espacio para encabezado
        Document documento = new Document(PageSize.A4, 35, 35, 85, 32);
        PdfWriter writer = PdfWriter.getInstance(documento, salida);
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of(
                config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador")));
        String fechaGenerado = "Generado: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(ahora);

        writer.setPageEvent(new EstilosReportePDF.EventoPaginaReporte(
                "REPORTE DE CORTE DE CAJA", "ADMINISTRACIÓN", fechaGenerado, true));

        documento.open();
        escribir(documento, reporte, tipo, desde, hasta, usuario);
        documento.close();
        return salida.toByteArray();
    }

    private static void escribir(Document documento, ReportesDAO.Reporte reporte, String tipo, LocalDate desde,
            LocalDate hasta, String usuario) throws DocumentException {

        // Título de sección y datos del período
        documento.add(EstilosReportePDF.tituloSeccion("Resumen de corte de caja"));

        PdfPTable infoPeriodo = new PdfPTable(2);
        infoPeriodo.setWidthPercentage(100);
        infoPeriodo.setSpacingAfter(4);

        PdfPCell celdaTipo = new PdfPCell(new Phrase("Corte " + tipo.toLowerCase() + " de todas las cajas. Generado por " + usuario + ".",
                EstilosReportePDF.fuente(9, Font.NORMAL, EstilosReportePDF.GRIS)));
        celdaTipo.setBorder(Rectangle.NO_BORDER);
        celdaTipo.setPadding(0);

        PdfPCell celdaRango = new PdfPCell(new Phrase("Período: " + FECHA.format(desde) + " - " + FECHA.format(hasta),
                EstilosReportePDF.fuente(9.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        celdaRango.setBorder(Rectangle.NO_BORDER);
        celdaRango.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaRango.setPadding(0);

        infoPeriodo.addCell(celdaTipo);
        infoPeriodo.addCell(celdaRango);
        documento.add(infoPeriodo);

        // Tarjetas métricas (KPIs)
        BigDecimal promedio = reporte.tickets() == 0 ? BigDecimal.ZERO
                : reporte.total().divide(BigDecimal.valueOf(reporte.tickets()), 2, RoundingMode.HALF_UP);

        var kpis = List.of(
                new EstilosReportePDF.TarjetaKPI("TICKETS VENDIDOS", String.valueOf(reporte.tickets())),
                new EstilosReportePDF.TarjetaKPI("INGRESOS TOTALES", dinero(reporte.total())),
                new EstilosReportePDF.TarjetaKPI("PROMEDIO POR TICKET", dinero(promedio))
        );
        documento.add(EstilosReportePDF.crearTarjetasKPI(kpis));

        // Sección: Ventas por cajero
        int cajeros = reporte.cajeros().size();
        String subtituloCajeros = cajeros + (cajeros == 1 ? " cajero con ventas" : " cajeros con ventas");

        PdfPTable seccionCajeros = new PdfPTable(2);
        seccionCajeros.setWidthPercentage(100);
        seccionCajeros.setSpacingBefore(6);

        PdfPCell titCajeros = new PdfPCell(new Phrase("Ventas por cajero", EstilosReportePDF.fuente(13, Font.BOLD, EstilosReportePDF.AZUL)));
        titCajeros.setBorder(Rectangle.NO_BORDER);
        titCajeros.setPadding(0);

        PdfPCell cantCajeros = new PdfPCell(new Phrase(subtituloCajeros, EstilosReportePDF.fuente(9, Font.NORMAL, EstilosReportePDF.GRIS)));
        cantCajeros.setBorder(Rectangle.NO_BORDER);
        cantCajeros.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cantCajeros.setPadding(0);

        seccionCajeros.addCell(titCajeros);
        seccionCajeros.addCell(cantCajeros);
        documento.add(seccionCajeros);

        // Tabla de ventas por cajero
        String[] columnas = {"CAJERO", "TICKETS", "TOTAL RECAUDADO", "PARTICIPACIÓN"};
        float[] anchos = {3.4f, 1.1f, 1.7f, 1.4f};
        int[] alineaciones = {Element.ALIGN_LEFT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};

        PdfPTable tabla = EstilosReportePDF.crearTabla(anchos, columnas, alineaciones);

        List<ReportesDAO.VentaCajero> filas = reporte.cajeros().stream()
                .sorted(Comparator.comparing(ReportesDAO.VentaCajero::total).reversed()).toList();

        for (int i = 0; i < filas.size(); i++) {
            var venta = filas.get(i);
            Color fondo = (i % 2 == 0) ? EstilosReportePDF.FILA_PAR : Color.WHITE;
            BigDecimal participacion = reporte.total().signum() == 0 ? BigDecimal.ZERO
                    : venta.total().multiply(BigDecimal.valueOf(100)).divide(reporte.total(), 1, RoundingMode.HALF_UP);

            String textoCajero = venta.nombre() + " (#" + venta.idUsuario() + ")"
                    + (venta.cajas() != null && !venta.cajas().isBlank() ? " · " + venta.cajas() : "");
            tabla.addCell(EstilosReportePDF.celdaDato(textoCajero,
                    Element.ALIGN_LEFT, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tabla.addCell(EstilosReportePDF.celdaDato(String.valueOf(venta.tickets()),
                    Element.ALIGN_RIGHT, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tabla.addCell(EstilosReportePDF.celdaDato(dinero(venta.total()),
                    Element.ALIGN_RIGHT, fondo, EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
            tabla.addCell(EstilosReportePDF.celdaDato(participacion.toPlainString() + " %",
                    Element.ALIGN_RIGHT, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
        }

        if (filas.isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("No hay ventas registradas en el período.",
                    EstilosReportePDF.fuente(9, Font.ITALIC, EstilosReportePDF.GRIS)));
            vacio.setColspan(4);
            vacio.setPadding(10);
            vacio.setBorder(Rectangle.BOTTOM);
            vacio.setBorderColor(EstilosReportePDF.BORDE);
            tabla.addCell(vacio);
        }
        documento.add(tabla);

        // Desglose opcional por caja física
        if (reporte.cajas() != null && !reporte.cajas().isEmpty()) {
            documento.add(EstilosReportePDF.tituloSeccion("Recaudación por caja física"));
            String[] colsCajas = {"CAJA", "CAJEROS EN TURNO", "TICKETS", "TOTAL RECAUDADO", "PARTICIPACIÓN"};
            float[] anchosCajas = {2.6f, 1.4f, 1.1f, 1.6f, 1.3f};
            int[] alignCajas = {Element.ALIGN_LEFT, Element.ALIGN_CENTER, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};
            PdfPTable tablaCajas = EstilosReportePDF.crearTabla(anchosCajas, colsCajas, alignCajas);
            for (int i = 0; i < reporte.cajas().size(); i++) {
                var c = reporte.cajas().get(i);
                Color fondo = (i % 2 == 0) ? EstilosReportePDF.FILA_PAR : Color.WHITE;
                BigDecimal part = reporte.total().signum() == 0 ? BigDecimal.ZERO
                        : c.total().multiply(BigDecimal.valueOf(100)).divide(reporte.total(), 1, RoundingMode.HALF_UP);
                tablaCajas.addCell(EstilosReportePDF.celdaDato(c.nombreCaja(), Element.ALIGN_LEFT, fondo, EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
                tablaCajas.addCell(EstilosReportePDF.celdaDato(String.valueOf(c.cajerosDistintos()), Element.ALIGN_CENTER, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
                tablaCajas.addCell(EstilosReportePDF.celdaDato(String.valueOf(c.tickets()), Element.ALIGN_RIGHT, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
                tablaCajas.addCell(EstilosReportePDF.celdaDato(dinero(c.total()), Element.ALIGN_RIGHT, fondo, EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
                tablaCajas.addCell(EstilosReportePDF.celdaDato(part.toPlainString() + " %", Element.ALIGN_RIGHT, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
            }
            documento.add(tablaCajas);
        }

        // Caja de resumen al pie
        String subtituloTotal = reporte.tickets() + " tickets   |   " + subtituloCajeros;
        documento.add(EstilosReportePDF.cajaResumen("TOTAL DEL PERÍODO", subtituloTotal, dinero(reporte.total())));

        // Observaciones
        Paragraph obs = new Paragraph("Observaciones", EstilosReportePDF.fuente(9.5f, Font.BOLD, EstilosReportePDF.AZUL));
        obs.setSpacingBefore(6);
        documento.add(obs);

        Paragraph nota = new Paragraph("Los montos corresponden a las transacciones registradas en el período seleccionado. "
                + "La participación indica el porcentaje de recaudación atribuido a cada usuario de caja.",
                EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS));
        nota.setSpacingBefore(1);
        documento.add(nota);
    }

    static String dinero(BigDecimal valor) {
        if (valor == null) return "$ 0.00";
        return "$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
