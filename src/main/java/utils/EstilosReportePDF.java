package utils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.IOException;
import java.util.List;

/**
 * Utilidades y estilos unificados para los reportes PDF del sistema.
 * Replica el diseño visual institucional de reporte_funcion.jrxml:
 * - A4 Portrait (595 x 842 pt).
 * - Banda superior azul marino (#182235) con franja roja (#B4233C).
 * - Tipografía Helvetica en tamaños y pesos estandarizados.
 * - Tablas con cabecera sólida azul marino y filas alternas con zebra striping (#F4F5F7).
 * - Tarjetas de KPI y cuadro de totales en verde suave (#E8F5EC).
 * - Pie de página institucional con fecha y "Página X de Y".
 */
public final class EstilosReportePDF {

    public static final Color AZUL = new Color(24, 34, 53);          // #182235 (Primario institucional)
    public static final Color ROJO = new Color(180, 35, 60);         // #B4233C (Acento institucional)
    public static final Color GRIS = new Color(88, 101, 121);        // #586579 (Texto secundario / muted)
    public static final Color BORDE = new Color(220, 225, 232);      // #DCE1E8 (Bordes de tabla)
    public static final Color FILA_PAR = new Color(244, 245, 247);   // #F4F5F7 (Zebra row)
    public static final Color FONDO_TOTAL = new Color(232, 245, 236);// #E8F5EC (Verde total suave)
    public static final Color TEXTO_TOTAL = new Color(22, 115, 71);   // #167347 (Verde total texto)

    private static BaseFont baseFont;
    private static BaseFont baseFontBold;

    static {
        try {
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            baseFontBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException | IOException e) {
            baseFont = null;
            baseFontBold = null;
        }
    }

    private EstilosReportePDF() {}

    public static Font fuente(float tamano, int estilo, Color color) {
        if (baseFont != null && baseFontBold != null) {
            BaseFont bf = (estilo == Font.BOLD) ? baseFontBold : baseFont;
            return new Font(bf, tamano, estilo, color);
        }
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    public static Paragraph tituloSeccion(String texto) {
        Paragraph p = new Paragraph(texto, fuente(14, Font.BOLD, AZUL));
        p.setSpacingBefore(8);
        p.setSpacingAfter(8);
        return p;
    }

    public static Paragraph descripcionSeccion(String texto) {
        Paragraph p = new Paragraph(texto, fuente(9f, Font.NORMAL, GRIS));
        p.setSpacingAfter(8);
        return p;
    }

    public record TarjetaKPI(String titulo, String valor) {}

    public static PdfPTable crearTarjetasKPI(List<TarjetaKPI> kpis) {
        if (kpis == null || kpis.isEmpty()) return new PdfPTable(1);
        float[] anchos = new float[kpis.size()];
        for (int i = 0; i < anchos.length; i++) anchos[i] = 1f;

        PdfPTable contenedor = new PdfPTable(anchos);
        contenedor.setWidthPercentage(100);
        contenedor.setSpacingBefore(4);
        contenedor.setSpacingAfter(6);

        for (int i = 0; i < kpis.size(); i++) {
            TarjetaKPI kpi = kpis.get(i);
            PdfPCell celda = new PdfPCell();
            celda.setBackgroundColor(FILA_PAR);
            celda.setBorder(Rectangle.BOX);
            celda.setBorderColor(BORDE);
            celda.setPadding(8);
            celda.setPaddingBottom(10);

            Paragraph tit = new Paragraph(kpi.titulo().toUpperCase(), fuente(7.5f, Font.BOLD, GRIS));
            tit.setAlignment(Element.ALIGN_CENTER);
            tit.setSpacingAfter(4);
            celda.addElement(tit);

            Paragraph val = new Paragraph(kpi.valor(), fuente(13, Font.BOLD, AZUL));
            val.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(val);

            contenedor.addCell(celda);
        }
        return contenedor;
    }

    public static PdfPTable crearTabla(float[] anchos, String[] cabeceras, int[] alineaciones) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(6);

        for (int i = 0; i < cabeceras.length; i++) {
            PdfPCell celda = new PdfPCell(new Phrase(cabeceras[i], fuente(8f, Font.BOLD, Color.WHITE)));
            celda.setBackgroundColor(AZUL);
            celda.setHorizontalAlignment(alineaciones != null && i < alineaciones.length ? alineaciones[i] : Element.ALIGN_LEFT);
            celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celda.setPaddingTop(6);
            celda.setPaddingBottom(6);
            celda.setPaddingLeft(6);
            celda.setPaddingRight(6);
            celda.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(celda);
        }
        return tabla;
    }

    public static PdfPCell celdaDato(String texto, int alineacion, Color fondo, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPaddingTop(5);
        celda.setPaddingBottom(5);
        celda.setPaddingLeft(6);
        celda.setPaddingRight(6);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(BORDE);
        return celda;
    }

    public static PdfPTable cajaResumen(String titulo, String subtitulo, String monto) {
        PdfPTable tabla = new PdfPTable(new float[]{3.5f, 1.5f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(6);
        tabla.setSpacingAfter(4);

        PdfPCell izq = new PdfPCell();
        izq.setBackgroundColor(FONDO_TOTAL);
        izq.setBorder(Rectangle.BOX);
        izq.setBorderColor(BORDE);
        izq.setPadding(10);

        Paragraph tit = new Paragraph(titulo, fuente(10f, Font.BOLD, AZUL));
        izq.addElement(tit);
        if (subtitulo != null && !subtitulo.isBlank()) {
            Paragraph sub = new Paragraph(subtitulo, fuente(8.5f, Font.NORMAL, GRIS));
            sub.setSpacingBefore(2);
            izq.addElement(sub);
        }

        PdfPCell der = new PdfPCell();
        der.setBackgroundColor(FONDO_TOTAL);
        der.setBorder(Rectangle.BOX);
        der.setBorderColor(BORDE);
        der.setPadding(10);
        der.setHorizontalAlignment(Element.ALIGN_RIGHT);
        der.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph total = new Paragraph(monto, fuente(14f, Font.BOLD, TEXTO_TOTAL));
        total.setAlignment(Element.ALIGN_RIGHT);
        der.addElement(total);

        tabla.addCell(izq);
        tabla.addCell(der);
        return tabla;
    }

    /**
     * Controlador de eventos de página para dibujar encabezado corporativo (banner)
     * y pie de página institucional ("Página X de Y").
     */
    public static class EventoPaginaReporte extends PdfPageEventHelper {
        private final String subtituloReporte;
        private final String etiquetaRol;
        private final String fechaGenerado;
        private final boolean bannerSoloPagina1;
        private PdfTemplate plantillaTotal;
        private int totalPaginas = 1;

        public EventoPaginaReporte(String subtituloReporte, String etiquetaRol, String fechaGenerado, boolean bannerSoloPagina1) {
            this.subtituloReporte = subtituloReporte;
            this.etiquetaRol = etiquetaRol;
            this.fechaGenerado = fechaGenerado;
            this.bannerSoloPagina1 = bannerSoloPagina1;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            plantillaTotal = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            int pagina = writer.getPageNumber();
            totalPaginas = pagina;

            // 1. Encabezado corporativo (página 1, o todas si se configuró así)
            if (pagina == 1 || !bannerSoloPagina1) {
                float altoPagina = document.getPageSize().getHeight();
                float anchoPagina = document.getPageSize().getWidth();

                // Banda azul (#182235) de 75 pt
                cb.saveState();
                cb.setColorFill(AZUL);
                cb.rectangle(0, altoPagina - 75, anchoPagina, 75);
                cb.fill();

                // Franja roja (#B4233C) de 5 pt debajo de la banda azul
                cb.setColorFill(ROJO);
                cb.rectangle(0, altoPagina - 80, anchoPagina, 5);
                cb.fill();
                cb.restoreState();

                // Textos del encabezado
                cb.beginText();
                cb.setColorFill(Color.WHITE);

                // Marca: CINE | GESTIÓN
                cb.setFontAndSize(baseFontBold != null ? baseFontBold : baseFont, 16);
                cb.showTextAligned(Element.ALIGN_LEFT, "CINE | GESTIÓN", 35, altoPagina - 36, 0);

                // Subtítulo del reporte
                cb.setFontAndSize(baseFontBold != null ? baseFontBold : baseFont, 10.5f);
                cb.showTextAligned(Element.ALIGN_LEFT, subtituloReporte, 35, altoPagina - 56, 0);

                // Rol / Departamento a la derecha
                cb.setFontAndSize(baseFontBold != null ? baseFontBold : baseFont, 9.5f);
                cb.showTextAligned(Element.ALIGN_RIGHT, etiquetaRol, anchoPagina - 35, altoPagina - 36, 0);
                cb.endText();
            }

            // 2. Pie de página institucional en todas las páginas
            float pieY = 26;
            float margenIzq = 35;
            float margenDer = document.getPageSize().getWidth() - 35;

            // Línea divisoria
            cb.saveState();
            cb.setColorStroke(BORDE);
            cb.setLineWidth(0.6f);
            cb.moveTo(margenIzq, pieY + 12);
            cb.lineTo(margenDer, pieY + 12);
            cb.stroke();
            cb.restoreState();

            // Texto fecha / generado a la izquierda
            cb.beginText();
            cb.setFontAndSize(baseFont != null ? baseFont : baseFontBold, 8f);
            cb.setColorFill(GRIS);
            cb.showTextAligned(Element.ALIGN_LEFT, fechaGenerado, margenIzq, pieY, 0);

            // "Página X de " a la derecha
            String textoPag = "Página " + pagina + " de ";
            float anchoTexto = (baseFont != null ? baseFont : baseFontBold).getWidthPoint(textoPag, 8f);
            cb.showTextAligned(Element.ALIGN_RIGHT, textoPag, margenDer - 20, pieY, 0);
            cb.endText();

            // Template con total de páginas al final
            cb.addTemplate(plantillaTotal, margenDer - 20, pieY - 2);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            if (plantillaTotal != null) {
                plantillaTotal.beginText();
                plantillaTotal.setFontAndSize(baseFont != null ? baseFont : baseFontBold, 8f);
                plantillaTotal.setColorFill(GRIS);
                plantillaTotal.showText(String.valueOf(totalPaginas));
                plantillaTotal.endText();
            }
        }
    }
}
