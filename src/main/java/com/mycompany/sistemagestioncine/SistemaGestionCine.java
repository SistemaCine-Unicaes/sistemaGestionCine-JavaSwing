package com.mycompany.sistemagestioncine;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import views.Login;

public class SistemaGestionCine {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
                java.util.logging.Logger.getLogger(SistemaGestionCine.class.getName())
                        .log(java.util.logging.Level.WARNING, "No se pudo cargar Nimbus", e);
            }
            new Login().setVisible(true);
        });
    }
}