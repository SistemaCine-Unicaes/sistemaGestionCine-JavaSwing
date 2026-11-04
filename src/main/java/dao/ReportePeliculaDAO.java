package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import models.Pelicula;

/** Consulta la ficha, las funciones y todas las entradas vendidas para una película. */
public final class ReportePeliculaDAO {
    private final Connection conexion;

    public ReportePeliculaDAO(Connection conexion) { this.conexion = conexion; }

    public record Funcion(int id, Date fecha, Time inicio, Time fin, String estado,
            int idSala, long tickets, BigDecimal total) {}
    public record Ticket(int id, int idFuncion, Timestamp compra, Date fechaFuncion,
            Time horaFuncion, int idSala, String asiento, String tipoAsiento,
            int idUsuario, String cajero, BigDecimal monto) {}
    public record Reporte(Pelicula pelicula, List<Funcion> funciones, List<Ticket> tickets,
            long totalTickets, BigDecimal totalIngresos) {}

    public Reporte obtener(int idPelicula) {
        Pelicula pelicula = obtenerPelicula(idPelicula);
        if (pelicula == null) return null;
        List<Funcion> funciones = obtenerFunciones(idPelicula);
        List<Ticket> tickets = obtenerTickets(idPelicula);
        long cantidad = tickets.size();
        BigDecimal total = tickets.stream().map(Ticket::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Reporte(pelicula, List.copyOf(funciones), List.copyOf(tickets), cantidad, total);
    }

    private Pelicula obtenerPelicula(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Pelicula WHERE id_pelicula=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? PeliculaDAO.mapear(rs) : null; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    private List<Funcion> obtenerFunciones(int idPelicula) {
        String sql = "SELECT f.id_funcion,f.fecha_proyeccion,f.hora_inicio,f.hora_fin,f.estado,f.id_sala,"
                + "COUNT(t.id_ticket) AS tickets,COALESCE(SUM(t.monto),0) AS total "
                + "FROM Funcion f LEFT JOIN Ticket t USING(id_funcion) WHERE f.id_pelicula=? "
                + "GROUP BY f.id_funcion ORDER BY f.fecha_proyeccion,f.hora_inicio,f.id_funcion";
        List<Funcion> filas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPelicula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filas.add(new Funcion(rs.getInt("id_funcion"), rs.getDate("fecha_proyeccion"),
                        rs.getTime("hora_inicio"), rs.getTime("hora_fin"), rs.getString("estado"),
                        rs.getInt("id_sala"), rs.getLong("tickets"), rs.getBigDecimal("total")));
            }
            return filas;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    private List<Ticket> obtenerTickets(int idPelicula) {
        String sql = "SELECT t.id_ticket,t.id_funcion,t.fecha_hora_compra,f.fecha_proyeccion,f.hora_inicio,"
                + "f.id_sala,a.fila || '-' || a.numero AS asiento,a.tipo_de_asiento,"
                + "u.id_usuario,u.nombre AS cajero,t.monto "
                + "FROM Ticket t JOIN Funcion f USING(id_funcion) JOIN Asiento a USING(id_asiento) "
                + "JOIN Usuario u USING(id_usuario) WHERE f.id_pelicula=? "
                + "ORDER BY f.fecha_proyeccion,f.hora_inicio,t.fecha_hora_compra,t.id_ticket";
        List<Ticket> filas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPelicula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filas.add(new Ticket(rs.getInt("id_ticket"), rs.getInt("id_funcion"),
                        rs.getTimestamp("fecha_hora_compra"), rs.getDate("fecha_proyeccion"),
                        rs.getTime("hora_inicio"), rs.getInt("id_sala"), rs.getString("asiento"),
                        rs.getString("tipo_de_asiento"), rs.getInt("id_usuario"), rs.getString("cajero"),
                        rs.getBigDecimal("monto")));
            }
            return filas;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
