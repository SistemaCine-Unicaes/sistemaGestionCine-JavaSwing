package controlllers;

import java.sql.Date;
import javax.swing.SwingUtilities;
import models.Pelicula;
import org.junit.jupiter.api.Test;
import views.PeliculasView;
import static org.junit.jupiter.api.Assertions.*;

class PeliculasFormularioTest {
    @Test void fichaIncluyePosterEstrenoYEstadoElegidoAlCrearYEditar() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PeliculasView vista = new PeliculasView();
            vista.setTitulo("  Órbita  "); vista.setDuracion("120"); vista.setSinopsis("Una expedición");
            vista.setGenero("Ciencia ficción"); vista.setDirector("Ana López");
            vista.setFechaEstreno("29/02/2028"); vista.setTipoEstreno("MUNDIAL");
            vista.setImagenUrl("https://example.com/poster.jpg"); vista.setEstado("PROXIMAMENTE");
            Pelicula nueva = PeliculasController.leerFormulario(vista, 0);
            assertEquals("Órbita", nueva.getNombre()); assertEquals(0, nueva.getIdPelicula());
            assertEquals(Date.valueOf("2028-02-29"), nueva.getFechaEstreno());
            assertEquals("MUNDIAL", nueva.getTipoEstreno()); assertEquals("PROXIMAMENTE", nueva.getEstado());
            assertEquals("https://example.com/poster.jpg", nueva.getImagenUrl());
            vista.setTipoEstreno("Estreno heredado");
            assertEquals("Estreno heredado", PeliculasController.leerFormulario(vista, 83).getTipoEstreno());
            vista.setFechaEstreno(""); vista.setImagenUrl(""); vista.setTipoEstreno(null);
            Pelicula editada = PeliculasController.leerFormulario(vista, 83);
            assertEquals(83, editada.getIdPelicula());
            assertNull(editada.getFechaEstreno()); assertNull(editada.getImagenUrl()); assertNull(editada.getTipoEstreno());
        });
    }

    @Test void rechazaDatosInvalidosAntesDeGuardar() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PeliculasView vista = new PeliculasView();
            assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
            vista.setTitulo("Película");
            for (String duracion : new String[]{"", "abc", "0", "-1", "1440", "12.5"}) {
                vista.setDuracion(duracion);
                assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
            }
            vista.setDuracion("90");
            for (String fecha : new String[]{"31/02/2026", "29/02/2027", "24/13/2026", "2026-09-24", "01/01/0000"}) {
                vista.setFechaEstreno(fecha);
                assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
            }
            vista.setFechaEstreno("24/09/2026");
            vista.setImagenUrl("https://");
            assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
            vista.setImagenUrl("src/assets/posters/83_poster.png");
            assertEquals("src/assets/posters/83_poster.png", PeliculasController.leerFormulario(vista, 83).getImagenUrl());
            vista.setTitulo("a".repeat(151));
            assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
            vista.setTitulo("Película"); vista.setGenero("a".repeat(51));
            assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
            vista.setGenero("Drama"); vista.setDirector("a".repeat(101));
            assertThrows(IllegalArgumentException.class, () -> PeliculasController.leerFormulario(vista, 0));
        });
    }
}
