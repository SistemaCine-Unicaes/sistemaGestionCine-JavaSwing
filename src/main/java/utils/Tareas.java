package utils;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import views.LoadingView;

/** La tarea no toca componentes Swing; el resultado se entrega en el hilo de Swing. */
public final class Tareas {
    private Tareas() {}

    public static <T> void ejecutar(Component vista, Callable<T> trabajo, Consumer<T> terminado) {
        Window ventana = vista instanceof Window w ? w : SwingUtilities.getWindowAncestor(vista);
        while (ventana != null && !(ventana instanceof Frame)) ventana = ventana.getOwner();
        LoadingView carga = new LoadingView((Frame) ventana, true);
        carga.setLocationRelativeTo(vista);
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override protected T doInBackground() throws Exception { return trabajo.call(); }
            @Override protected void done() {
                carga.dispose();
                try {
                    terminado.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    error(vista, "La operación fue interrumpida.");
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    java.util.logging.Logger.getLogger(Tareas.class.getName())
                            .log(java.util.logging.Level.WARNING, "Operación fallida", causa);
                    error(vista, causa.getMessage() == null ? "No se pudo completar la operación." : causa.getMessage());
                } catch (RuntimeException e) {
                    error(vista, e.getMessage() == null ? "No se pudo mostrar el resultado." : e.getMessage());
                }
            }
        };
        worker.execute();
        carga.setVisible(true);
    }

    public static void error(Component vista, String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "No se pudo completar", JOptionPane.ERROR_MESSAGE);
    }

    public static void validar(Component vista, Runnable accion) {
        try { accion.run(); }
        catch (IllegalArgumentException | IllegalStateException e) { error(vista, e.getMessage()); }
    }
}
