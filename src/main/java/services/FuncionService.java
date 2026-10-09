package services;

import config.Sesion;
import dao.FuncionDAO;
import dao.SalaDAO;
import java.sql.*;
import java.time.LocalDateTime;
import models.Funcion;
import models.Sala;

public class FuncionService {

    private final java.util.function.Supplier<Connection> conexiones;

    public FuncionService() {
        this(config.Conexion::getConexion);
    }

    public FuncionService(java.util.function.Supplier<Connection> conexiones) {
        this.conexiones = conexiones;
    }

    public void programar(Funcion funcion) {
        Sesion.exigirAdministrador();
        if (funcion.getFechaProyeccion() == null || funcion.getHoraInicio() == null) {
            throw new IllegalArgumentException("Ingresa la fecha y la hora de la función.");
        }
        Transacciones.ejecutar(conexiones, c -> {
            Sala sala = new SalaDAO(c).bloquear(funcion.getIdSala());
            if (sala == null || !"ACTIVA".equals(sala.getEstado())) {
                throw new IllegalStateException("La sala debe estar activa.");
            }
            int duracion;
            try (PreparedStatement ps = c.prepareStatement("SELECT duracion,estado FROM Pelicula WHERE id_pelicula=? FOR SHARE")) {
                ps.setInt(1, funcion.getIdPelicula());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"CARTELERA".equals(rs.getString("estado"))) {
                        throw new IllegalStateException("La película debe estar en cartelera.");
                    }
                    duracion = rs.getInt("duracion");
                }
            }
            if (duracion <= 0 || duracion >= 1440) {
                throw new IllegalStateException("La duración debe ser entre 1 y 1439 minutos.");
            }
            LocalDateTime inicio = funcion.getFechaProyeccion().toLocalDate().atTime(funcion.getHoraInicio().toLocalTime());
            try (Statement ps = c.createStatement(); ResultSet rs = ps.executeQuery("SELECT LOCALTIMESTAMP")) {
                rs.next();
                if (!inicio.isAfter(rs.getTimestamp(1).toLocalDateTime())) {
                    throw new IllegalArgumentException("La función debe comenzar en el futuro.");
                }
            }
            LocalDateTime fin = inicio.plusMinutes(duracion);
            String sql = "SELECT EXISTS(SELECT 1 FROM Funcion WHERE id_sala=? AND estado <> 'cancelada' "
                    + "AND fecha_proyeccion + hora_inicio < ? AND fecha_proyeccion + hora_fin "
                    + "+ CASE WHEN hora_fin <= hora_inicio THEN interval '1 day' ELSE interval '0 day' END "
                    + "+ (? * interval '1 minute') > ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, sala.getIdSala());
                ps.setTimestamp(2, Timestamp.valueOf(fin.plusMinutes(sala.getTiempoDeLimpieza())));
                ps.setInt(3, sala.getTiempoDeLimpieza());
                ps.setTimestamp(4, Timestamp.valueOf(inicio));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getBoolean(1)) {
                        throw new IllegalStateException("El horario se cruza con otra función o con el tiempo de limpieza de la sala.");
                    }
                }
            }
            funcion.setHoraFin(Time.valueOf(fin.toLocalTime()));
            funcion.setEstado("Programada");
            new FuncionDAO(c).programarFuncion(funcion);
            return null;
        });
    }
}
