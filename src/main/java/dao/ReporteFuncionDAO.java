/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.Conexion;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author axele
 */
public class ReporteFuncionDAO {
    public record ResumenFuncion(
            int idFuncion,
            String pelicula,
            LocalDate fecha,
            String hora,
            int idSala,
            String estado,
            int capacidad,
            long boletos,
            BigDecimal ingresos
    ) {}

    public List<ResumenFuncion> listarPorFecha(LocalDate fecha)
            throws SQLException {

        String sql = """
            SELECT
                f.id_funcion,
                p.nombre AS pelicula,
                f.fecha_proyeccion,
                f.hora_inicio,
                f.id_sala,
                f.estado::text AS estado,
                s.capacidad_total,
                COUNT(t.id_ticket) AS boletos,
                COALESCE(SUM(t.monto), 0) AS ingresos
            FROM Funcion f
            JOIN Pelicula p
                ON p.id_pelicula = f.id_pelicula
            JOIN Sala s
                ON s.id_sala = f.id_sala
            LEFT JOIN Ticket t
                ON t.id_funcion = f.id_funcion
            WHERE f.fecha_proyeccion = ?
            GROUP BY
                f.id_funcion,
                p.nombre,
                f.fecha_proyeccion,
                f.hora_inicio,
                f.id_sala,
                f.estado,
                s.capacidad_total
            ORDER BY f.hora_inicio, f.id_funcion
            """;

        List<ResumenFuncion> lista = new ArrayList<>();

        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(new ResumenFuncion(
                            rs.getInt("id_funcion"),
                            rs.getString("pelicula"),
                            rs.getDate("fecha_proyeccion").toLocalDate(),
                            rs.getTime("hora_inicio").toString(),
                            rs.getInt("id_sala"),
                            rs.getString("estado"),
                            rs.getInt("capacidad_total"),
                            rs.getLong("boletos"),
                            rs.getBigDecimal("ingresos")
                    ));
                }
            }
        }

        return lista;
    }
}
