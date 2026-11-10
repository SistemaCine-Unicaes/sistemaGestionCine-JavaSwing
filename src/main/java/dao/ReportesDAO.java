package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportesDAO {
    private final Connection conexion;
    public ReportesDAO(Connection conexion) { this.conexion = conexion; }

    public record VentaCajero(int idUsuario, String nombre, long tickets, BigDecimal total, String cajas) {
        public VentaCajero(int idUsuario, String nombre, long tickets, BigDecimal total) {
            this(idUsuario, nombre, tickets, total, "Caja 01");
        }
    }

    public record VentaPorCaja(int idCaja, String nombreCaja, long tickets, BigDecimal total, int cajerosDistintos) {}

    public record Reporte(List<VentaCajero> cajeros, long tickets, BigDecimal total, List<VentaPorCaja> cajas) {
        public Reporte(List<VentaCajero> cajeros, long tickets, BigDecimal total) {
            this(cajeros, tickets, total, List.of());
        }
    }

    public Reporte obtenerReporte(LocalDate desde, LocalDate hastaExclusiva) {
        new CajaDAO(conexion).asegurarEstructura();

        String sqlCajeros = "SELECT u.id_usuario, u.nombre, COUNT(t.id_ticket) AS cantidad, "
                + "COALESCE(SUM(t.monto), 0) AS total, "
                + "COALESCE(STRING_AGG(DISTINCT COALESCE(c.nombre, 'Caja ' || COALESCE(t.id_caja, 1)), ', '), 'Caja 01') AS cajas "
                + "FROM Ticket t "
                + "JOIN Usuario u USING(id_usuario) "
                + "LEFT JOIN Caja c ON t.id_caja = c.id_caja "
                + "WHERE t.fecha_hora_compra >= ? AND t.fecha_hora_compra < ? "
                + "GROUP BY u.id_usuario, u.nombre "
                + "ORDER BY u.nombre, u.id_usuario";

        List<VentaCajero> filas = new ArrayList<>();
        long cantidad = 0;
        BigDecimal total = BigDecimal.ZERO.setScale(2);

        try (PreparedStatement ps = conexion.prepareStatement(sqlCajeros)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hastaExclusiva.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaCajero fila = new VentaCajero(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getLong("cantidad"),
                            rs.getBigDecimal("total"),
                            rs.getString("cajas")
                    );
                    filas.add(fila);
                    cantidad += fila.tickets();
                    total = total.add(fila.total());
                }
            }
        } catch (SQLException e) {
            // Fallback si la columna id_caja no existiese
            String sqlFallback = "SELECT u.id_usuario, u.nombre, COUNT(*) AS cantidad, SUM(t.monto) AS total "
                    + "FROM Ticket t JOIN Usuario u USING(id_usuario) "
                    + "WHERE t.fecha_hora_compra >= ? AND t.fecha_hora_compra < ? "
                    + "GROUP BY u.id_usuario, u.nombre ORDER BY u.nombre, u.id_usuario";
            try (PreparedStatement ps = conexion.prepareStatement(sqlFallback)) {
                ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
                ps.setTimestamp(2, Timestamp.valueOf(hastaExclusiva.atStartOfDay()));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        VentaCajero fila = new VentaCajero(
                                rs.getInt("id_usuario"),
                                rs.getString("nombre"),
                                rs.getLong("cantidad"),
                                rs.getBigDecimal("total")
                        );
                        filas.add(fila);
                        cantidad += fila.tickets();
                        total = total.add(fila.total());
                    }
                }
            } catch (SQLException e2) {
                throw new AccesoDatosException(e);
            }
        }

        // Consultar ventas consolidadas por caja
        List<VentaPorCaja> cajas = new ArrayList<>();
        String sqlCajas = "SELECT "
                + "    sub.id_caja, "
                + "    sub.nombre_caja, "
                + "    COUNT(sub.id_ticket) AS tickets, "
                + "    COALESCE(SUM(sub.monto), 0) AS total, "
                + "    COUNT(DISTINCT sub.id_usuario) AS cajeros_distintos "
                + "FROM ( "
                + "    SELECT "
                + "        t.id_ticket, "
                + "        t.monto, "
                + "        u.id_usuario, "
                + "        COALESCE(c.id_caja, COALESCE(t.id_caja, 1)) AS id_caja, "
                + "        COALESCE(c.nombre, 'Caja ' || COALESCE(c.numero_caja, t.id_caja, 1)) AS nombre_caja "
                + "    FROM Ticket t "
                + "    LEFT JOIN Caja c ON t.id_caja = c.id_caja "
                + "    LEFT JOIN Usuario u ON t.id_usuario = u.id_usuario "
                + "    WHERE t.fecha_hora_compra >= ? AND t.fecha_hora_compra < ? "
                + ") sub "
                + "GROUP BY sub.id_caja, sub.nombre_caja "
                + "ORDER BY sub.id_caja";

        try (PreparedStatement ps = conexion.prepareStatement(sqlCajas)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hastaExclusiva.atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cajas.add(new VentaPorCaja(
                            rs.getInt("id_caja"),
                            rs.getString("nombre_caja"),
                            rs.getLong("tickets"),
                            rs.getBigDecimal("total"),
                            rs.getInt("cajeros_distintos")
                    ));
                }
            }
        } catch (SQLException ignored) {
            // No bloqueante si la estructura aún no tiene cajas
        }

        return new Reporte(List.copyOf(filas), cantidad, total, List.copyOf(cajas));
    }
}
