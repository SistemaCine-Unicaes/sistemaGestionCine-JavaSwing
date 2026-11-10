package dao;

import models.Ticket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private final Connection conexion;
    public TicketDAO(Connection conexion) { this.conexion = conexion; }

    public boolean venderTicket(Ticket t) {
        int idCaja = t.getIdCaja() != null ? t.getIdCaja() : config.Sesion.getIdCajaActual();
        String sql = "INSERT INTO Ticket(id_funcion,id_asiento,id_usuario,monto,id_caja) VALUES (?,?,?,?,?) RETURNING id_ticket,fecha_hora_compra";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, t.getIdFuncion());
            ps.setInt(2, t.getIdAsiento());
            ps.setInt(3, t.getIdUsuario());
            ps.setBigDecimal(4, t.getMonto());
            ps.setInt(5, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                t.setIdTicket(rs.getInt(1));
                t.setFechaHoraCompra(rs.getTimestamp(2));
                t.setIdCaja(idCaja);
            }
            return true;
        } catch (SQLException e) {
            // Fallback en caso de que la columna id_caja no exista en la instancia actual
            String sqlFallback = "INSERT INTO Ticket(id_funcion,id_asiento,id_usuario,monto) VALUES (?,?,?,?) RETURNING id_ticket,fecha_hora_compra";
            try (PreparedStatement ps2 = conexion.prepareStatement(sqlFallback)) {
                ps2.setInt(1, t.getIdFuncion());
                ps2.setInt(2, t.getIdAsiento());
                ps2.setInt(3, t.getIdUsuario());
                ps2.setBigDecimal(4, t.getMonto());
                try (ResultSet rs = ps2.executeQuery()) {
                    rs.next();
                    t.setIdTicket(rs.getInt(1));
                    t.setFechaHoraCompra(rs.getTimestamp(2));
                }
                return true;
            } catch (SQLException ex) {
                throw new AccesoDatosException(e);
            }
        }
    }

    public List<Ticket> obtenerTicketsPorFuncion(int idFuncion) {
        List<Ticket> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Ticket WHERE id_funcion=? ORDER BY id_ticket")) {
            ps.setInt(1, idFuncion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer idCaja = null;
                    try {
                        idCaja = rs.getInt("id_caja");
                        if (rs.wasNull()) idCaja = 1;
                    } catch (Exception ignored) {}
                    lista.add(new Ticket(rs.getInt("id_ticket"), rs.getInt("id_funcion"),
                            rs.getInt("id_asiento"), rs.getInt("id_usuario"), rs.getBigDecimal("monto"),
                            rs.getTimestamp("fecha_hora_compra"), idCaja));
                }
            }
            return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
