package views;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import models.Usuario;
import views.estilos.Tema;

/** Menú compartido del MDI: conserva los botones con sus eventos y permisos. */
final class BarraLateral extends JPanel {
    private final Map<MDI.Modulo, JButton> botones;

    BarraLateral(Map<MDI.Modulo, JButton> botones, JButton salir, Usuario usuario) {
        super(new BorderLayout(0, 24));
        this.botones = botones;
        setBackground(Tema.SUPERFICIE);
        setPreferredSize(new Dimension(230, 600));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDE),
                BorderFactory.createEmptyBorder(24, 16, 20, 16)));
        JPanel marca = columna();
        marca.add(Tema.texto("CINE", Tema.TITULO, Tema.PRIMARIO));
        marca.add(Box.createVerticalStrut(4));
        marca.add(Tema.texto("Sistema de gestión", Tema.CUERPO, Tema.SECUNDARIO));
        add(marca, BorderLayout.NORTH);

        JPanel menu = columna();
        seccion(menu, "OPERACIÓN");
        agregar(menu, MDI.Modulo.CARTELERA);
        agregar(menu, MDI.Modulo.TAQUILLA);
        // El cajero no ve la sección de administración.
        if (config.Sesion.esAdministrador(usuario)) {
            menu.add(Box.createVerticalStrut(20));
            seccion(menu, "ADMINISTRACIÓN");
            agregar(menu, MDI.Modulo.PELICULAS);
            agregar(menu, MDI.Modulo.SALAS);
            agregar(menu, MDI.Modulo.FUNCIONES);
            agregar(menu, MDI.Modulo.CORTE_CAJA);
            agregar(menu, MDI.Modulo.CONFIGURACION);
            agregar(menu, MDI.Modulo.USUARIOS);
        }
        JScrollPane scroll = new JScrollPane(menu);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Tema.SUPERFICIE);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);

        JPanel pie = columna();
        pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(16, 0, 0, 0)));
        JLabel nombre = Tema.texto(java.util.Objects.toString(usuario.getNombre(), "Usuario"), Tema.CUERPO.deriveFont(Font.BOLD), Tema.TEXTO);
        nombre.putClientProperty("html.disable", true);
        nombre.setToolTipText(nombre.getText());
        nombre.setMaximumSize(new Dimension(196, 24));
        pie.add(nombre); pie.add(Box.createVerticalStrut(4));
        pie.add(Tema.texto(usuario.getRol(), Tema.ETIQUETA, Tema.SECUNDARIO));
        pie.add(Box.createVerticalStrut(16));
        Tema.aplicarBoton(salir, false); salir.setForeground(Tema.PRIMARIO);
        salir.setAlignmentX(Component.LEFT_ALIGNMENT); salir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pie.add(salir); add(pie, BorderLayout.SOUTH);
    }

    private static JPanel columna() {
        JPanel panel = new JPanel(); panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS)); return panel;
    }
    private static void seccion(JPanel panel, String texto) {
        panel.add(Tema.texto(texto, Tema.ETIQUETA, Tema.SECUNDARIO));
        panel.add(Box.createVerticalStrut(10));
    }
    private void agregar(JPanel menu, MDI.Modulo modulo) {
        JButton boton = botones.get(modulo);
        boton.setUI(new BasicButtonUI());
        boton.setFont(Tema.CUERPO); boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        boton.setPreferredSize(new Dimension(194, 44));
        boton.setOpaque(true); boton.setFocusPainted(true); boton.setRolloverEnabled(true);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addChangeListener(e -> pintar(boton));
        pintar(boton); menu.add(boton); menu.add(Box.createVerticalStrut(6));
    }
    private static void pintar(JButton boton) {
        boolean activo = boton.isSelected();
        Color fondo = activo ? Tema.PRIMARIO : boton.getModel().isRollover() && boton.isEnabled() ? Tema.FONDO : Tema.SUPERFICIE;
        boton.setBackground(fondo);
        boton.setForeground(!boton.isEnabled() ? Tema.SECUNDARIO : activo ? Color.WHITE : Tema.TEXTO);
        boton.setFont(Tema.CUERPO.deriveFont(activo ? Font.BOLD : Font.PLAIN));
        boton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(fondo, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }
    void seleccionar(MDI.Modulo modulo) { botones.forEach((clave, boton) -> boton.setSelected(clave == modulo)); }
}
