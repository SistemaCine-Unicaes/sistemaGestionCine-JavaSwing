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

    private static List<String> etiquetas(Component componente) {
        List<String> resultado = new ArrayList<>();
        if (componente instanceof JLabel etiqueta) resultado.add(etiqueta.getText());
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) resultado.addAll(etiquetas(hijo));
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
                // El menú Administración existe pero el cajero no lo ve.
                JMenu administracion = mdi.getJMenuBar().getMenu(0);
                assertEquals("Administración", administracion.getText());
                assertFalse(administracion.isEnabled()); assertFalse(administracion.isVisible());
                List<AbstractButton> botones = botones(mdi);
                assertTrue(botones.stream().filter(b -> "Venta de boletos".equals(b.getText())).findFirst().orElseThrow().isEnabled());
                assertTrue(botones.stream().filter(b -> "Cartelera".equals(b.getText())).findFirst().orElseThrow().isEnabled());
                for (String modulo : List.of("Películas", "Salas", "Funciones", "Corte de caja", "Configuración", "Usuarios")) {
                    assertTrue(botones.stream().noneMatch(b -> modulo.equals(b.getText())), modulo + " no debe aparecer para el cajero");
                }
                assertTrue(etiquetas(mdi).stream().noneMatch("ADMINISTRACIÓN"::equals));
                assertTrue(etiquetas(mdi).contains("OPERACIÓN"));
                JMenu modulos = mdi.getJMenuBar().getMenu(1);
                List<String> visibles = new ArrayList<>();
                for (int i = 0; i < modulos.getItemCount(); i++) {
                    JMenuItem item = modulos.getItem(i);
                    if (item != null && item.isVisible()) visibles.add(item.getText());
                }
                assertEquals(List.of("Cartelera", "Venta de boletos", "Cerrar sesión"), visibles);
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
                String[] etiquetas = {"Cartelera", "Películas", "Salas", "Funciones", "Venta de boletos", "Corte de caja", "Configuración", "Usuarios"};
                MDI.Modulo[] modulos = {MDI.Modulo.CARTELERA, MDI.Modulo.PELICULAS, MDI.Modulo.SALAS, MDI.Modulo.FUNCIONES,
                        MDI.Modulo.TAQUILLA, MDI.Modulo.CORTE_CAJA, MDI.Modulo.CONFIGURACION, MDI.Modulo.USUARIOS};
                List<AbstractButton> controles = botones(mdi);
                for (int i = 0; i < etiquetas.length; i++) {
                    String etiqueta = etiquetas[i];
                    AbstractButton boton = controles.stream().filter(b -> b instanceof JButton && etiqueta.equals(b.getText()))
                            .findFirst().orElseThrow();
                    assertTrue(boton.isEnabled());
                    boton.doClick(); assertEquals(modulos[i], mdi.ultimo);
                    assertTrue(boton.isSelected());
                    assertEquals(1, controles.stream().filter(b -> b instanceof JButton && b.isSelected()).count());
                    assertTrue(mdi.getTitle().contains(modulos[i].getTitulo()));
                }
                JMenu menu = mdi.getJMenuBar().getMenu(0);
                assertTrue(menu.isVisible()); assertTrue(etiquetas(mdi).contains("ADMINISTRACIÓN"));
                menu.getItem(0).doClick(); assertEquals(MDI.Modulo.FUNCIONES, mdi.ultimo);
                menu.getItem(1).doClick(); assertEquals(MDI.Modulo.CORTE_CAJA, mdi.ultimo);
                menu.getItem(2).doClick(); assertEquals(MDI.Modulo.CONFIGURACION, mdi.ultimo);
                menu.getItem(3).doClick(); assertEquals(MDI.Modulo.USUARIOS, mdi.ultimo);
                assertTrue(mdi.getTitle().contains("Usuarios"));
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

    private static void distribuir(Container c) {
        c.invalidate(); c.doLayout();
        for(Component hijo:c.getComponents()) if(hijo instanceof Container n) distribuir(n);
    }

    @Test void mapaImpideConfirmarHuecosYPermiteCorregirLaSeleccion() throws Exception {
        SwingUtilities.invokeAndWait(()-> {
            MapaAsientosView mapa=new MapaAsientosView(null,true);
            try {
                List<Asiento> fila=new ArrayList<>();
                for(int n=1;n<=4;n++) fila.add(new Asiento(n*10,1,"Normal","A",n,"Disponible"));
                mapa.mostrarAsientos(fila,Set.of(10),2);
                List<AbstractButton> controles=botones(mapa);
                AbstractButton dos=controles.stream().filter(b->"A-2".equals(b.getText())).findFirst().orElseThrow();
                AbstractButton tres=controles.stream().filter(b->"A-3".equals(b.getText())).findFirst().orElseThrow();
                AbstractButton cuatro=controles.stream().filter(b->"A-4".equals(b.getText())).findFirst().orElseThrow();
                AbstractButton confirmar=controles.stream().filter(b->"Confirmar compra".equals(b.getText())).findFirst().orElseThrow();
                tres.doClick(); assertTrue(tres.isSelected()); assertFalse(confirmar.isEnabled());
                cuatro.doClick(); assertEquals(List.of(30,40),mapa.getAsientosSeleccionados()); assertFalse(confirmar.isEnabled());
                cuatro.doClick(); dos.doClick(); assertTrue(confirmar.isEnabled());
                assertEquals(List.of(20,30),mapa.getAsientosSeleccionados());
                dos.doClick(); assertFalse(confirmar.isEnabled());
                mapa.mostrarAsientos(fila,Set.of(),2); assertFalse(confirmar.isEnabled());
                assertTrue(mapa.getAsientosSeleccionados().isEmpty());
            } finally { mapa.dispose(); }
        });
    }

    @Test void loginRecuperaElEspacioDeLaPortadaYModalConservaPlanoGrande() throws Exception {
        SwingUtilities.invokeAndWait(()-> {
            Login login=new Login();
            try {
                for(int ancho:new int[]{480,1000,480}) {
                    login.getContentPane().setSize(ancho,400);
                    for(int i=0;i<12;i++) distribuir(login.getContentPane());
                    JButton entrar=login.getRootPane().getDefaultButton();
                    assertTrue(entrar.getWidth()>=250,"El formulario debe ocupar el ancho liberado por la portada");
                }
            } finally { login.dispose(); }
            MantenimientoSalaView modal=new MantenimientoSalaView(null);
            try {
                models.Sala sala=new models.Sala(180,4,25,15,"MANTENIMIENTO"); sala.setIdSala(6); sala.setMotivoInactividad("Proyector");
                List<Asiento> asientos=new ArrayList<>();
                for(int i=0;i<180;i++) {
                    Asiento a=new Asiento(i+1,6,"Normal",dao.AsientoDAO.nombreFila(i/15),i%15+1,i==0?"Averiado":"Disponible");
                    a.setColumnaPlano(i%15+(i%15>=10?2:i%15>=5?1:0)); a.setFilaPlano(i/15);
                    asientos.add(a);
                }
                modal.mostrar(sala,asientos); modal.getContentPane().setSize(480,400);
                for(int i=0;i<12;i++) distribuir(modal.getContentPane());
                List<AbstractButton> controles=botones(modal).stream().filter(b->b.getText()!=null && !b.getText().isEmpty()).toList();
                assertEquals(182,controles.size());
                assertTrue(controles.stream().allMatch(b->b.getWidth()>0 && b.getHeight()>0));
                assertTrue(controles.stream().anyMatch(b->"Reactivar sala".equals(b.getText())));
                assertTrue(controles.stream().filter(b->b.getText().contains("A-1<")).findFirst().orElseThrow().isEnabled());
            } finally { modal.dispose(); }
        });
    }

    @Test void horariosSonUnaPantallaDeCarteleraParaElCajero() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Usuario usuario = new Usuario(); usuario.setRol("Cajero"); Sesion.setUsuarioActual(usuario);
            class MDIPrueba extends MDI {
                models.Pelicula pelicula;
                Modulo ultimo;
                @Override protected void mostrarHorarios(models.Pelicula p) { pelicula = p; }
                @Override protected void mostrarModulo(Modulo modulo) { ultimo = modulo; }
            }
            MDIPrueba mdi = new MDIPrueba();
            try {
                models.Pelicula pelicula = new models.Pelicula(); pelicula.setIdPelicula(82); pelicula.setNombre("Órbita");
                mdi.abrirHorarios(pelicula);
                assertSame(pelicula, mdi.pelicula); assertTrue(mdi.getTitle().contains("Órbita"));
                assertTrue(botones(mdi).stream().filter(b -> "Cartelera".equals(b.getText())).findFirst().orElseThrow().isSelected());
                mdi.volverACartelera(); assertEquals(MDI.Modulo.CARTELERA, mdi.ultimo);
            } finally { mdi.dispose(); }
        });
    }

    @Test void loginPermiteMostrarYOcultarLaClaveSinCambiarLaAccionDeIngreso() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Login login = new Login();
            try {
                JCheckBox mostrar = (JCheckBox) botones(login).stream().filter(b -> b instanceof JCheckBox).findFirst().orElseThrow();
                JPasswordField clave = buscarClave(login);
                clave.setText("prueba-local"); assertNotEquals(0, clave.getEchoChar());
                mostrar.doClick(); assertEquals(0, clave.getEchoChar());
                login.limpiarPassword(); assertEquals(0, login.getPassword().length);
                assertFalse(mostrar.isSelected()); assertNotEquals(0, clave.getEchoChar());
                assertEquals(1, login.getRootPane().getDefaultButton().getActionListeners().length);
            } finally { login.dispose(); }
        });
    }

    private static JPasswordField buscarClave(Container raiz) {
        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof JPasswordField clave) return clave;
            if (hijo instanceof Container contenedor) {
                JPasswordField clave = buscarClave(contenedor); if (clave != null) return clave;
            }
        }
        return null;
    }
}
