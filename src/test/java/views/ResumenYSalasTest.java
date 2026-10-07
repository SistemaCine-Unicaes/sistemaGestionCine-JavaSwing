package views;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import models.Sala;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ResumenYSalasTest {
    @TempDir Path temporal;

    private static <T> List<T> componentes(Component raiz, Class<T> tipo) {
        List<T> lista = new ArrayList<>();
        if (tipo.isInstance(raiz)) lista.add(tipo.cast(raiz));
        if (raiz instanceof Container contenedor) for (Component hijo : contenedor.getComponents()) lista.addAll(componentes(hijo, tipo));
        return lista;
    }
    private static void distribuir(Container c) {
        c.doLayout(); for (Component hijo : c.getComponents()) if (hijo instanceof Container n) distribuir(n);
    }

    @Test void posterSeActualizaSinOcultarTotalNiContinuar() throws Exception {
        Path archivo = temporal.resolve("poster.png");
        ImageIO.write(new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB), "png", archivo.toFile());
        CountDownLatch cargado = new CountDownLatch(1);
        AtomicReference<TaquillaView> referencia = new AtomicReference<>();
        AtomicReference<PosterView> poster = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            TaquillaView vista = new TaquillaView(); referencia.set(vista);
            vista.mostrarResumen("Órbita", "24/09/2026", "18:00", "Sala 7", 2, "$4.50");
            vista.setTotalPagar("$9.00"); vista.mostrarPoster(archivo.toString());
            PosterView imagen = componentes(vista, PosterView.class).getFirst(); poster.set(imagen);
            imagen.addPropertyChangeListener("icon", e -> cargado.countDown());
            vista.setSize(1050, 950); for (int i = 0; i < 5; i++) distribuir(vista);
            JButton continuar = componentes(vista, JButton.class).stream().filter(b -> b.getText().startsWith("Continuar a asientos")).findFirst().orElseThrow();
            JLabel total = componentes(vista, JLabel.class).stream().filter(l -> "$9.00".equals(l.getText())).findFirst().orElseThrow();
            assertTrue(continuar.getHeight() > 0); assertTrue(total.getHeight() > 0);
            assertTrue(continuar.getParent().getHeight() > 0); assertTrue(total.getParent().getHeight() > 0);
        });
        assertTrue(cargado.await(10, TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(() -> {
            assertEquals(195, poster.get().getIcon().getIconHeight());
            assertEquals(97, poster.get().getIcon().getIconWidth());
            referencia.get().mostrarPoster(null);
            PosterView vacio = componentes(referencia.get(), PosterView.class).getFirst();
            assertNotSame(poster.get(), vacio); assertNull(vacio.getIcon());
            assertEquals("Sin póster", vacio.getText());
        });
    }

    @Test void salasAbreLaSalaCorrectaSinOfrecerEditarDistribucion() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SalasView vista = new SalasView(); Sala sala = new Sala(120, 8, 15, 12, "ACTIVA"); sala.setIdSala(7);
            vista.mostrarSalas(List.of(sala));
            AtomicReference<Sala> abierta = new AtomicReference<>(); vista.alAbrir(abierta::set);
            componentes(vista,JButton.class).stream().filter(b -> b.getText().equals("Ver sala y asientos")).findFirst().orElseThrow().doClick();
            assertSame(sala,abierta.get());
            assertTrue(componentes(vista,JTextField.class).isEmpty());
            assertFalse(componentes(vista,JButton.class).stream().anyMatch(b -> b.getText().contains("Crear") || b.getText().contains("Guardar")));
        });
    }
}
