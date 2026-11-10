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
import dao.ReportePeliculaDAO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import models.Pelicula;

/**
 * Genera un PDF imprimible con la ficha técnica, métricas de taquilla y el historial
 * completo de ventas de una película, con el diseño visual unificado del sistema.
 */
public final class ReportePeliculaPDF {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportePeliculaPDF() {}

    public static void generar(ReportePeliculaDAO.Reporte reporte, Path destino)
            throws IOException, DocumentException {
        Files.write(destino, crear(reporte));
    }

    static byte[] crear(ReportePeliculaDAO.Reporte reporte) throws IOException, DocumentException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        // A4 estándar vertical (595 x 842 pt), idéntico al reporte de funciones
        Document documento = new Document(PageSize.A4, 35, 35, 95, 45);
        PdfWriter writer = PdfWriter.getInstance(documento, salida);

        String generado = "Generado: " + ahora();
        writer.setPageEvent(new EstilosReportePDF.EventoPaginaReporte(
                "REPORTE DE PELÍCULA", "ADMINISTRACIÓN", generado, true));

        documento.open();
        escribir(reporte, documento);
        documento.close();
        return salida.toByteArray();
    }

    private static void escribir(ReportePeliculaDAO.Reporte reporte, Document documento)
            throws DocumentException {
        Pelicula pelicula = reporte.pelicula();

        // 1. Título de sección
        documento.add(EstilosReportePDF.tituloSeccion("Información de la película"));

        // 2. Ficha de la película con póster y metadatos
        PdfPTable fichaGeneral = new PdfPTable(new float[]{1.3f, 4.0f});
        fichaGeneral.setWidthPercentage(100);
        fichaGeneral.setSpacingAfter(10);

        // Celda del póster
        PdfPCell celdaPoster = new PdfPCell();
        celdaPoster.setBorder(Rectangle.NO_BORDER);
        celdaPoster.setPadding(0);
        celdaPoster.setPaddingRight(12);

        com.lowagie.text.Image poster = poster(pelicula.getImagenUrl());
        if (poster != null) {
            poster.scaleToFit(110, 150);
            celdaPoster.addElement(poster);
        } else {
            celdaPoster.setBackgroundColor(EstilosReportePDF.FILA_PAR);
            celdaPoster.setBorder(Rectangle.BOX);
            celdaPoster.setBorderColor(EstilosReportePDF.BORDE);
            celdaPoster.setMinimumHeight(120);
            Paragraph placeholder = new Paragraph("Sin póster\ndisponible",
                    EstilosReportePDF.fuente(8.5f, Font.ITALIC, EstilosReportePDF.GRIS));
            placeholder.setAlignment(Element.ALIGN_CENTER);
            celdaPoster.addElement(placeholder);
        }

        // Celda de datos
        PdfPCell celdaDatos = new PdfPCell();
        celdaDatos.setBorder(Rectangle.NO_BORDER);
        celdaDatos.setPadding(0);

        Paragraph nombrePeli = new Paragraph(texto(pelicula.getNombre()), EstilosReportePDF.fuente(13, Font.BOLD, EstilosReportePDF.AZUL));
        nombrePeli.setSpacingAfter(6);
        celdaDatos.addElement(nombrePeli);

        Paragraph datos = new Paragraph();
        datos.setLeading(14);
        datos.add(new Phrase("ID Película: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase("#" + pelicula.getIdPelicula() + "   |   ", EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
        datos.add(new Phrase("Estado: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase(texto(pelicula.getEstado()) + "\n", EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));

        datos.add(new Phrase("Duración: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase(pelicula.getDuracion() + " min   |   ", EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
        datos.add(new Phrase("Género: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase(texto(pelicula.getGenero()) + "\n", EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));

        datos.add(new Phrase("Director: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase(texto(pelicula.getDirector()) + "   |   ", EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
        datos.add(new Phrase("Estreno: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase((pelicula.getFechaEstreno() == null ? "Sin fecha" : FECHA.format(pelicula.getFechaEstreno().toLocalDate())) + "\n",
                EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));

        datos.add(new Phrase("Tipo de estreno: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase(texto(pelicula.getTipoEstreno()) + "\n\n", EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));

        datos.add(new Phrase("Sinopsis: ", EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        datos.add(new Phrase(texto(pelicula.getSinopsis()), EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
        celdaDatos.addElement(datos);

        fichaGeneral.addCell(celdaPoster);
        fichaGeneral.addCell(celdaDatos);
        documento.add(fichaGeneral);

        // 3. Tarjetas métricas (KPIs)
        BigDecimal promedio = reporte.totalTickets() == 0 ? BigDecimal.ZERO
                : reporte.totalIngresos().divide(BigDecimal.valueOf(reporte.totalTickets()), 2, RoundingMode.HALF_UP);

        var kpis = List.of(
                new EstilosReportePDF.TarjetaKPI("FUNCIONES", String.valueOf(reporte.funciones().size())),
                new EstilosReportePDF.TarjetaKPI("BOLETOS VENDIDOS", String.valueOf(reporte.totalTickets())),
                new EstilosReportePDF.TarjetaKPI("INGRESOS TOTALES", dinero(reporte.totalIngresos())),
                new EstilosReportePDF.TarjetaKPI("PROMEDIO POR BOLETO", dinero(promedio))
        );
        documento.add(EstilosReportePDF.crearTarjetasKPI(kpis));

        // 4. Funciones programadas
        documento.add(EstilosReportePDF.tituloSeccion("Funciones programadas"));
        documento.add(EstilosReportePDF.descripcionSeccion("Proyecciones programadas e historial de funciones para esta película."));

        String[] colsFunciones = {"FUNCIÓN", "FECHA", "HORARIO", "SALA", "ESTADO", "BOLETOS", "TOTAL"};
        float[] anchosFunciones = {1.0f, 1.2f, 1.3f, 1.0f, 1.2f, 1.0f, 1.3f};
        int[] alignFunciones = {Element.ALIGN_CENTER, Element.ALIGN_CENTER, Element.ALIGN_CENTER,
                Element.ALIGN_CENTER, Element.ALIGN_CENTER, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};

        PdfPTable tablaFunciones = EstilosReportePDF.crearTabla(anchosFunciones, colsFunciones, alignFunciones);

        for (int i = 0; i < reporte.funciones().size(); i++) {
            var f = reporte.funciones().get(i);
            Color fondo = (i % 2 == 0) ? EstilosReportePDF.FILA_PAR : Color.WHITE;
            String horario = (f.inicio() != null ? f.inicio().toString().substring(0, 5) : "--:--")
                    + " - " + (f.fin() != null ? f.fin().toString().substring(0, 5) : "--:--");

            tablaFunciones.addCell(EstilosReportePDF.celdaDato("#" + f.id(), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaFunciones.addCell(EstilosReportePDF.celdaDato(f.fecha() != null ? FECHA.format(f.fecha().toLocalDate()) : "—",
                    Element.ALIGN_CENTER, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaFunciones.addCell(EstilosReportePDF.celdaDato(horario, Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaFunciones.addCell(EstilosReportePDF.celdaDato("Sala " + f.idSala(), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaFunciones.addCell(EstilosReportePDF.celdaDato(texto(f.estado()), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
            tablaFunciones.addCell(EstilosReportePDF.celdaDato(String.valueOf(f.tickets()), Element.ALIGN_RIGHT, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaFunciones.addCell(EstilosReportePDF.celdaDato(dinero(f.total()), Element.ALIGN_RIGHT, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        }

        if (reporte.funciones().isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("No hay funciones registradas para esta película.",
                    EstilosReportePDF.fuente(8.5f, Font.ITALIC, EstilosReportePDF.GRIS)));
            vacio.setColspan(7);
            vacio.setPadding(8);
            vacio.setBorder(Rectangle.BOTTOM);
            vacio.setBorderColor(EstilosReportePDF.BORDE);
            tablaFunciones.addCell(vacio);
        }
        documento.add(tablaFunciones);

        // 5. Detalle de boletos vendidos
        documento.add(EstilosReportePDF.tituloSeccion("Detalle de boletos vendidos"));
        documento.add(EstilosReportePDF.descripcionSeccion("Registro de entradas emitidas individualmente por función y butaca."));

        String[] colsTickets = {"TICKET", "FUNCIÓN", "FECHA COMPRA", "SALA", "ASIENTO", "TIPO", "CAJERO", "MONTO"};
        float[] anchosTickets = {0.9f, 0.9f, 1.6f, 0.9f, 0.9f, 1.0f, 1.6f, 1.0f};
        int[] alignTickets = {Element.ALIGN_CENTER, Element.ALIGN_CENTER, Element.ALIGN_CENTER,
                Element.ALIGN_CENTER, Element.ALIGN_CENTER, Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_RIGHT};

        PdfPTable tablaTickets = EstilosReportePDF.crearTabla(anchosTickets, colsTickets, alignTickets);

        for (int i = 0; i < reporte.tickets().size(); i++) {
            var t = reporte.tickets().get(i);
            Color fondo = (i % 2 == 0) ? EstilosReportePDF.FILA_PAR : Color.WHITE;

            tablaTickets.addCell(EstilosReportePDF.celdaDato("#" + t.id(), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato("#" + t.idFuncion(), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato(t.compra() != null ? FECHA_HORA.format(t.compra().toLocalDateTime()) : "—",
                    Element.ALIGN_CENTER, fondo, EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato("Sala " + t.idSala(), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato(texto(t.asiento()), Element.ALIGN_CENTER, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato(texto(t.tipoAsiento()), Element.ALIGN_LEFT, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.GRIS)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato(texto(t.cajero()) + " (#" + t.idUsuario() + ")", Element.ALIGN_LEFT, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.NORMAL, EstilosReportePDF.AZUL)));
            tablaTickets.addCell(EstilosReportePDF.celdaDato(dinero(t.monto()), Element.ALIGN_RIGHT, fondo,
                    EstilosReportePDF.fuente(8.5f, Font.BOLD, EstilosReportePDF.AZUL)));
        }

        if (reporte.tickets().isEmpty()) {
            PdfPCell vacio = new PdfPCell(new Phrase("Esta película todavía no registra boletos vendidos.",
                    EstilosReportePDF.fuente(8.5f, Font.ITALIC, EstilosReportePDF.GRIS)));
            vacio.setColspan(8);
            vacio.setPadding(8);
            vacio.setBorder(Rectangle.BOTTOM);
            vacio.setBorderColor(EstilosReportePDF.BORDE);
            tablaTickets.addCell(vacio);
        }
        documento.add(tablaTickets);

        // 6. Resumen de taquilla al pie
        String subtituloResumen = "Total de entradas emitidas: " + reporte.totalTickets() + " boletos";
        documento.add(EstilosReportePDF.cajaResumen("RESUMEN DE TAQUILLA", subtituloResumen, dinero(reporte.totalIngresos())));
    }

    private static com.lowagie.text.Image poster(String ruta) {
        if (ruta == null || ruta.isBlank()) return null;
        try {
            BufferedImage imagen;
            String origen = ruta.trim();
            if (origen.regionMatches(true, 0, "https://", 0, 8)
                    || origen.regionMatches(true, 0, "http://", 0, 7)) {
                HttpURLConnection conexion = (HttpURLConnection) URI.create(origen).toURL().openConnection();
                conexion.setConnectTimeout(5000);
                conexion.setReadTimeout(5000);
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

    private static String ahora() {
        ZoneId zona = ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador"));
        return FECHA_HORA.format(LocalDateTime.now(zona));
    }

    private static String dinero(BigDecimal valor) {
        if (valor == null) return "$ 0.00";
        return "$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String texto(String valor) {
        return (valor == null || valor.isBlank()) ? "Sin especificar" : valor;
    }
}
