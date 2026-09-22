package services;

import config.Sesion;
import dao.AsientoDAO;
import dao.SalaDAO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import models.Sala;

public class SalaService {
    private final java.util.function.Supplier<java.sql.Connection> conexiones;
    public SalaService() { this(config.Conexion::getConexion); }
    public SalaService(java.util.function.Supplier<java.sql.Connection> conexiones) { this.conexiones = conexiones; }
    public static void validar(Sala sala) {
        if (sala.getCapacidadTotal() <= 0 || sala.getCapacidadTotal() > 5000
                || sala.getAsientosPorFila() <= 0 || sala.getAsientosPorFila() > sala.getCapacidadTotal()
                || sala.getAsientosEspeciales() < 0 || sala.getAsientosEspeciales() > sala.getCapacidadTotal()
                || sala.getTiempoDeLimpieza() < 0 || sala.getTiempoDeLimpieza() > 1440) {
            throw new IllegalArgumentException("Revisa la sala: capacidad de 1 a 5000, asientos por fila entre 1 y la capacidad, "
                    + "especiales entre 0 y la capacidad y limpieza entre 0 y 1440 minutos.");
        }
    }

    public void guardar(Sala sala) {
        Sesion.exigirAdministrador();
        validar(sala);
        Transacciones.ejecutar(conexiones, c -> {
            SalaDAO salas = new SalaDAO(c);
            AsientoDAO asientos = new AsientoDAO(c);
            if (sala.getIdSala() == 0) {
                sala.setEstado("ACTIVA");
                salas.registrarSala(sala);
                asientos.generarAsientos(sala);
            } else {
                Sala anterior = salas.bloquear(sala.getIdSala());
                if (anterior == null) throw new IllegalStateException("La sala ya no existe.");
                sala.setEstado(anterior.getEstado());
                boolean cambiaMapa = anterior.getCapacidadTotal() != sala.getCapacidadTotal()
                        || anterior.getAsientosPorFila() != sala.getAsientosPorFila()
                        || anterior.getAsientosEspeciales() != sala.getAsientosEspeciales()
                        || asientos.obtenerAsientosPorSala(sala.getIdSala()).size() != sala.getCapacidadTotal();
                if (cambiaMapa) {
                    try (PreparedStatement ps = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM Ticket t JOIN Asiento a USING(id_asiento) WHERE a.id_sala=?)")) {
                        ps.setInt(1, sala.getIdSala());
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getBoolean(1)) throw new IllegalStateException("La sala tiene boletos vendidos. No se puede cambiar la distribución de sus asientos.");
                        }
                    }
                    asientos.eliminarPorSala(sala.getIdSala());
                    asientos.generarAsientos(sala);
                }
                if (anterior.getTiempoDeLimpieza() < sala.getTiempoDeLimpieza()) {
                    try (PreparedStatement ps = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM Funcion WHERE id_sala=? AND estado IN ('Programada','en curso') AND fecha_proyeccion >= CURRENT_DATE)")) {
                        ps.setInt(1, sala.getIdSala());
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getBoolean(1)) throw new IllegalStateException("No se puede aumentar la limpieza mientras la sala tenga funciones pendientes; podría causar cruces de horarios.");
                        }
                    }
                }
                salas.actualizarSala(sala);
            }
            return null;
        });
    }
}
