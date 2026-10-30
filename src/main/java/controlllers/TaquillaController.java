package controlllers;

import config.Conexion;
import config.Sesion;
import dao.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import models.*;
import services.VentaService;
import utils.Tareas;
import views.*;

public class TaquillaController {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter COMPRA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private final MDI mdi;
    private final TaquillaView vista;
    private List<Pelicula> peliculas = List.of();
    private List<Funcion> funciones = List.of();
    private List<Funcion> visibles = List.of();
    private ConfiguracionDAO.Precios precios;
    private boolean cargando;
    private record Catalogo(List<Pelicula> peliculas, List<Funcion> funciones, ConfiguracionDAO.Precios precios) {}
    private record Mapa(List<Asiento> asientos, Set<Integer> vendidos) {}

    public TaquillaController(MDI mdi, TaquillaView vista) {
        this(mdi, vista, null);
    }

    public TaquillaController(MDI mdi, TaquillaView vista, Integer idFuncion) {
        Sesion.exigirVenta(); this.mdi = mdi; this.vista = vista;
        this.funcionInicial = idFuncion;
        vista.habilitarContinuar(false);
        vista.addPeliculaChangeListener(e -> { if (!cargando) cargarFunciones(); });
        vista.addFuncionChangeListener(e -> { if (!cargando) Tareas.validar(vista, this::actualizarTotal); });
        vista.addCantidadesChangeListener(e -> Tareas.validar(vista, this::actualizarTotal));
        vista.addContinuarListener(e -> Tareas.validar(vista, this::abrirMapa));
        cargar();
    }

    private void cargar() {
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) {
                return new Catalogo(new PeliculaDAO(c).obtenerTodas(), new FuncionDAO(c).listarDisponibles(), new ConfiguracionDAO(c).obtenerPrecios());
            }
        }, datos -> {
            precios = datos.precios(); 
            funciones = datos.funciones();
            Set<Integer> ids = funciones.stream().map(Funcion::getIdPelicula).collect(Collectors.toSet());
            peliculas = datos.peliculas().stream().filter(p -> ids.contains(p.getIdPelicula())).toList();
            cargando = true;
            vista.getCbPelicula().removeAllItems();
            for (Pelicula p : peliculas) vista.getCbPelicula().addItem(p.getNombre() + " (#" + p.getIdPelicula() + ")");
            cargando = false;
            cargarFunciones();
            if (precios == null || !precios.estanCompletos()) {
                Tareas.error(vista, "El administrador debe guardar las tarifas en Administración → Configuración antes de vender.");
            } else if (peliculas.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "No hay funciones futuras disponibles. El administrador puede programarlas desde Administración.");
            }
        });
    }

    private void seleccionarFuncionInicial(int idFuncion) {
        Funcion elegida = funciones.stream().filter(f -> f.getIdFuncion() == idFuncion).findFirst().orElse(null);
        if (elegida == null) {
            vista.getCbFuncion().setSelectedIndex(-1);
            Tareas.error(vista, "La función seleccionada ya no está disponible. Elige otro horario.");
            return;
        }
        for (int i = 0; i < peliculas.size(); i++) {
            if (peliculas.get(i).getIdPelicula() == elegida.getIdPelicula()) {
                vista.getCbPelicula().setSelectedIndex(i);
                for (int j = 0; j < visibles.size(); j++) {
                    if (visibles.get(j).getIdFuncion() == idFuncion) {
                        vista.getCbFuncion().setSelectedIndex(j);
                        return;
                    }
                }
            }
        }
    }

    private void cargarFunciones() {
        int indice = vista.getCbPelicula().getSelectedIndex();
        visibles = indice < 0 ? List.of() : funciones.stream().filter(f -> f.getIdPelicula() == peliculas.get(indice).getIdPelicula()).toList();
        cargando = true;
        vista.getCbFuncion().removeAllItems();
        for (Funcion f : visibles) vista.getCbFuncion().addItem(f.getFechaProyeccion().toLocalDate().format(CorteCajaController.FECHA)
                + " · " + hora(f) + " · Sala " + f.getIdSala() + " (#" + f.getIdFuncion() + ")");
        cargando = false;
        actualizarTotal();
    }

    private void actualizarTotal() {
        int cantAdulto = vista.getCantidadAdulto();
        int cantNino = vista.getCantidadNino();
        int cantMayor = vista.getCantidadAdultoMayor();
        int totalBoletos = cantAdulto + cantNino + cantMayor;

        BigDecimal totalPagar = BigDecimal.ZERO;
        String desgloseBoletos = totalBoletos + " (" + cantAdulto + " Ad, " + cantNino + " Niñ, " + cantMayor + " May)";
        String desglosePrecios = null;

        if (precios != null && precios.estanCompletos()) {
            BigDecimal subAdulto = precios.adulto().multiply(BigDecimal.valueOf(cantAdulto));
            BigDecimal subNino = precios.nino().multiply(BigDecimal.valueOf(cantNino));
            BigDecimal subMayor = precios.adultoMayor().multiply(BigDecimal.valueOf(cantMayor));
            totalPagar = subAdulto.add(subNino).add(subMayor);
            desglosePrecios = "$" + precios.adulto().toPlainString() + " / $" + precios.nino().toPlainString() + " / $" + precios.adultoMayor().toPlainString();
        }

        vista.setTotalPagar("$" + totalPagar.toPlainString());

        int indicePelicula = vista.getCbPelicula().getSelectedIndex();
        int indiceFuncion = vista.getCbFuncion().getSelectedIndex();
        Funcion f = indiceFuncion < 0 ? null : visibles.get(indiceFuncion);

        vista.mostrarResumen(indicePelicula < 0 ? null : peliculas.get(indicePelicula).getNombre(),
                f == null ? null : f.getFechaProyeccion().toLocalDate().format(CorteCajaController.FECHA),
                f == null ? null : hora(f), f == null ? null : "Sala " + f.getIdSala(),
                desgloseBoletos, desglosePrecios);

        vista.habilitarContinuar(precios != null && precios.estanCompletos() && f != null && totalBoletos > 0);

        if (f != null) {
            vista.setDisponibilidad("Calculando...");
            Tareas.ejecutar(vista, () -> {
                try (Connection c = Conexion.getConexion()) {
                    List<Asiento> asientos = new AsientoDAO(c).obtenerAsientosPorSala(f.getIdSala());
                    Set<Integer> vendidos = new TicketDAO(c).obtenerTicketsPorFuncion(f.getIdFuncion()).stream()
                            .map(Ticket::getIdAsiento).collect(Collectors.toSet());
                    return new Mapa(asientos, vendidos);
                }
            }, mapa -> {
                long libres = mapa.asientos().stream().filter(a -> "Disponible".equals(a.getEstado()) && !mapa.vendidos().contains(a.getIdAsiento())).count();
                vista.setDisponibilidad(libres + " asientos disponibles");
            });
        } else {
            vista.setDisponibilidad(" ");
        }
    }

    private static String hora(Funcion f) {
        return f.getHoraInicio().toLocalTime().format(HORA);
    }

    private void abrirMapa() {
        Sesion.exigirVenta();
        int indice = vista.getCbFuncion().getSelectedIndex();
        if (indice < 0 || precios == null || !precios.estanCompletos()) {
            throw new IllegalArgumentException("Selecciona una función y verifica los precios de los boletos.");
        }
        int cantidad = vista.getCantidadTotalBoletos();
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Debes seleccionar al menos un boleto.");
        }
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

        int cantAdulto = vista.getCantidadAdulto();
        int cantNino = vista.getCantidadNino();
        int cantMayor = vista.getCantidadAdultoMayor();
        BigDecimal subAdulto = precios.adulto().multiply(BigDecimal.valueOf(cantAdulto));
        BigDecimal subNino = precios.nino().multiply(BigDecimal.valueOf(cantNino));
        BigDecimal subMayor = precios.adultoMayor().multiply(BigDecimal.valueOf(cantMayor));
        BigDecimal total = subAdulto.add(subNino).add(subMayor);

        if (JOptionPane.showConfirmDialog(dialogo, "¿Confirmar la venta de " + cantidad + " boletos por $" + total.toPlainString() + "?",
                "Confirmar venta", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        Tareas.ejecutar(dialogo, () -> new VentaService().vender(funcion.getIdFuncion(), seleccionados, cantAdulto, cantNino, cantMayor, precios), tickets -> {
            dialogo.dispose();
            TicketReciboView recibo = new TicketReciboView(mdi, true);
            recibo.mostrarTicket(datosRecibo(pelicula, funcion, mapa.asientos(), tickets, Sesion.exigirSesion().getNombre()));
            recibo.setLocationRelativeTo(mdi); 
            recibo.setVisible(true);
            cargar();
        });
    }

    public static String crearRecibo(Pelicula pelicula, Funcion funcion, List<Asiento> asientos, List<Ticket> tickets, String cajero) {
        return datosRecibo(pelicula, funcion, asientos, tickets, cajero).comoTexto();
    }

    public static TicketReciboView.Datos datosRecibo(Pelicula pelicula, Funcion funcion, List<Asiento> asientos, List<Ticket> tickets, String cajero) {
        Map<Integer, Asiento> porId = asientos.stream().collect(Collectors.toMap(Asiento::getIdAsiento, a -> a));
        List<TicketReciboView.Linea> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        ConfiguracionDAO.Precios p;
        try (Connection c = Conexion.getConexion()) {
            p = new ConfiguracionDAO(c).obtenerPrecios();
        } catch (Exception e) {
            p = null;
        }

        for (Ticket t : tickets) {
            Asiento a = porId.get(t.getIdAsiento());
            String nombreAsiento = a != null ? a.getFila() + "-" + a.getNumero() : "—";

            String tipo = "General";
            if (p != null) {
                if (p.adulto() != null && t.getMonto().compareTo(p.adulto()) == 0) {
                    tipo = "Adulto";
                } else if (p.nino() != null && t.getMonto().compareTo(p.nino()) == 0) {
                    tipo = "Niño";
                } else if (p.adultoMayor() != null && t.getMonto().compareTo(p.adultoMayor()) == 0) {
                    tipo = "Adulto Mayor";
                }
            }

            lineas.add(new TicketReciboView.Linea(t.getIdTicket(), nombreAsiento, t.getMonto().toPlainString(), tipo));
            total = total.add(t.getMonto());
        }

        String compra = tickets.isEmpty() || tickets.get(0).getFechaHoraCompra() == null ? "—"
                : tickets.get(0).getFechaHoraCompra().toLocalDateTime().format(COMPRA);

        return new TicketReciboView.Datos(pelicula.getNombre(), funcion.getFechaProyeccion().toLocalDate().format(CorteCajaController.FECHA),
                hora(funcion), funcion.getIdSala(), funcion.getIdFuncion(), cajero, compra, lineas, total.toPlainString());
    }
}