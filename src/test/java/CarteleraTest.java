import java.awt.Component;
import java.awt.Container;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import models.Funcion;
import models.Pelicula;
import org.junit.jupiter.api.Test;
import views.CarteleraView;
import static org.junit.jupiter.api.Assertions.*;

class CarteleraTest {
    private static <T> List<T> componentes(Component raiz, Class<T> tipo) {
        List<T> resultado = new ArrayList<>();
        if (tipo.isInstance(raiz)) resultado.add(tipo.cast(raiz));
        if (raiz instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) resultado.addAll(componentes(hijo, tipo));
        }
        return resultado;
    }

    private static Pelicula pelicula(int id, String titulo, String estado) {
        return new Pelicula(id, titulo, "Sinopsis de prueba", 120, "Drama", "Dirección",
                null, null, null, estado);
    }

    private static JButton boton(CarteleraView vista, String texto) {
        return componentes(vista, JButton.class).stream().filter(b -> texto.equals(b.getText())).findFirst().orElseThrow();
    }

    @Test void filtraLosPosteresYAbreLosHorariosDeLaPeliculaElegida() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            CarteleraView vista = new CarteleraView();
            vista.mostrarCatalogo(List.of(pelicula(9, "Órbita", "CARTELERA"), pelicula(15, "Mar", "CARTELERA"),
                    pelicula(20, "Archivo", "ARCHIVADA"), pelicula(25, "Próximo estreno", "PROXIMAMENTE")));
            JTextField filtro = componentes(vista, JTextField.class).getFirst();
            assertEquals(2, componentes(vista, JButton.class).stream().filter(b -> "Ver horarios".equals(b.getText())).count());
            filtro.setText(" ORBITA ");
            assertEquals(1, componentes(vista, JButton.class).stream().filter(b -> "Ver horarios".equals(b.getText())).count());
            AtomicInteger elegida = new AtomicInteger(-1);
            vista.setHorariosListener(p -> elegida.set(p.getIdPelicula()));
            boton(vista, "Ver horarios").doClick();
            assertEquals(9, elegida.get());
            assertTrue(componentes(vista, JTable.class).isEmpty());
            assertTrue(componentes(vista, JButton.class).stream().noneMatch(b -> "Comprar boletos".equals(b.getText())));
            filtro.setText("mar");
            boton(vista, "Ver horarios").doClick();
            assertEquals(15, elegida.get());
            filtro.setText("inexistente");
            assertTrue(componentes(vista, JLabel.class).stream().anyMatch(l -> l.getText().contains("No se encontraron")));
            vista.setCargando(true);
            assertFalse(filtro.isEnabled());
            assertFalse(boton(vista, "Actualizar cartelera").isEnabled());
            vista.setCargando(false); vista.mostrarErrorCarga();
            assertTrue(boton(vista, "Actualizar cartelera").isEnabled());
        });
    }

    @Test void horariosSeparanPeliculasFechasYSalasYEntreganElIdReal() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            views.HorariosPeliculaView vista = new views.HorariosPeliculaView(pelicula(9, "Órbita", "CARTELERA"));
            List<Funcion> funciones = List.of(
                    new Funcion(803,9,7,Date.valueOf("2027-01-02"),Time.valueOf("18:30:00"),Time.valueOf("20:30:00"),"Programada"),
                    new Funcion(804,9,2,Date.valueOf("2027-01-02"),Time.valueOf("20:30:00"),Time.valueOf("22:30:00"),"Programada"),
                    new Funcion(805,9,7,Date.valueOf("2027-02-10"),Time.valueOf("23:00:00"),Time.valueOf("01:00:00"),"Programada"),
                    new Funcion(806,15,7,Date.valueOf("2027-01-02"),Time.valueOf("10:00:00"),Time.valueOf("12:00:00"),"Programada"));
            vista.mostrarFunciones(funciones);
            var horas = componentes(vista, JButton.class).stream().filter(b -> b.getClientProperty("idFuncion") != null).toList();
            assertEquals(2, horas.size());
            assertTrue(horas.stream().noneMatch(b -> Integer.valueOf(806).equals(b.getClientProperty("idFuncion"))));
            assertTrue(componentes(vista, JLabel.class).stream().anyMatch(l -> "Sala 2".equals(l.getText())));
            AtomicInteger elegida = new AtomicInteger(-1); vista.setComprarListener(elegida::set);
            horas.stream().filter(b -> Integer.valueOf(803).equals(b.getClientProperty("idFuncion"))).findFirst().orElseThrow().doClick();
            assertEquals(803, elegida.get());
            JButton fecha = componentes(vista, JButton.class).stream().filter(b -> b.getText().contains("10/02/2027")).findFirst().orElseThrow();
            fecha.doClick();
            assertTrue(fecha.isSelected()); assertEquals(views.estilos.Tema.PRIMARIO, fecha.getBackground());
            horas = componentes(vista, JButton.class).stream().filter(b -> b.getClientProperty("idFuncion") != null).toList();
            assertEquals(1, horas.size()); horas.getFirst().doClick(); assertEquals(805, elegida.get());
            assertTrue(componentes(vista, JLabel.class).stream().anyMatch(l -> l.getText().contains("(+1 día)")));
            vista.setCargando(true);
            assertTrue(componentes(vista, JButton.class).stream().noneMatch(b -> b.getClientProperty("idFuncion") != null));
            vista.setCargando(false); vista.mostrarFunciones(List.of());
            assertTrue(componentes(vista, JLabel.class).stream().anyMatch(l -> l.getText().contains("todavía no tiene funciones")));
            vista.mostrarErrorCarga();
            assertTrue(componentes(vista, JButton.class).stream().filter(b -> "Actualizar horarios".equals(b.getText())).findFirst().orElseThrow().isEnabled());
        });
    }
}
