/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.ConexionDB;
import models.Asiento;

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
public class AsientoDAO {
    
    public boolean generarAsientosParaSala(int idSala, String[] filas, int asientosPorFila) {
        if (filas == null || filas.length == 0 || asientosPorFila <= 0) return false;

        // Utilizamos StringBuilder para construir la consulta dinámicamente
        StringBuilder sql = new StringBuilder("INSERT INTO Asiento (id_sala, tipo_de_asiento, fila, numero, estado) VALUES ");

        // Pre-calculamos el total de asientos para evitar redimensionamientos de listas
        int totalAsientos = filas.length * asientosPorFila;
        
        for (int i = 0; i < totalAsientos; i++) {
            sql.append("(?, 'Normal', ?, ?, 'Disponible'::estado_asiento)");
            if (i < totalAsientos - 1) {
                sql.append(", ");
            }
        }

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
             
            int paramIndex = 1;
            
            for (String fila : filas) {
                for (int i = 1; i <= asientosPorFila; i++) {
                    ps.setInt(paramIndex++, idSala);
                    ps.setString(paramIndex++, fila);
                    ps.setInt(paramIndex++, i);
                }
            }
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas == totalAsientos;
            
        } catch (SQLException e) {
            System.err.println("ERROR: Falla al ejecutar el Multi-Row Insert de asientos.");
            System.err.println("Detalle: " + e.getMessage());
            return false;
        }
    }
    
    public List<Asiento> obtenerAsientosPorSala(int idSala) {
        List<Asiento> listaAsientos = new ArrayList<>();
        // Ordenamiento para que se listen secuencialmente (A-1, A-2... B-1)
        String sql = "SELECT * FROM Asiento WHERE id_sala = ? ORDER BY fila, numero";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, idSala);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asiento asiento = new Asiento();
                    asiento.setIdAsiento(rs.getInt("id_asiento"));
                    asiento.setIdSala(rs.getInt("id_sala"));
                    asiento.setTipoDeAsiento(rs.getString("tipo_de_asiento"));
                    asiento.setFila(rs.getString("fila"));
                    asiento.setNumero(rs.getInt("numero"));
                    asiento.setEstado(rs.getString("estado"));
                    
                    listaAsientos.add(asiento);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudieron obtener los asientos de la sala " + idSala);
            System.err.println("Detalle: " + e.getMessage());
        }
        
        return listaAsientos;
    }
    
    public static void main(String[] args) {
        AsientoDAO dao = new AsientoDAO();
        
        int idSalaPrueba = 1; // Asegúrate de que exista una sala con ID 1 en tu BD
        String[] filas = {"A", "B", "C"};
        int asientosPorFila = 10;
        
        System.out.println("--- GENERANDO ASIENTOS ---");
        boolean exito = dao.generarAsientosParaSala(idSalaPrueba, filas, asientosPorFila);
        
        if (exito) {
            System.out.println("✅ " + (filas.length * asientosPorFila) + " asientos generados exitosamente.");
            
            System.out.println("\n--- LISTANDO ASIENTOS DE LA SALA " + idSalaPrueba + " ---");
            List<Asiento> asientos = dao.obtenerAsientosPorSala(idSalaPrueba);
            
            // Imprimimos solo los primeros 5 para no saturar la consola
            for (int i = 0; i < Math.min(5, asientos.size()); i++) {
                System.out.println(asientos.get(i).toString());
            }
            if (asientos.size() > 5) {
                System.out.println("... y " + (asientos.size() - 5) + " asientos más.");
            }
        }
    }
}
