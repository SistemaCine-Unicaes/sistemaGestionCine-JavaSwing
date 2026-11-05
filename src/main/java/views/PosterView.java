package views;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.*;
import views.estilos.Tema;

/** Carga pósteres locales o HTTP sin bloquear el hilo de Swing. */
final class PosterView extends JLabel {
    PosterView(String ruta) {
        this(ruta, 140, 195);
    }

    PosterView(String ruta, int ancho, int alto) {
        super("Sin póster", SwingConstants.CENTER);
        setOpaque(true); setBackground(Tema.FONDO); setForeground(Tema.SECUNDARIO);
        setFont(Tema.CUERPO); setPreferredSize(new Dimension(ancho, alto));
        if (ruta == null || ruta.isBlank()) return;
        setText("Cargando póster…");
        new SwingWorker<ImageIcon, Void>() {
            @Override protected ImageIcon doInBackground() throws Exception {
                BufferedImage imagen;
                String origen = ruta.trim();
                if (origen.regionMatches(true, 0, "https://", 0, 8)
                        || origen.regionMatches(true, 0, "http://", 0, 7)) {
                    HttpURLConnection conexion = (HttpURLConnection) URI.create(origen).toURL().openConnection();
                    conexion.setConnectTimeout(5000); conexion.setReadTimeout(5000);
                    conexion.setRequestProperty("User-Agent", "Mozilla/5.0");
                    try (InputStream entrada = conexion.getInputStream()) { imagen = ImageIO.read(entrada); }
                    finally { conexion.disconnect(); }
                } else {
                    try (InputStream entrada = Files.newInputStream(Path.of(origen))) { imagen = ImageIO.read(entrada); }
                }
                if (imagen == null) return null;
                double escala = Math.min((double) ancho / imagen.getWidth(), (double) alto / imagen.getHeight());
                return new ImageIcon(imagen.getScaledInstance(Math.max(1, (int) (imagen.getWidth() * escala)),
                        Math.max(1, (int) (imagen.getHeight() * escala)), Image.SCALE_SMOOTH));
            }
            @Override protected void done() {
                try {
                    ImageIcon icono = get();
                    setIcon(icono); setText(icono == null ? "Imagen no disponible" : "");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); setText("Imagen no disponible");
                } catch (java.util.concurrent.ExecutionException e) { setText("Imagen no disponible"); }
            }
        }.execute();
    }
}
