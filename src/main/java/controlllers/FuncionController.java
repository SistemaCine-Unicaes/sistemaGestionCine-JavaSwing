package controlllers;

import config.Conexion;
import config.Sesion;
import dao.PeliculaDAO;
import dao.SalaDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JOptionPane;
import models.Funcion;
import models.Pelicula;
import models.Sala;
import services.FuncionService;
import utils.Tareas;
import views.FuncionesView;

public final class FuncionController {
    private final FuncionesView vista;
    private List<Pelicula> peliculas = List.of();
    private List<Sala> salas = List.of();
    private record Catalogo(List<Pelicula> peliculas, List<Sala> salas) {}

    public FuncionController(FuncionesView vista) {
        Sesion.exigirAdministrador();
        this.vista = vista;
        vista.setFecha(LocalDate.now(java.time.ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA",
                "America/El_Salvador"))).format(CorteCajaController.FECHA));
        vista.addProgramarListener(e -> Tareas.validar(vista, this::programar));
        vista.addRecargarListener(e -> Tareas.validar(vista, this::cargar));
        cargar();
    }

    private void cargar() {
        Sesion.exigirAdministrador();
        vista.habilitarProgramar(false);
        Tareas.ejecutar(vista, () -> {
            try (Connection c = Conexion.getConexion()) {
                return new Catalogo(new PeliculaDAO(c).obtenerTodas().stream()
                        .filter(p -> "CARTELERA".equals(p.getEstado())).toList(),
                        new SalaDAO(c).listarSalas().stream().filter(s -> "ACTIVA".equals(s.getEstado())).toList());
            }
        }, catalogo -> {
            peliculas = catalogo.peliculas(); salas = catalogo.salas();
            vista.mostrarOpciones(peliculas, salas);
            boolean disponibles = !peliculas.isEmpty() && !salas.isEmpty();
            vista.habilitarProgramar(disponibles);
            if (!disponibles) Tareas.error(vista, "Primero registra una película en cartelera y una sala activa.");
        });
    }

    private void programar() {
        Sesion.exigirAdministrador();
        int indicePelicula = vista.getPeliculaSeleccionada();
        int indiceSala = vista.getSalaSeleccionada();
        if (indicePelicula < 0 || indiceSala < 0) throw new IllegalArgumentException("Selecciona una película y una sala.");
        Funcion funcion = new Funcion();
        try {
            funcion.setFechaProyeccion(Date.valueOf(LocalDate.parse(vista.getFecha(), CorteCajaController.FECHA)));
            funcion.setHoraInicio(Time.valueOf(LocalTime.parse(vista.getHora(),
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                            .withResolverStyle(java.time.format.ResolverStyle.STRICT))));
        } catch (java.time.DateTimeException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Revisa la fecha y la hora. Ejemplo: 15/09/2026 y 18:00.");
        }
        funcion.setIdPelicula(peliculas.get(indicePelicula).getIdPelicula());
        funcion.setIdSala(salas.get(indiceSala).getIdSala());
        Tareas.ejecutar(vista, () -> { new FuncionService().programar(funcion); return funcion; }, creada ->
                JOptionPane.showMessageDialog(vista, "Función " + creada.getIdFuncion()
                        + " programada hasta las " + creada.getHoraFin() + "."));
    }
}