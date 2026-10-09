package dao;

import models.Ticket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private final Connection conexion;
    public TicketDAO(Connection conexion) { this.conexion = conexion; }

    public boolean venderTicket(Ticket t) {
        String sql = "INSERT INTO Ticket(id_funcion,id_asiento,id_usuario,monto) VALUES (?,?,?,?) RETURNING id_ticket,fecha_hora_compra";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, t.getIdFuncion()); ps.setInt(2, t.getIdAsiento());
            ps.setInt(3, t.getIdUsuario()); ps.setBigDecimal(4, t.getMonto());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next(); t.setIdTicket(rs.getInt(1)); t.setFechaHoraCompra(rs.getTimestamp(2));
            }
            return true;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public List<Ticket> obtenerTicketsPorFuncion(int idFuncion) {
        List<Ticket> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Ticket WHERE id_funcion=? ORDER BY id_ticket")) {
            ps.setInt(1, idFuncion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new Ticket(rs.getInt("id_ticket"), rs.getInt("id_funcion"),
                        rs.getInt("id_asiento"), rs.getInt("id_usuario"), rs.getBigDecimal("monto"), rs.getTimestamp("fecha_hora_compra")));
            }
            return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}