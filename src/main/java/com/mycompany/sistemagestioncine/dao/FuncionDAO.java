package com.mycompany.sistemagestioncine.dao;

import com.mycompany.sistemagestioncine.models.Funcion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class FuncionDAO {
    
    private final Connection conexion;

    public FuncionDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public boolean programarFuncion(Funcion funcion) {
        String sql = "INSERT INTO Funcion (id_pelicula, id_sala, fecha_proyeccion, hora_inicio, hora_fin, estado) VALUES (?, ?, ?, ?, ?, CAST(? AS estado_funcion))";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, funcion.getIdPelicula());
            ps.setInt(2, funcion.getIdSala());
            ps.setDate(3, funcion.getFechaProyeccion());
            ps.setTime(4, funcion.getHoraInicio());
            ps.setTime(5, funcion.getHoraFin());
            ps.setString(6, funcion.getEstado()); 
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al programar la función: " + e.getMessage());
            return false;
        }
    }

    public List<Funcion> listarFuncionesDelDia(Date fecha) {
        List<Funcion> listaFunciones = new ArrayList<>();
        String sql = "SELECT * FROM Funcion WHERE fecha_proyeccion = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setDate(1, fecha);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Funcion funcion = new Funcion(
                        rs.getInt("id_funcion"),
                        rs.getInt("id_pelicula"),
                        rs.getInt("id_sala"),
                        rs.getDate("fecha_proyeccion"),
                        rs.getTime("hora_inicio"),
                        rs.getTime("hora_fin"),
                        rs.getString("estado")
                    );
                    listaFunciones.add(funcion);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar las funciones del día: " + e.getMessage());
        }
        
        return listaFunciones;
    }
}