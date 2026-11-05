package views.estilos;

import java.awt.*;
import java.awt.font.*;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;

/** Envuelve etiquetas de texto sin alterar su valor, eventos ni accesibilidad. */
public final class TextoAdaptableUI extends BasicLabelUI {
    public static void aplicar(JLabel etiqueta) {
        etiqueta.setUI(new TextoAdaptableUI());
        etiqueta.addComponentListener(new java.awt.event.ComponentAdapter() {
            private int ancho;
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (ancho != etiqueta.getWidth()) { ancho=etiqueta.getWidth(); etiqueta.revalidate(); }
            }
        });
    }
    private boolean admite(JLabel l) {
        return l.getIcon()==null && l.getText()!=null && !l.getText().isEmpty() && !l.getText().startsWith("<html>");
    }
    private List<TextLayout> lineas(JLabel l) {
        Insets i=l.getInsets(); int ancho=Math.max(1,l.getWidth()-i.left-i.right);
        AttributedString texto=new AttributedString(l.getText()); texto.addAttribute(TextAttribute.FONT,l.getFont());
        LineBreakMeasurer medidor=new LineBreakMeasurer(texto.getIterator(),l.getFontMetrics(l.getFont()).getFontRenderContext());
        List<TextLayout> lineas=new ArrayList<>();
        while(medidor.getPosition()<l.getText().length()) lineas.add(medidor.nextLayout(ancho));
        return lineas;
    }
    @Override public Dimension getPreferredSize(JComponent c) {
        Dimension d=super.getPreferredSize(c); JLabel l=(JLabel)c;
        if(admite(l) && l.getWidth()>0) {
            Insets i=l.getInsets(); d.height=lineas(l).size()*l.getFontMetrics(l.getFont()).getHeight()+i.top+i.bottom;
        }
        return d;
    }
    @Override public void paint(Graphics graphics,JComponent c) {
        JLabel l=(JLabel)c;
        if(!admite(l)) { super.paint(graphics,c); return; }
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(l.isEnabled()?l.getForeground():Tema.SECUNDARIO);
        Insets i=l.getInsets(); int alto=l.getFontMetrics(l.getFont()).getHeight();
        List<TextLayout> lineas=lineas(l); float y=i.top;
        if(l.getVerticalAlignment()==SwingConstants.CENTER) y+=Math.max(0,(l.getHeight()-i.top-i.bottom-lineas.size()*alto)/2f);
        for(TextLayout linea:lineas) {
            float x=i.left;
            if(l.getHorizontalAlignment()==SwingConstants.CENTER) x+=Math.max(0,(l.getWidth()-i.left-i.right-linea.getAdvance())/2);
            else if(l.getHorizontalAlignment()==SwingConstants.RIGHT || l.getHorizontalAlignment()==SwingConstants.TRAILING)
                x=Math.max(i.left,l.getWidth()-i.right-linea.getAdvance());
            linea.draw(g,x,y+linea.getAscent()); y+=alto;
        }
        g.dispose();
    }
}
