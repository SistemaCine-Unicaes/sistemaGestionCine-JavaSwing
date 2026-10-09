package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportesDAO {
    private final Connection conexion;
    public ReportesDAO(Connection conexion) { this.conexion = conexion; }
    public record VentaCajero(int idUsuario, String nombre, long tickets, BigDecimal total) {}
    public record Reporte(List<VentaCajero> cajeros, long tickets, BigDecimal total) {}

    public Reporte obtenerReporte(LocalDate desde, LocalDate hastaExclusiva) {
        String sql = "SELECT u.id_usuario,u.nombre,COUNT(*) AS cantidad,SUM(t.monto) AS total "
                + "FROM Ticket t JOIN Usuario u USING(id_usuario) "
                + "WHERE t.fecha_hora_compra>=? AND t.fecha_hora_compra<? "
                + "GROUP BY u.id_usuario,u.nombre ORDER BY u.nombre,u.id_usuario";
        List<VentaCajero> filas = new ArrayList<>();
        long cantidad = 0;
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hastaExclusiva.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaCajero fila = new VentaCajero(rs.getInt("id_usuario"), rs.getString("nombre"),
                            rs.getLong("cantidad"), rs.getBigDecimal("total"));
                    filas.add(fila); cantidad += fila.tickets(); total = total.add(fila.total());
                }
            }
            return new Reporte(List.copyOf(filas), cantidad, total);
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}