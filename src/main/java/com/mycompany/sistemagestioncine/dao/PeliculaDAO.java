package com.mycompany.sistemagestioncine.dao;

import com.mycompany.sistemagestioncine.models.Pelicula;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDAO {
    
    private final Connection conexion;

    public PeliculaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public boolean insertarPelicula(Pelicula pelicula) {
        // Se aplica CAST(? AS estado_pelicula) para manejar el ENUM de PostgreSQL
        String sql = "INSERT INTO Pelicula (nombre, sinopsis, duracion, genero, director, fecha_estreno, tipo_estreno, imagen_url, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS estado_pelicula))";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pelicula.getNombre());
            ps.setString(2, pelicula.getSinopsis());
            ps.setInt(3, pelicula.getDuracion());
            ps.setString(4, pelicula.getGenero());
            ps.setString(5, pelicula.getDirector());
            ps.setDate(6, pelicula.getFechaEstreno());
            ps.setString(7, pelicula.getTipoEstreno());
            ps.setString(8, pelicula.getImagenUrl());
            ps.setString(9, pelicula.getEstado());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al insertar la película: " + e.getMessage());
            return false;
        }
    }

    public List<Pelicula> obtenerTodas() {
        List<Pelicula> listaPeliculas = new ArrayList<>();
        String sql = "SELECT * FROM Pelicula";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Pelicula p = new Pelicula(
                    rs.getInt("id_pelicula"),
                    rs.getString("nombre"),
                    rs.getString("sinopsis"),
                    rs.getInt("duracion"),
                    rs.getString("genero"),
                    rs.getString("director"),
                    rs.getDate("fecha_estreno"),
                    rs.getString("tipo_estreno"),
                    rs.getString("imagen_url"),
                    rs.getString("estado")
                );
                listaPeliculas.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener las películas: " + e.getMessage());
        }
        
        return listaPeliculas;
    }
    
    
    public boolean actualizarImagenPoster(int idPelicula, String imagenUrl) {
    String sql = "UPDATE Pelicula SET imagen_url = ? WHERE id_pelicula = ?";
    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
        ps.setString(1, imagenUrl);
        ps.setInt(2, idPelicula);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error al actualizar la URL del póster: " + e.getMessage());
        return false;
    }
    }
}