package dao;

import config.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ReporteSalaDAO {

    public List<Object[]> obtenerOcupacion(String filtroSala) throws Exception {
        List<Object[]> reporte = new ArrayList<>();

        String sql = "SELECT "
                   + "    s.id_sala, "
                   + "    'Sala ' || s.id_sala || CASE WHEN s.nombre IS NOT NULL AND s.nombre <> '' AND s.nombre <> (s.id_sala::text) THEN ' · ' || s.nombre ELSE '' END AS nombre_sala, "
                   + "    s.capacidad_total, "
                   + "    f.id_funcion, "
                   + "    COALESCE(p.nombre, 'Sin funciones programadas') AS pelicula, "
                   + "    f.fecha_proyeccion, "
                   + "    f.hora_inicio, "
                   + "    COALESCE(f.estado::text, '-') AS estado_funcion, "
                   + "    COUNT(t.id_ticket) AS boletos_vendidos, "
                   + "    COALESCE(SUM(t.monto), 0) AS recaudacion "
                   + "FROM Sala s "
                   + "LEFT JOIN Funcion f ON s.id_sala = f.id_sala "
                   + "LEFT JOIN Pelicula p ON f.id_pelicula = p.id_pelicula "
                   + "LEFT JOIN Ticket t ON f.id_funcion = t.id_funcion "
                   + "WHERE (? = 'Todas las salas' "
                   + "   OR s.id_sala::text = ? "
                   + "   OR s.nombre = ? "
                   + "   OR ('Sala ' || s.id_sala) = ? "
                   + "   OR ('Sala ' || s.id_sala || CASE WHEN s.nombre IS NOT NULL AND s.nombre <> '' AND s.nombre <> (s.id_sala::text) THEN ' · ' || s.nombre ELSE '' END) = ?) "
                   + "GROUP BY s.id_sala, s.nombre, s.capacidad_total, f.id_funcion, p.nombre, f.fecha_proyeccion, f.hora_inicio, f.estado "
                   + "ORDER BY s.id_sala, f.fecha_proyeccion DESC NULLS LAST, f.hora_inicio DESC NULLS LAST, f.id_funcion NULLS LAST";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                ps.setString(i, filtroSala != null ? filtroSala : "Todas las salas");
            }

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
                while (rs.next()) {
                    String idFuncStr = rs.getObject("id_funcion") != null ? ("#" + rs.getInt("id_funcion")) : "-";
                    Date fProj = rs.getDate("fecha_proyeccion");
                    Time hIni = rs.getTime("hora_inicio");
                    String fechaHora = (fProj != null) 
                            ? sdfFecha.format(fProj) + (hIni != null ? " " + sdfHora.format(hIni) : "")
                            : "-";

                    reporte.add(new Object[]{
                        rs.getString("nombre_sala"),
                        idFuncStr,
                        rs.getString("pelicula"),
                        fechaHora,
                        rs.getString("estado_funcion"),
                        rs.getLong("boletos_vendidos"),
                        String.format("$%.2f", rs.getDouble("recaudacion"))
                    });
                }
            }
        }
        return reporte;
    }

    public List<String> obtenerNombresSalas() {
        List<String> salas = new ArrayList<>();
        String sql = "SELECT s.id_sala, "
                   + "'Sala ' || s.id_sala || CASE WHEN s.nombre IS NOT NULL AND s.nombre <> '' AND s.nombre <> (s.id_sala::text) THEN ' · ' || s.nombre ELSE '' END AS nombre_sala "
                   + "FROM Sala s ORDER BY s.id_sala";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salas.add(rs.getString("nombre_sala"));
            }
        } catch (Exception e) {
            // Retorna lista vacía si hay error de conexión
        }
        return salas;
    }
}
