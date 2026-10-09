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
                if (anterior.getCodigoPlano() != null) throw new IllegalStateException("La distribución de una sala predefinida no se puede editar.");
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

    public static String validarMotivo(boolean activar, String motivo) {
        String limpio = motivo == null ? "" : motivo.trim();
        if (!activar && (limpio.isEmpty() || limpio.length() > 300))
            throw new IllegalArgumentException("Indica el motivo de desactivación (1 a 300 caracteres).");
        return activar ? null : limpio;
    }

    /** El bloqueo de sala también es usado por venta y programación: evita cambios concurrentes. */
    public void cambiarEstado(int idSala, Integer idAsiento, boolean activar, String motivo) {
        Sesion.exigirAdministrador();
        String razon = validarMotivo(activar, motivo);
        Transacciones.ejecutar(conexiones, c -> {
            Sala sala = new SalaDAO(c).bloquear(idSala);
            if (sala == null) throw new IllegalStateException("La sala ya no existe.");
            if (idAsiento != null) {
                try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM Asiento WHERE id_asiento=? AND id_sala=? FOR UPDATE")) {
                    ps.setInt(1,idAsiento); ps.setInt(2,idSala);
                    try (ResultSet rs=ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("El asiento no pertenece a esta sala.");
                    }
                }
            }
            if (!activar) {
                String sql = "SELECT EXISTS(SELECT 1 FROM Ticket t JOIN Funcion f USING(id_funcion) "
                    + "JOIN Asiento a USING(id_asiento) WHERE f.id_sala=? "
                    + "AND f.estado IN ('Programada','en curso') AND f.fecha_proyeccion + f.hora_fin "
                    + "+ CASE WHEN f.hora_fin<=f.hora_inicio THEN interval '1 day' ELSE interval '0 day' END > LOCALTIMESTAMP"
                    + (idAsiento == null ? "" : " AND a.id_asiento=?") + ")";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1,idSala); if (idAsiento != null) ps.setInt(2,idAsiento);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next(); if (rs.getBoolean(1)) throw new IllegalStateException(
                            "Hay boletos vendidos para funciones pendientes. Resuelve esas ventas antes de desactivar "
                            + (idAsiento == null ? "la sala." : "el asiento."));
                    }
                }
            }
            String sql = idAsiento == null
                    ? "UPDATE Sala SET estado=?::estado_sala,motivo_inactividad=? WHERE id_sala=?"
                    : "UPDATE Asiento SET estado=?::estado_asiento,motivo_inactividad=? WHERE id_asiento=? AND id_sala=?";
            try (PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setString(1,idAsiento == null ? (activar ? "ACTIVA" : "MANTENIMIENTO") : (activar ? "Disponible" : "Averiado"));
                ps.setString(2,razon); ps.setInt(3,idAsiento == null ? idSala : idAsiento);
                if (idAsiento != null) ps.setInt(4,idSala);
                if (ps.executeUpdate()!=1) throw new IllegalStateException("No se pudo actualizar el estado.");
            }
            return null;
        });
    }
}
