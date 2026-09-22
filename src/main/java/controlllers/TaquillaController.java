package controlllers;

import config.Conexion;
import config.Sesion;
import dao.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import models.*;
import services.VentaService;
import utils.Tareas;
import views.*;

public class TaquillaController {
    private final MDI mdi;
    private final TaquillaView vista;
    private List<Pelicula> peliculas = List.of();
    private List<Funcion> funciones = List.of();
    private List<Funcion> visibles = List.of();
    private BigDecimal precio;
    private boolean cargando;
    private record Catalogo(List<Pelicula> peliculas, List<Funcion> funciones, BigDecimal precio) {}
    private record Mapa(List<Asiento> asientos, Set<Integer> vendidos) {}

    public TaquillaController(MDI mdi, TaquillaView vista) {
        Sesion.exigirVenta(); this.mdi = mdi; this.vista = vista;
        vista.habilitarContinuar(false);
        vista.addPeliculaChangeListener(e -> { if (!cargando) cargarFunciones(); });
        vista.addCantidadChangeListener(e -> Tareas.validar(vista, this::actualizarTotal));
        vista.addContinuarListener(e -> Tareas.validar(vista, this::abrirMapa));
        cargar();
    }

    private void cargar() {
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) {
                return new Catalogo(new PeliculaDAO(c).obtenerTodas(), new FuncionDAO(c).listarDisponibles(), new ConfiguracionDAO(c).obtenerPrecio());
            }
        }, datos -> {
            precio = datos.precio(); funciones = datos.funciones();
            Set<Integer> ids = funciones.stream().map(Funcion::getIdPelicula).collect(Collectors.toSet());
            peliculas = datos.peliculas().stream().filter(p -> ids.contains(p.getIdPelicula())).toList();
            cargando = true;
            vista.getCbPelicula().removeAllItems();
            for (Pelicula p : peliculas) vista.getCbPelicula().addItem(p.getNombre() + " (#" + p.getIdPelicula() + ")");
            cargando = false;
            cargarFunciones();
            if (precio == null) Tareas.error(vista, "El administrador debe guardar el precio en Administración → Configuración antes de vender.");
            else if (peliculas.isEmpty()) JOptionPane.showMessageDialog(vista, "No hay funciones futuras disponibles. El administrador puede programarlas desde Administración.");
        });
    }

    private void cargarFunciones() {
        int indice = vista.getCbPelicula().getSelectedIndex();
        visibles = indice < 0 ? List.of() : funciones.stream().filter(f -> f.getIdPelicula() == peliculas.get(indice).getIdPelicula()).toList();
        vista.getCbFuncion().removeAllItems();
        for (Funcion f : visibles) vista.getCbFuncion().addItem(f.getFechaProyeccion().toLocalDate().format(CorteCajaController.FECHA)
                + " " + f.getHoraInicio() + " · Sala " + f.getIdSala() + " (#" + f.getIdFuncion() + ")");
        actualizarTotal();
    }

    private void actualizarTotal() {
        int cantidad = vista.getCantidadBoletos();
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        vista.setTotalPagar(precio == null ? "$0.00" : "$" + precio.multiply(BigDecimal.valueOf(cantidad)).toPlainString());
        vista.habilitarContinuar(precio != null && !visibles.isEmpty());
    }

    private void abrirMapa() {
        Sesion.exigirVenta();
        int indice = vista.getCbFuncion().getSelectedIndex();
        if (indice < 0 || precio == null) throw new IllegalArgumentException("Selecciona una función y verifica el precio del boleto.");
        int cantidad = vista.getCantidadBoletos();
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        Funcion funcion = visibles.get(indice);
        Pelicula pelicula = peliculas.get(vista.getCbPelicula().getSelectedIndex());
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) {
                List<Asiento> asientos = new AsientoDAO(c).obtenerAsientosPorSala(funcion.getIdSala());
                Set<Integer> vendidos = new TicketDAO(c).obtenerTicketsPorFuncion(funcion.getIdFuncion()).stream()
                        .map(Ticket::getIdAsiento).collect(Collectors.toSet());
                return new Mapa(asientos, vendidos);
            }
        }, mapa -> {
            long libres = mapa.asientos().stream().filter(a -> "Disponible".equals(a.getEstado()) && !mapa.vendidos().contains(a.getIdAsiento())).count();
            if (cantidad > libres) { Tareas.error(vista, "Solo quedan " + libres + " asientos disponibles para esta función."); return; }
            MapaAsientosView dialogo = new MapaAsientosView(mdi, true);
            dialogo.setTitle(pelicula.getNombre() + " · Sala " + funcion.getIdSala() + " · Selecciona " + cantidad + " asientos");
            dialogo.mostrarAsientos(mapa.asientos(), mapa.vendidos(), cantidad);
            dialogo.addConfirmarListener(e -> Tareas.validar(dialogo, () -> vender(dialogo, funcion, pelicula, mapa, cantidad)));
            dialogo.setVisible(true);
        });
    }

    private void vender(MapaAsientosView dialogo, Funcion funcion, Pelicula pelicula, Mapa mapa, int cantidad) {
        List<Integer> seleccionados = dialogo.getAsientosSeleccionados();
        VentaService.validarSeleccion(seleccionados, cantidad);
        BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
        if (JOptionPane.showConfirmDialog(dialogo, "¿Confirmar la venta de " + cantidad + " boletos por $" + total.toPlainString() + "?",
                "Confirmar venta", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Tareas.ejecutar(dialogo, () -> new VentaService().vender(funcion.getIdFuncion(), seleccionados, cantidad, precio), tickets -> {
            dialogo.dispose();
            TicketReciboView recibo = new TicketReciboView(mdi, true);
            recibo.setTextoTicket(crearRecibo(pelicula, funcion, mapa.asientos(), tickets, Sesion.exigirSesion().getNombre()));
            recibo.setLocationRelativeTo(mdi); recibo.setVisible(true);
            cargar();
        });
    }

    public static String crearRecibo(Pelicula pelicula, Funcion funcion, List<Asiento> asientos, List<Ticket> tickets, String cajero) {
        Map<Integer, Asiento> porId = asientos.stream().collect(Collectors.toMap(Asiento::getIdAsiento, a -> a));
        StringBuilder texto = new StringBuilder("RECIBO DE VENTA\n\n");
        texto.append("Película: ").append(pelicula.getNombre()).append('\n');
        texto.append("Función: ").append(funcion.getIdFuncion()).append(" · Sala: ").append(funcion.getIdSala()).append('\n');
        texto.append("Fecha: ").append(funcion.getFechaProyeccion()).append(" · Hora: ").append(funcion.getHoraInicio()).append('\n');
        texto.append("Cajero: ").append(cajero).append("\n\n");
        BigDecimal total = BigDecimal.ZERO;
        for (Ticket t : tickets) {
            Asiento a = porId.get(t.getIdAsiento());
            texto.append("Ticket #").append(t.getIdTicket()).append("  Asiento ").append(a.getFila()).append('-').append(a.getNumero())
                    .append("  $").append(t.getMonto().toPlainString()).append('\n');
            total = total.add(t.getMonto());
        }
        if (!tickets.isEmpty()) texto.append("\nCompra: ").append(tickets.get(0).getFechaHoraCompra()).append('\n');
        return texto.append("TOTAL: $").append(total.toPlainString()).append('\n').toString();
    }
}
