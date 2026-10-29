package utils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
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

/** Reporte PDF del corte de caja con el diseño de la plantilla compartida por el equipo. */
public final class ReporteCajaPDF {
    private static final Color AZUL = new Color(24, 34, 53);
    private static final Color ROJO = new Color(180, 35, 60);
    private static final Color GRIS = new Color(88, 101, 121);
    private static final Color BORDE = new Color(220, 225, 232);
    private static final Color FILA = new Color(243, 244, 246);
    private static final Color ROSA = new Color(252, 231, 235);
    private static final Color CLARO = new Color(226, 232, 240);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReporteCajaPDF() {}

    /** @param hasta último día incluido en el corte. */
    public static void generar(ReportesDAO.Reporte reporte, String tipo, LocalDate desde, LocalDate hasta,
            String usuario, Path destino) throws IOException, DocumentException {
        // Se arma en memoria: si falla, no queda un PDF incompleto en el destino.
        Files.write(destino, crear(reporte, tipo, desde, hasta, usuario));
    }

    static byte[] crear(ReportesDAO.Reporte reporte, String tipo, LocalDate desde, LocalDate hasta, String usuario)
            throws IOException, DocumentException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 48, 48, 152, 70);
        PdfWriter writer = PdfWriter.getInstance(documento, salida);
        writer.setPageEvent(new Pagina(LocalDateTime.now(ZoneId.of(
                config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador")))));
        documento.open();
        escribir(documento, reporte, tipo, desde, hasta, usuario);
        documento.close();
        return salida.toByteArray();
    }

    private static void escribir(Document documento, ReportesDAO.Reporte reporte, String tipo, LocalDate desde,
            LocalDate hasta, String usuario) throws DocumentException {
        PdfPTable titulo = fila(texto("Resumen de caja", 17, Font.BOLD, AZUL),
                texto("Período: " + FECHA.format(desde) + " - " + FECHA.format(hasta), 10.5f, Font.NORMAL, GRIS));
        documento.add(titulo);
        Paragraph descripcion = new Paragraph("Corte " + tipo.toLowerCase() + " de todas las cajas. Generado por "
                + usuario + ".", fuente(9, Font.NORMAL, GRIS));
        descripcion.setSpacingBefore(4); descripcion.setSpacingAfter(16);
        documento.add(descripcion);

        BigDecimal promedio = reporte.tickets() == 0 ? BigDecimal.ZERO
                : reporte.total().divide(BigDecimal.valueOf(reporte.tickets()), 2, RoundingMode.HALF_UP);
        PdfPTable tarjetas = new PdfPTable(3);
        tarjetas.setWidthPercentage(100);
        tarjeta(tarjetas, "TICKETS VENDIDOS", String.valueOf(reporte.tickets()), 0, 6);
        tarjeta(tarjetas, "INGRESOS TOTALES", dinero(reporte.total()), 6, 6);
        tarjeta(tarjetas, "PROMEDIO POR TICKET", dinero(promedio), 6, 0);
        documento.add(tarjetas);

        int cajeros = reporte.cajeros().size();
        PdfPTable seccion = fila(texto("Ventas por cajero", 14, Font.BOLD, AZUL),
                texto(cajeros + (cajeros == 1 ? " cajero con ventas" : " cajeros con ventas"), 9, Font.NORMAL, GRIS));
        seccion.setSpacingBefore(24);
        documento.add(seccion);

        PdfPTable tabla = new PdfPTable(new float[]{3.4f, 1.1f, 1.6f, 1.4f});
        tabla.setWidthPercentage(100); tabla.setSpacingBefore(10); tabla.setHeaderRows(1);
        String[] columnas = {"CAJERO", "TICKETS", "TOTAL RECAUDADO", "PARTICIPACIÓN"};
        for (int i = 0; i < columnas.length; i++) {
            PdfPCell celda = celda(columnas[i], fuente(8, Font.BOLD, Color.WHITE), i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            celda.setBackgroundColor(AZUL); celda.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(celda);
        }
        // Mismo orden que la pantalla: de mayor a menor recaudación.
        List<ReportesDAO.VentaCajero> filas = reporte.cajeros().stream()
                .sorted(Comparator.comparing(ReportesDAO.VentaCajero::total).reversed()).toList();
        for (int i = 0; i < filas.size(); i++) {
            var venta = filas.get(i);
            Color fondo = i % 2 == 0 ? FILA : Color.WHITE;
            BigDecimal participacion = reporte.total().signum() == 0 ? BigDecimal.ZERO
                    : venta.total().multiply(BigDecimal.valueOf(100)).divide(reporte.total(), 1, RoundingMode.HALF_UP);
            tabla.addCell(dato(venta.nombre() + " (#" + venta.idUsuario() + ")", Font.NORMAL, Element.ALIGN_LEFT, fondo));
            tabla.addCell(dato(String.valueOf(venta.tickets()), Font.NORMAL, Element.ALIGN_RIGHT, fondo));
            tabla.addCell(dato(dinero(venta.total()), Font.BOLD, Element.ALIGN_RIGHT, fondo));
            tabla.addCell(dato(participacion.toPlainString() + " %", Font.NORMAL, Element.ALIGN_RIGHT, fondo));
        }
        if (filas.isEmpty()) {
            PdfPCell vacio = celda("No hay ventas registradas en el período.", fuente(9, Font.ITALIC, GRIS), Element.ALIGN_LEFT);
            vacio.setColspan(4); vacio.setBorder(Rectangle.BOTTOM); vacio.setBorderColor(BORDE);
            tabla.addCell(vacio);
        }
        documento.add(tabla);

        PdfPTable total = new PdfPTable(2);
        total.setWidthPercentage(100); total.setSpacingBefore(10);
        PdfPCell etiquetaTotal = celda(texto("TOTAL DEL PERÍODO", 10, Font.BOLD, ROJO), Element.ALIGN_LEFT);
        PdfPCell cifraTotal = celda(texto(reporte.tickets() + " tickets   |   " + dinero(reporte.total()), 13, Font.BOLD, ROJO),
                Element.ALIGN_RIGHT);
        etiquetaTotal.setCellEvent(new Tarjeta(ROSA, null, null, 0, 0, true, false));
        cifraTotal.setCellEvent(new Tarjeta(ROSA, null, null, 0, 0, false, true));
        for (PdfPCell c : new PdfPCell[]{etiquetaTotal, cifraTotal}) {
            c.setBorder(Rectangle.NO_BORDER); c.setPadding(15); total.addCell(c);
        }
        documento.add(total);

        Paragraph observaciones = new Paragraph("Observaciones", fuente(10, Font.BOLD, AZUL));
        observaciones.setSpacingBefore(24);
        documento.add(observaciones);
        Paragraph nota = new Paragraph("Los montos son los registrados al momento de cada venta. La participación es el "
                + "porcentaje de los ingresos del período que corresponde a cada cajero.", fuente(9, Font.NORMAL, GRIS));
        nota.setSpacingBefore(3);
        documento.add(nota);
    }

    private static void tarjeta(PdfPTable tabla, String etiqueta, String valor, float izquierda, float derecha) {
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.NO_BORDER); celda.setMinimumHeight(74);
        celda.setPaddingLeft(izquierda + 18); celda.setPaddingRight(derecha + 10);
        celda.setPaddingTop(12); celda.setPaddingBottom(14);
        celda.addElement(new Paragraph(etiqueta, fuente(8, Font.BOLD, GRIS)));
        Paragraph cifra = new Paragraph(valor, fuente(21, Font.BOLD, AZUL));
        cifra.setSpacingBefore(8);
        celda.addElement(cifra);
        celda.setCellEvent(new Tarjeta(Color.WHITE, BORDE, ROJO, izquierda, derecha, true, true));
        tabla.addCell(celda);
    }

    private static PdfPTable fila(Phrase izquierda, Phrase derecha) {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        PdfPCell a = celda(izquierda, Element.ALIGN_LEFT), b = celda(derecha, Element.ALIGN_RIGHT);
        for (PdfPCell c : new PdfPCell[]{a, b}) {
            c.setBorder(Rectangle.NO_BORDER); c.setPadding(0); c.setPaddingBottom(14);
            c.setVerticalAlignment(Element.ALIGN_BOTTOM); tabla.addCell(c);
        }
        return tabla;
    }

    private static PdfPCell dato(String valor, int estilo, int alineacion, Color fondo) {
        PdfPCell celda = celda(valor, fuente(8.5f, estilo, AZUL), alineacion);
        celda.setBackgroundColor(fondo);
        celda.setBorder(Rectangle.BOTTOM); celda.setBorderColor(BORDE); celda.setBorderWidth(0.6f);
        return celda;
    }

    private static PdfPCell celda(String valor, Font fuente, int alineacion) {
        return celda(new Phrase(valor, fuente), alineacion);
    }

    private static PdfPCell celda(Phrase frase, int alineacion) {
        PdfPCell celda = new PdfPCell(frase);
        celda.setHorizontalAlignment(alineacion); celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPaddingTop(10); celda.setPaddingBottom(12); celda.setPaddingLeft(10); celda.setPaddingRight(10);
        return celda;
    }

    private static Phrase texto(String valor, float tamano, int estilo, Color color) {
        return new Phrase(valor, fuente(tamano, estilo, color));
    }

    private static Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    static String dinero(BigDecimal valor) {
        return "$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Fondo redondeado de una celda; une celdas vecinas redondeando solo sus extremos. */
    private record Tarjeta(Color fondo, Color borde, Color acento, float izquierda, float derecha,
            boolean inicio, boolean fin) implements PdfPCellEvent {
        @Override public void cellLayout(PdfPCell celda, Rectangle r, PdfContentByte[] lienzos) {
            PdfContentByte cb = lienzos[PdfPTable.BACKGROUNDCANVAS];
            float x = r.getLeft() + izquierda - (inicio ? 0 : 8), ancho = r.getWidth() - izquierda - derecha + (inicio ? 0 : 8) + (fin ? 0 : 8);
            cb.saveState();
            cb.setColorFill(fondo);
            cb.roundRectangle(x, r.getBottom(), ancho, r.getHeight(), 6);
            if (borde == null) cb.fill();
            else { cb.setColorStroke(borde); cb.setLineWidth(0.8f); cb.fillStroke(); }
            if (acento != null) {
                cb.setColorFill(acento);
                cb.roundRectangle(x, r.getBottom(), 3.5f, r.getHeight(), 1.5f);
                cb.fill();
            }
            cb.restoreState();
        }
    }

    /** Encabezado y pie de cada página, con "Página X de Y". */
    private static final class Pagina extends PdfPageEventHelper {
        private final String generado;
        private PdfTemplate totalPaginas;
        private BaseFont base;

        Pagina(LocalDateTime ahora) {
            generado = "Generado: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(ahora);
        }

        @Override public void onOpenDocument(PdfWriter writer, Document documento) {
            totalPaginas = writer.getDirectContent().createTemplate(30, 12);
            try { base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED); }
            catch (DocumentException | IOException e) { throw new IllegalStateException("No se pudo cargar la fuente del reporte.", e); }
        }

        @Override public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle hoja = documento.getPageSize();
            float alto = hoja.getHeight(), ancho = hoja.getWidth();
            cb.saveState();
            cb.setColorFill(AZUL); cb.rectangle(0, alto - 118, ancho, 118); cb.fill();
            cb.setColorFill(ROJO); cb.rectangle(0, alto - 123, ancho, 5); cb.fill();
            cb.roundRectangle(48, alto - 88, 44, 44, 9); cb.fill();
            cb.restoreState();
            escribir(cb, "C", 24, Font.BOLD, Color.WHITE, Element.ALIGN_CENTER, 70, alto - 75);
            escribir(cb, "CINE | GESTIÓN", 20, Font.BOLD, Color.WHITE, Element.ALIGN_LEFT, 106, alto - 63);
            escribir(cb, "REPORTE DE CORTE DE CAJA", 12.5f, Font.BOLD, CLARO, Element.ALIGN_LEFT, 106, alto - 83);
            escribir(cb, "ADMINISTRACIÓN", 10, Font.BOLD, CLARO, Element.ALIGN_RIGHT, ancho - 48, alto - 60);
            escribir(cb, generado, 8.5f, Font.NORMAL, CLARO, Element.ALIGN_RIGHT, ancho - 48, alto - 76);

            cb.saveState();
            cb.setColorStroke(BORDE); cb.setLineWidth(0.6f);
            cb.moveTo(48, 52); cb.lineTo(ancho - 48, 52); cb.stroke();
            cb.restoreState();
            escribir(cb, "Sistema de Gestión de Cine  •  Reporte generado para administración", 8, Font.NORMAL, GRIS,
                    Element.ALIGN_LEFT, 48, 38);
            // El total se conoce al cerrar el documento; se reserva su espacio a la derecha.
            float x = ancho - 48 - 12;
            escribir(cb, "Página " + writer.getPageNumber() + " de", 8, Font.NORMAL, GRIS, Element.ALIGN_RIGHT, x, 38);
            cb.addTemplate(totalPaginas, x + 2.5f, 38);
        }

        @Override public void onCloseDocument(PdfWriter writer, Document documento) {
            totalPaginas.beginText();
            totalPaginas.setFontAndSize(base, 8);
            totalPaginas.setColorFill(GRIS);
            totalPaginas.setTextMatrix(0, 0);
            totalPaginas.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPaginas.endText();
        }

        private static void escribir(PdfContentByte cb, String valor, float tamano, int estilo, Color color,
                int alineacion, float x, float y) {
            ColumnText.showTextAligned(cb, alineacion, texto(valor, tamano, estilo, color), x, y, 0);
        }
    }
}
