package views;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import models.Asiento;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponsividadTest {
    private static void distribuir(Container c) {
        c.invalidate(); c.doLayout(); for (Component hijo:c.getComponents()) if (hijo instanceof Container n) distribuir(n);
    }
    private static void controles(Container c) {
        for (Component hijo:c.getComponents()) {
            if (hijo instanceof JButton || hijo instanceof JTextField || hijo instanceof JComboBox<?>) {
                if (hijo.isVisible()) {
                    assertTrue(hijo.getWidth()>0 && hijo.getHeight()>0, hijo.getClass()+" sin espacio");
                    assertTrue(hijo.getX()>=0 && hijo.getX()+hijo.getWidth()<=c.getWidth()+1,
                            hijo.getClass()+" fuera del ancho: "+hijo.getBounds()+" padre "+c.getSize());
                }
            }
            if (hijo instanceof Container n && hijo.isVisible()) controles(n);
        }
    }
    @Test void modulosMantienenControlesDentroDeSusPaneles() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (JPanel vista : List.of(new PeliculasView(),new TaquillaView(),new FuncionesView(),
                    new ConfiguracionView(),new CorteCajaView(),new SalasView(),new CarteleraView())) {
                for (int ancho : new int[]{1280,800,480,800,1280}) {
                    vista.setSize(ancho,520);
                    for (int i=0;i<12;i++) distribuir(vista);
                    try { controles(vista); } catch (AssertionError e) { throw new AssertionError(vista.getClass()+" ancho "+ancho,e); }
                    JScrollPane scroll=(JScrollPane)java.util.Arrays.stream(vista.getComponents()).filter(c -> c instanceof JScrollPane).findFirst().orElse(null);
                    if (scroll!=null && scroll.getViewport().getView() instanceof Scrollable) {
                        assertEquals(scroll.getViewport().getWidth(),scroll.getViewport().getView().getWidth());
                    }
                }
            }
        });
    }
    @Test void planoConservaPasillosYCoordenadasAlReducirAncho() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PlanoAsientosPanel plano=new PlanoAsientosPanel();
            Asiento a=new Asiento(1,1,"Normal","A",1,"Disponible"); a.setColumnaPlano(0); a.setFilaPlano(0);
            Asiento b=new Asiento(2,1,"Normal","B",2,"Averiado"); b.setColumnaPlano(3); b.setFilaPlano(2);
            JButton uno=new JButton("A-1"),dos=new JButton("B-2");
            plano.colocar(uno,a,0); plano.colocar(dos,b,1);
            JScrollPane scroll=new JScrollPane(plano);
            for(int ancho : new int[]{600,220,140,600}) {
                scroll.setSize(ancho,250); for(int i=0;i<5;i++) distribuir(scroll);
                assertTrue(dos.getX()-uno.getX()>=120); assertTrue(dos.getY()-uno.getY()>=80);
                assertTrue(uno.getWidth()>=34); assertTrue(dos.getX()+dos.getWidth()<=plano.getWidth());
            }
        });
    }
}
