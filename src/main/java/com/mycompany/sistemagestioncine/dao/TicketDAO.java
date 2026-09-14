package com.mycompany.sistemagestioncine.dao;

import com.mycompany.sistemagestioncine.models.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    private final Connection conexion;

    public TicketDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public boolean venderTicket(Ticket ticket) {
        String sql = "INSERT INTO Ticket (id_funcion, id_asiento, id_usuario, monto) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, ticket.getIdFuncion());
            ps.setInt(2, ticket.getIdAsiento());
            ps.setInt(3, ticket.getIdUsuario());
            ps.setBigDecimal(4, ticket.getMonto());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Requisito estrella: Identificar el código SQLState 23505 (unique_violation)
            if ("23505".equals(e.getSQLState())) {
                System.err.println("⚠️ Error de Concurrencia (23505): El asiento ya fue vendido para esta función.");
            } else {
                System.err.println("Error al vender el ticket: " + e.getMessage());
            }
            return false;
        }
    }

    public List<Ticket> obtenerTicketsPorFuncion(int idFuncion) {
        List<Ticket> lista = new ArrayList<>();
        String sql = "SELECT * FROM Ticket WHERE id_funcion = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idFuncion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ticket t = new Ticket(
                        rs.getInt("id_ticket"),
                        rs.getInt("id_funcion"),
                        rs.getInt("id_asiento"),
                        rs.getInt("id_usuario"),
                        rs.getBigDecimal("monto"),
                        rs.getTimestamp("fecha_hora_compra")
                    );
                    lista.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener tickets por función: " + e.getMessage());
        }

        return lista;
    }
    
    
    // Reporte Mensual: recibe mes (1-12) y año (ej. 2026)
    public double obtenerTotalVentasPorMes(int mes, int anio) {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total FROM Ticket " +
                     "WHERE EXTRACT(MONTH FROM fecha_hora_compra) = ? " +
                     "AND EXTRACT(YEAR FROM fecha_hora_compra) = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas mensuales: " + e.getMessage());
        }
        return 0.0;
    }

    // Reporte Anual: recibe solo el año (ej. 2026)
    public double obtenerTotalVentasPorAnio(int anio) {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total FROM Ticket " +
                     "WHERE EXTRACT(YEAR FROM fecha_hora_compra) = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, anio);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas anuales: " + e.getMessage());
        }
        return 0.0;
    }
}