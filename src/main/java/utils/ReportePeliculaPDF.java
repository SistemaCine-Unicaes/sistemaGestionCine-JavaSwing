package utils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import dao.ReportePeliculaDAO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

/** Genera un PDF imprimible con la ficha y el historial completo de ventas de la película. */
public final class ReportePeliculaPDF {
    private static final Color ROJO = new Color(180, 35, 60);
    private static final Color AZUL = new Color(24, 34, 53);
    private static final Color GRIS = new Color(88, 101, 121);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportePeliculaPDF() {}

    public static void generar(ReportePeliculaDAO.Reporte reporte, Path destino)
            throws IOException, DocumentException {
        Document documento = new Document(PageSize.A4.rotate(), 34, 34, 34, 42);
        try (OutputStream salida = Files.newOutputStream(destino)) {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());
            documento.open();
            escribir(reporte, documento);
            documento.close();
        } finally {
            if (documento.isOpen()) documento.close();
        }
    }

    private static void escribir(ReportePeliculaDAO.Reporte reporte, Document documento)
            throws DocumentException {
        var pelicula = reporte.pelicula();
        Font marca = new Font(Font.HELVETICA, 9, Font.BOLD, ROJO);
        Font titulo = new Font(Font.HELVETICA, 22, Font.BOLD, AZUL);
        Font normal = new Font(Font.HELVETICA, 9, Font.NORMAL, GRIS);
        Font negrita = new Font(Font.HELVETICA, 9, Font.BOLD, AZUL);
        Font seccion = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL);

        documento.add(new Paragraph("SISTEMA DE GESTIÓN DE CINE  /  REPORTES", marca));
        Paragraph encabezado = new Paragraph("Reporte: " + texto(pelicula.getNombre()), titulo);
        encabezado.setSpacingBefore(5); encabezado.setSpacingAfter(3); documento.add(encabezado);
        documento.add(new Paragraph("Generado: " + ahora(), normal));

        com.lowagie.text.Image poster = poster(pelicula.getImagenUrl());
        if (poster != null) {
            poster.scaleToFit(130, 170);
        }

        PdfPTable ficha = new PdfPTable(new float[]{1});
        ficha.setWidthPercentage(100); ficha.setSpacingBefore(10); ficha.setSpacingAfter(10);
        dato(ficha, "PELÍCULA", pelicula.getNombre(), negrita, normal);
        dato(ficha, "ID", "#" + pelicula.getIdPelicula(), negrita, normal);
        dato(ficha, "ESTADO", texto(pelicula.getEstado()), negrita, normal);
        dato(ficha, "DURACIÓN", pelicula.getDuracion() + " min", negrita, normal);
        dato(ficha, "GÉNERO", texto(pelicula.getGenero()), negrita, normal);
        dato(ficha, "DIRECTOR", texto(pelicula.getDirector()), negrita, normal);
        dato(ficha, "ESTRENO", pelicula.getFechaEstreno() == null ? "Sin fecha" : FECHA.format(pelicula.getFechaEstreno().toLocalDate()), negrita, normal);
        dato(ficha, "TIPO DE ESTRENO", texto(pelicula.getTipoEstreno()), negrita, normal);
        PdfPCell sinopsis = new PdfPCell(new Phrase("Sinopsis: " + texto(pelicula.getSinopsis()), normal));
        sinopsis.setPadding(8); sinopsis.setBorder(com.lowagie.text.Rectangle.NO_BORDER); ficha.addCell(sinopsis);
        PdfPTable encabezadoPelicula = new PdfPTable(new float[]{1.15f, 4.85f});
        encabezadoPelicula.setWidthPercentage(100);
        encabezadoPelicula.setSpacingBefore(10);
        encabezadoPelicula.setSpacingAfter(10);
        PdfPCell celdaPoster = new PdfPCell();
        celdaPoster.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        celdaPoster.setPadding(4);
        if (poster == null) celdaPoster.addElement(new Paragraph("Póster no disponible", normal));
        else celdaPoster.addElement(poster);
        PdfPCell celdaFicha = new PdfPCell(ficha);
        celdaFicha.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        celdaFicha.setPadding(4);
        encabezadoPelicula.addCell(celdaPoster);
        encabezadoPelicula.addCell(celdaFicha);
        documento.add(encabezadoPelicula);

        PdfPTable resumen = new PdfPTable(new float[]{1, 1, 1, 1});
        resumen.setWidthPercentage(100); resumen.setSpacingAfter(16);
        dato(resumen, "FUNCIONES", String.valueOf(reporte.funciones().size()), negrita, normal);
        dato(resumen, "BOLETOS VENDIDOS", String.valueOf(reporte.totalTickets()), negrita, normal);
        dato(resumen, "INGRESOS TOTALES", dinero(reporte.totalIngresos()), negrita, normal);
        BigDecimal promedio = reporte.totalTickets() == 0 ? BigDecimal.ZERO
                : reporte.totalIngresos().divide(BigDecimal.valueOf(reporte.totalTickets()), 2, RoundingMode.HALF_UP);
        dato(resumen, "PROMEDIO POR BOLETO", dinero(promedio), negrita, normal);
        documento.add(resumen);

        documento.add(new Paragraph("Funciones programadas", seccion));
        documento.add(new Paragraph("Incluye funciones sin ventas.", normal));
        PdfPTable funciones = tabla(new String[]{"Función", "Fecha", "Inicio", "Fin", "Sala", "Estado", "Boletos", "Ingresos"},
                new float[]{0.8f, 1.1f, 0.9f, 0.9f, 0.7f, 1.2f, 0.8f, 1.1f});
        for (var f : reporte.funciones()) {
            celda(funciones, "#" + f.id()); celda(funciones, FECHA.format(f.fecha().toLocalDate()));
            celda(funciones, f.inicio().toString().substring(0, 5)); celda(funciones, f.fin().toString().substring(0, 5));
            celda(funciones, "Sala #" + f.idSala()); celda(funciones, texto(f.estado()));
            celda(funciones, String.valueOf(f.tickets())); celda(funciones, dinero(f.total()));
        }
        if (reporte.funciones().isEmpty()) vacio(funciones, 8, "No hay funciones registradas para esta película.");
        documento.add(funciones);

        Paragraph detalle = new Paragraph("Detalle de boletos vendidos", seccion);
        detalle.setSpacingBefore(16); detalle.setSpacingAfter(3); documento.add(detalle);
        documento.add(new Paragraph("Cada fila corresponde a un ticket registrado.", normal));
        PdfPTable tickets = tabla(new String[]{"Ticket", "Función", "Compra", "Proyección", "Sala", "Asiento", "Tipo", "Cajero", "Monto"},
                new float[]{0.7f, 0.7f, 1.3f, 1.25f, 0.7f, 0.7f, 0.9f, 1.2f, 0.8f});
        for (var t : reporte.tickets()) {
            celda(tickets, "#" + t.id()); celda(tickets, "#" + t.idFuncion());
            celda(tickets, FECHA_HORA.format(t.compra().toLocalDateTime()));
            celda(tickets, FECHA.format(t.fechaFuncion().toLocalDate()) + " " + t.horaFuncion().toString().substring(0, 5));
            celda(tickets, "Sala #" + t.idSala()); celda(tickets, texto(t.asiento()));
            celda(tickets, texto(t.tipoAsiento())); celda(tickets, texto(t.cajero()) + " (#" + t.idUsuario() + ")");
            celda(tickets, dinero(t.monto()));
        }
        if (reporte.tickets().isEmpty()) vacio(tickets, 9, "Esta película todavía no registra boletos vendidos.");
        documento.add(tickets);
    }

    private static PdfPTable tabla(String[] encabezados, float[] anchos) throws DocumentException {
        PdfPTable tabla = new PdfPTable(anchos); tabla.setWidthPercentage(100); tabla.setHeaderRows(1);
        tabla.setSpacingBefore(7); tabla.setSplitRows(true);
        Font fuente = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuente));
            celda.setBackgroundColor(AZUL); celda.setPadding(6); celda.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.addCell(celda);
        }
        return tabla;
    }

    private static void celda(PdfPTable tabla, String valor) {
        PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "—" : valor, new Font(Font.HELVETICA, 7)));
        celda.setPadding(5); celda.setBorderColor(new Color(225, 229, 235)); tabla.addCell(celda);
    }

    private static void dato(PdfPTable tabla, String etiqueta, String valor, Font fuenteEtiqueta, Font fuenteValor) {
        PdfPCell celda = new PdfPCell(); celda.setPadding(4); celda.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        Paragraph texto = new Paragraph();
        texto.add(new Phrase(etiqueta + "   ", fuenteEtiqueta));
        texto.add(new Phrase(valor, fuenteValor));
        celda.addElement(texto); tabla.addCell(celda);
    }

    private static com.lowagie.text.Image poster(String ruta) {
        if (ruta == null || ruta.isBlank()) return null;
        try {
            BufferedImage imagen;
            String origen = ruta.trim();
            if (origen.regionMatches(true, 0, "https://", 0, 8)
                    || origen.regionMatches(true, 0, "http://", 0, 7)) {
                HttpURLConnection conexion = (HttpURLConnection) URI.create(origen).toURL().openConnection();
                conexion.setConnectTimeout(5000); conexion.setReadTimeout(5000);
                conexion.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (var entrada = conexion.getInputStream()) { imagen = ImageIO.read(entrada); }
                finally { conexion.disconnect(); }
            } else {
                try (var entrada = Files.newInputStream(Path.of(origen))) { imagen = ImageIO.read(entrada); }
            }
            return imagen == null ? null : com.lowagie.text.Image.getInstance(imagen, null);
        } catch (Exception e) {
            return null;
        }
    }

    private static void vacio(PdfPTable tabla, int columnas, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, new Font(Font.HELVETICA, 8, Font.ITALIC, GRIS)));
        celda.setColspan(columnas); celda.setPadding(8); tabla.addCell(celda);
    }

    private static String ahora() {
        ZoneId zona = ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador"));
        return FECHA_HORA.format(LocalDateTime.now(zona));
    }
    private static String dinero(BigDecimal valor) { return "$" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
    private static String texto(String valor) { return valor == null || valor.isBlank() ? "Sin especificar" : valor; }

    private static final class PiePagina extends PdfPageEventHelper {
        @Override public void onEndPage(PdfWriter writer, Document documento) {
            Phrase texto = new Phrase("Página " + writer.getPageNumber(), new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS));
            com.lowagie.text.pdf.ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, texto,
                    documento.right(), documento.bottom() - 18, 0);
        }
    }
}
