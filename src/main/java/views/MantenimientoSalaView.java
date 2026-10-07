package views;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import models.Asiento;
import models.Sala;
import views.estilos.*;

/** Modal para habilitar/deshabilitar la sala o una butaca conservando su distribución. */
public class MantenimientoSalaView extends JDialog {
    @FunctionalInterface public interface Cambio { void aplicar(Integer asiento, boolean activar, String motivo); }
    private Cambio cambio = (a,b,c) -> {};
    private final PlanoAsientosPanel plano = new PlanoAsientosPanel();
    private final JLabel resumen = Tema.texto("",Tema.CUERPO,Tema.TEXTO);
    private final JTextArea motivoActual = new JTextArea();
    private final JButton estadoSala = Tema.botonSecundario("Desactivar sala");
    private Sala sala;

    public MantenimientoSalaView(Window parent) {
        super(parent,"Mantenimiento de sala",ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel contenido = new JPanel(new BorderLayout(12,12)); contenido.setBackground(Tema.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(16,16,16,16)); setContentPane(contenido);
        JPanel cabecera = new JPanel(new BorderLayout(0,8)); cabecera.setOpaque(false);
        cabecera.add(resumen,BorderLayout.NORTH);
        motivoActual.setLineWrap(true); motivoActual.setWrapStyleWord(true); motivoActual.setRows(2);
        motivoActual.setEditable(false); motivoActual.setOpaque(false); motivoActual.setFont(Tema.CUERPO);
        cabecera.add(motivoActual,BorderLayout.CENTER);
        JLabel pantalla = Tema.texto("P A N T A L L A",Tema.SUBTITULO,Tema.PRIMARIO);
        pantalla.setHorizontalAlignment(SwingConstants.CENTER);
        pantalla.setBorder(BorderFactory.createMatteBorder(0,0,3,0,Tema.PRIMARIO));
        cabecera.add(pantalla,BorderLayout.SOUTH); contenido.add(cabecera,BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(plano); scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(40); contenido.add(scroll,BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(0,12)); pie.setOpaque(false);
        JTextArea leyenda = new JTextArea("Blanco: activo · Gris / ×: desactivado · E: especial\nHaz clic en un asiento para cambiar su estado.");
        leyenda.setFont(Tema.CUERPO); leyenda.setLineWrap(true); leyenda.setWrapStyleWord(true);
        leyenda.setEditable(false); leyenda.setOpaque(false); leyenda.setRows(2); pie.add(leyenda,BorderLayout.NORTH);
        JPanel acciones = new JPanel(new RejillaAdaptable(2,180,12)); acciones.setOpaque(false);
        estadoSala.addActionListener(e -> solicitar(null,!"ACTIVA".equals(sala.getEstado()),"la sala completa"));
        JButton cerrar = Tema.botonPrimario("Cerrar"); cerrar.addActionListener(e -> dispose());
        acciones.add(estadoSala); acciones.add(cerrar); pie.add(acciones,BorderLayout.SOUTH); contenido.add(pie,BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(e -> dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        Adaptable.limitarVentana(this,980,720);
    }
    public void alCambiar(Cambio listener) { cambio = listener; }
    public void mostrar(Sala sala, List<Asiento> asientos) {
        this.sala = sala; setTitle("Sala " + sala.getIdSala() + " · " + sala.getNombre());
        long activos = asientos.stream().filter(a -> "Disponible".equals(a.getEstado())).count();
        resumen.setText(asientos.size()+" asientos · "+activos+" activos · "+(asientos.size()-activos)+" desactivados");
        boolean activa = "ACTIVA".equals(sala.getEstado());
        motivoActual.setText(activa ? "Sala activa" : "Sala desactivada: " + java.util.Objects.toString(sala.getMotivoInactividad(),sala.getEstado()));
        estadoSala.setText(activa ? "Desactivar sala" : "Reactivar sala");
        plano.removeAll(); java.util.Map<String,Integer> filas = new java.util.LinkedHashMap<>();
        for (Asiento asiento : asientos) {
            int fila = filas.computeIfAbsent(asiento.getFila(),k -> filas.size());
            boolean disponible = "Disponible".equals(asiento.getEstado());
            String etiqueta = asiento.getFila()+"-"+asiento.getNumero();
            JButton boton = new JButton(etiqueta);
            boton.setUI(new javax.swing.plaf.basic.BasicButtonUI()); boton.setOpaque(true);
            boton.setMargin(new Insets(0,0,0,0)); boton.setBackground(disponible ? Color.WHITE : Tema.BORDE);
            boton.setForeground(disponible ? Tema.TEXTO : Tema.SECUNDARIO);
            boolean especial = "Especial".equals(asiento.getTipoDeAsiento());
            boton.setText("<html><center>"+etiqueta+(disponible ? especial ? "<br>E" : "" : "<br>×")+"</center></html>");
            boton.setBorder(BorderFactory.createLineBorder(especial ? Tema.PRIMARIO : Tema.BORDE,especial ? 2 : 1));
            boton.setToolTipText(etiqueta+" · "+asiento.getTipoDeAsiento()+" · "+asiento.getEstado()
                    +(asiento.getMotivoInactividad()==null ? "" : " · "+asiento.getMotivoInactividad()));
            boton.getAccessibleContext().setAccessibleName(boton.getToolTipText());
            boton.addActionListener(e -> solicitar(asiento.getIdAsiento(),!disponible,"el asiento "+etiqueta));
            plano.colocar(boton,asiento,fila);
        }
        plano.revalidate(); plano.repaint();
    }
    private void solicitar(Integer asiento, boolean activar, String objeto) {
        String motivo = null;
        if (activar) {
            if (JOptionPane.showConfirmDialog(this,"¿Reactivar "+objeto+"?","Reactivar",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        } else {
            motivo = JOptionPane.showInputDialog(this,"Motivo para desactivar "+objeto+" (máximo 300 caracteres):","Mantenimiento",JOptionPane.QUESTION_MESSAGE);
            if (motivo==null) return;
        }
        cambio.aplicar(asiento,activar,motivo);
    }
}
