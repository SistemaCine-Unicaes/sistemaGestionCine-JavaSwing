/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.ConexionDB;
import models.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author axele
 */
public class SalaDAO {
    
    public boolean registrarSala(Sala sala) {
        // Se utiliza ?::estado_sala para que PostgreSQL reconozca el String como su tipo ENUM personalizado
        String sql = "INSERT INTO Sala (capacidad_total, asientos_especiales, tiempo_de_limpieza, asientos_por_fila, estado) " +
                     "VALUES (?, ?, ?, ?, ?::estado_sala)";
                     
        try {            
            Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql);
             
            ps.setInt(1, sala.getCapacidadTotal());
            ps.setInt(2, sala.getAsientosEspeciales());
            ps.setInt(3, sala.getTiempoDeLimpieza());
            ps.setInt(4, sala.getAsientosPorFila());
            ps.setString(5, sala.getEstado());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo registrar la sala.");
            System.err.println("Detalle: " + e.getMessage());
            return false;
        }
    }
    
    public List<Sala> listarSalas() {
        List<Sala> listaSalas = new ArrayList<>();
        String sql = "SELECT * FROM Sala ORDER BY id_sala";
        
        try {
            Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
             
            while (rs.next()) {
                Sala sala = new Sala();
                sala.setIdSala(rs.getInt("id_sala"));
                sala.setCapacidadTotal(rs.getInt("capacidad_total"));
                sala.setAsientosEspeciales(rs.getInt("asientos_especiales"));
                sala.setTiempoDeLimpieza(rs.getInt("tiempo_de_limpieza"));
                sala.setAsientosPorFila(rs.getInt("asientos_por_fila"));
                sala.setEstado(rs.getString("estado"));
                
                listaSalas.add(sala);
            }
            
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudieron obtener las salas.");
            System.err.println("Detalle: " + e.getMessage());
        }
        
        return listaSalas;
    }
    
    public boolean actualizarSala(Sala sala) {
        String sql = "UPDATE Sala SET capacidad_total = ?, asientos_especiales = ?, tiempo_de_limpieza = ?, asientos_por_fila = ?, estado = ?::estado_sala WHERE id_sala = ?";
                     
        try {
            Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql);
             
            ps.setInt(1, sala.getCapacidadTotal());
            ps.setInt(2, sala.getAsientosEspeciales());
            ps.setInt(3, sala.getTiempoDeLimpieza());
            ps.setInt(4, sala.getAsientosPorFila());
            ps.setString(5, sala.getEstado());
            ps.setInt(6, sala.getIdSala());
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo actualizar la sala.");
            System.err.println("Detalle: " + e.getMessage());
            return false;
        }
    }
    
    public boolean eliminarSala(int idSala) {
        String sql = "DELETE FROM Sala WHERE id_sala = ?";
        
        try {
            Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql); 
            
            ps.setInt(1, idSala);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo eliminar la sala.");
            System.err.println("Detalle: " + e.getMessage());
            return false;
        }
    }
    
    public Sala obtenerSalaPorId(int idSala) {
        Sala sala = null;
        String sql = "SELECT * FROM Sala WHERE id_sala = ?";
        
        try {
            Connection conn = ConexionDB.conectar();
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setInt(1, idSala);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sala = new Sala();
                    sala.setIdSala(rs.getInt("id_sala"));
                    sala.setCapacidadTotal(rs.getInt("capacidad_total"));
                    sala.setAsientosEspeciales(rs.getInt("asientos_especiales"));
                    sala.setTiempoDeLimpieza(rs.getInt("tiempo_de_limpieza"));
                    sala.setAsientosPorFila(rs.getInt("asientos_por_fila"));
                    sala.setEstado(rs.getString("estado"));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo obtener la sala con ID " + idSala);
            System.err.println("Detalle: " + e.getMessage());
        }
        
        return sala;
    }
    
    
    // Método de prueba temporal para consola
    public static void main(String[] args) {
        SalaDAO dao = new SalaDAO();
        
        // 1. Probar el registro de una nueva sala
        System.out.println("--- REGISTRAR SALA ---");
        Sala nuevaSala = new Sala(100, 10, 15, 10, "ACTIVA");
        boolean exito = dao.registrarSala(nuevaSala);
        
        if (exito) {
            System.out.println("✅ Sala registrada correctamente en Supabase.");
        } else {
            System.out.println("🚫 Falló el registro de la sala.");
        }
        
        // 2. Probar el listado de salas
        System.out.println("\n--- LISTAR SALAS ---");
        List<Sala> salas = dao.listarSalas();
        
        if (salas.isEmpty()) {
            System.out.println("No hay salas registradas actualmente.");
        } else {
            for (Sala s : salas) {
                System.out.println(s.toString());
            }
            
            // 3. Probar la búsqueda por ID (usando el ID de la primera sala listada)
            int idBusqueda = salas.get(0).getIdSala();
            System.out.println("\n--- BUSCAR SALA POR ID (" + idBusqueda + ") ---");
            Sala salaEncontrada = dao.obtenerSalaPorId(idBusqueda);
            
            if (salaEncontrada != null) {
                System.out.println("✅ Sala encontrada: " + salaEncontrada.toString());
            } else {
                System.out.println("🚫 No se encontró ninguna sala con el ID " + idBusqueda);
            }
        }
    }
}