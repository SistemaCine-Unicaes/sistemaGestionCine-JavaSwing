package com.mycompany.sistemagestioncine.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReportesDAO {

    private final Connection conexion;

    public ReportesDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public double obtenerTotalVentasPorFecha(Date fecha) {
        String sql = "SELECT COALESCE(SUM(monto), 0) AS total FROM Ticket WHERE DATE(fecha_hora_compra) = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, fecha);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total de ventas: " + e.getMessage());
        }
        return 0.0;
    }

    public int obtenerCantidadTicketsVendidos(Date fecha) {
        String sql = "SELECT COUNT(id_ticket) AS cantidad FROM Ticket WHERE DATE(fecha_hora_compra) = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, fecha);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cantidad de tickets: " + e.getMessage());
        }
        return 0;
    }

    // Método opcional: Ventas agrupadas por cajero (retorna Nombre -> Total)
    public Map<String, Double> obtenerVentasPorCajero(Date fecha) {
        Map<String, Double> ventasCajero = new HashMap<>();
        String sql = "SELECT u.nombre, COALESCE(SUM(t.monto), 0) AS total " +
                     "FROM Ticket t " +
                     "INNER JOIN Usuario u ON t.id_usuario = u.id_usuario " +
                     "WHERE DATE(t.fecha_hora_compra) = ? " +
                     "GROUP BY u.id_usuario, u.nombre";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, fecha);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventasCajero.put(rs.getString("nombre"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por cajero: " + e.getMessage());
        }
        return ventasCajero;
    }
}