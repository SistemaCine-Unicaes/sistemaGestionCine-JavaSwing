import config.Sesion;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import models.Asiento;
import models.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import views.*;
import static org.junit.jupiter.api.Assertions.*;

/** Construye los formularios y los libera sin mostrar ventanas ni consultar la base de datos. */
@EnabledIfSystemProperty(named = "cine.swing", matches = "true")
class SwingIntegrationTest {
    @AfterEach void limpiar() { Sesion.cerrarSesion(); }

    private static List<AbstractButton> botones(Component componente) {
        List<AbstractButton> resultado = new ArrayList<>();
        if (componente instanceof AbstractButton boton) resultado.add(boton);
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) resultado.addAll(botones(hijo));
        }
        return resultado;
    }

    @Test void loginTieneAccionYMDIReemplazaPanelesConPermisos() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Login login = new Login();
            try {
                assertNotNull(login.getRootPane().getDefaultButton());
                assertEquals(1, login.getRootPane().getDefaultButton().getActionListeners().length);
            } finally { login.dispose(); }
            Usuario usuario = new Usuario(); usuario.setRol("Cajero"); Sesion.setUsuarioActual(usuario);
            MDI mdi = new MDI();
            try {
                JPanel primero = new JPanel(), segundo = new JPanel();
                mdi.mostrarVistaCentral(primero); mdi.mostrarVistaCentral(segundo);
                assertNull(primero.getParent()); assertNotNull(segundo.getParent());
                assertInstanceOf(BorderLayout.class, segundo.getParent().getLayout());
                assertEquals(1, segundo.getParent().getComponentCount());
                assertFalse(mdi.getJMenuBar().getMenu(0).isEnabled());
                List<AbstractButton> botones = botones(mdi);
                assertFalse(botones.stream().filter(b -> "Peliculas".equals(b.getText())).findFirst().orElseThrow().isEnabled());
                assertTrue(botones.stream().filter(b -> "Venta Tickets".equals(b.getText())).findFirst().orElseThrow().isEnabled());
                for (String modulo : List.of("Funciones", "Corte de caja", "Configuración")) {
                    assertFalse(botones.stream().filter(b -> b instanceof JButton && modulo.equals(b.getText()))
                            .findFirst().orElseThrow().isEnabled());
                }
            } finally { mdi.dispose(); }
        });
    }

    @Test void accesosLateralesYMenuAdministrativoAbrenElMismoModulo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Usuario usuario = new Usuario(); usuario.setRol("Administrador"); Sesion.setUsuarioActual(usuario);
            // Aísla las consultas: comprueba los eventos reales de navegación sin conectarse a Supabase.
            class MDIPrueba extends MDI {
                Modulo ultimo;
                @Override protected void mostrarModulo(Modulo modulo) { ultimo = modulo; }
            }
            MDIPrueba mdi = new MDIPrueba();
            try {
                String[] etiquetas = {"Peliculas", "Salas", "Funciones", "Venta Tickets", "Corte de caja", "Configuración"};
                MDI.Modulo[] modulos = {MDI.Modulo.PELICULAS, MDI.Modulo.SALAS, MDI.Modulo.FUNCIONES,
                        MDI.Modulo.TAQUILLA, MDI.Modulo.CORTE_CAJA, MDI.Modulo.CONFIGURACION};
                List<AbstractButton> controles = botones(mdi);
                for (int i = 0; i < etiquetas.length; i++) {
                    String etiqueta = etiquetas[i];
                    AbstractButton boton = controles.stream().filter(b -> b instanceof JButton && etiqueta.equals(b.getText()))
                            .findFirst().orElseThrow();
                    assertTrue(boton.isEnabled());
                    boton.doClick(); assertEquals(modulos[i], mdi.ultimo);
                    assertTrue(mdi.getTitle().contains(modulos[i].getTitulo()));
                }
                JMenu menu = mdi.getJMenuBar().getMenu(0);
                menu.getItem(0).doClick(); assertEquals(MDI.Modulo.FUNCIONES, mdi.ultimo);
                menu.getItem(1).doClick(); assertEquals(MDI.Modulo.CORTE_CAJA, mdi.ultimo);
                menu.getItem(2).doClick(); assertEquals(MDI.Modulo.CONFIGURACION, mdi.ultimo);
            } finally { mdi.dispose(); }
        });
    }

    @Test void mapaBloqueaVendidosYAveriadosYDevuelveLosIdsReales() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapaAsientosView mapa = new MapaAsientosView(null, true);
            try {
                mapa.mostrarAsientos(List.of(new Asiento(100,1,"Normal","A",1,"Disponible"),
                        new Asiento(200,1,"Normal","A",2,"Disponible"),
                        new Asiento(300,1,"Especial","B",1,"Averiado")), Set.of(200), 1);
                List<AbstractButton> botones = botones(mapa);
                AbstractButton libre = botones.stream().filter(b -> "A-1".equals(b.getText())).findFirst().orElseThrow();
                AbstractButton vendido = botones.stream().filter(b -> "A-2".equals(b.getText())).findFirst().orElseThrow();
                AbstractButton averiado = botones.stream().filter(b -> "B-1".equals(b.getText())).findFirst().orElseThrow();
                AbstractButton confirmar = botones.stream().filter(b -> "Confirmar compra".equals(b.getText())).findFirst().orElseThrow();
                assertFalse(vendido.isEnabled()); assertFalse(averiado.isEnabled()); assertFalse(confirmar.isEnabled());
                libre.doClick(); assertEquals(List.of(100), mapa.getAsientosSeleccionados()); assertTrue(confirmar.isEnabled());
                libre.doClick(); assertTrue(mapa.getAsientosSeleccionados().isEmpty()); assertFalse(confirmar.isEnabled());
            } finally { mapa.dispose(); }
        });
    }
}
