package dao;

import config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReporteSalaDAO {

    public List<Object[]> obtenerOcupacion(String filtroSala) throws Exception {
        List<Object[]> reporte = new ArrayList<>();
        
        // Consulta SQL agrupando ventas por sala
        String sql = "SELECT s.nombre_sala, "
                   + "COUNT(t.id_ticket) as boletos_vendidos, "
                   + "COUNT(t.id_asiento) as asientos_ocupados, "
                   + "SUM(t.monto) as recaudacion "
                   + "FROM salas s "
                   + "LEFT JOIN funciones f ON s.id_sala = f.id_sala "
                   + "LEFT JOIN tickets t ON f.id_funcion = t.id_funcion "
                   + "WHERE (? = 'Todas las salas' OR s.nombre_sala = ?) "
                   + "GROUP BY s.nombre_sala ORDER BY s.nombre_sala";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, filtroSala);
            ps.setString(2, filtroSala);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reporte.add(new Object[]{
                        rs.getString("nombre_sala"),
                        rs.getInt("boletos_vendidos"),
                        rs.getInt("asientos_ocupados"),
                        String.format("$%.2f", rs.getDouble("recaudacion") != 0 ? rs.getDouble("recaudacion") : 0.0)
                    });
                }
            }
        }
        return reporte;
    }
}