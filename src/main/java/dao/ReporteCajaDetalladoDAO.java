package dao;

import config.Conexion;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Consulta de ventas y auditoría de uso por caja física.
 * Permite saber qué cajeros/usuarios operaron cada caja y cuánto generaron.
 */
public class ReporteCajaDetalladoDAO {
    private final Connection conexionExterna;

    public ReporteCajaDetalladoDAO() {
        this(null);
    }

    public ReporteCajaDetalladoDAO(Connection conexion) {
        this.conexionExterna = conexion;
    }

    private Connection getCon() throws SQLException {
        return conexionExterna != null ? conexionExterna : Conexion.getConexion();
    }

    private void cerrarSiPropia(Connection c) {
        if (conexionExterna == null && c != null) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }

    public record UsoCaja(
            int idCaja,
            String nombreCaja,
            int idUsuario,
            String nombreUsuario,
            String rol,
            long ticketsVendidos,
            BigDecimal totalRecaudado,
            Timestamp primeraVenta,
            Timestamp ultimaVenta
    ) {}

    public record ReporteCajas(
            List<UsoCaja> filas,
            long totalTickets,
            BigDecimal totalRecaudado,
            int cajasOperadas,
            int usuariosDistintos
    ) {}

    public ReporteCajas obtenerUsoPorCaja(String filtroCaja, LocalDate desde, LocalDate hastaExclusiva) {
        try {
            new CajaDAO(conexionExterna).asegurarEstructura();
        } catch (Exception ignored) {}

        LocalDate fDesde = (desde != null) ? desde : LocalDate.of(2000, 1, 1);
        LocalDate fHasta = (hastaExclusiva != null) ? hastaExclusiva : LocalDate.now().plusYears(50);
        String f = filtroCaja != null ? filtroCaja.trim() : "Todas las cajas";

        // Consulta primaria con subconsulta para garantizar agrupación válida en PostgreSQL
        String sql = "SELECT "
                + "    sub.id_caja, "
                + "    sub.nombre_caja, "
                + "    sub.id_usuario, "
                + "    sub.nombre_usuario, "
                + "    sub.rol, "
                + "    COUNT(sub.id_ticket) AS tickets_vendidos, "
                + "    COALESCE(SUM(sub.monto), 0) AS total_recaudado, "
                + "    MIN(sub.fecha_hora_compra) AS primera_venta, "
                + "    MAX(sub.fecha_hora_compra) AS ultima_venta "
                + "FROM ( "
                + "    SELECT "
                + "        t.id_ticket, "
                + "        t.monto, "
                + "        t.fecha_hora_compra, "
                + "        COALESCE(c.id_caja, COALESCE(t.id_caja, 1)) AS id_caja, "
                + "        COALESCE(c.nombre, 'Caja ' || COALESCE(c.numero_caja, t.id_caja, 1)) AS nombre_caja, "
                + "        u.id_usuario, "
                + "        u.nombre AS nombre_usuario, "
                + "        COALESCE(u.rol::text, 'Cajero') AS rol "
                + "    FROM Ticket t "
                + "    JOIN Usuario u ON t.id_usuario = u.id_usuario "
                + "    LEFT JOIN Caja c ON t.id_caja = c.id_caja "
                + "    WHERE t.fecha_hora_compra >= ? AND t.fecha_hora_compra < ? "
                + "      AND (? = 'Todas las cajas' "
                + "           OR c.nombre = ? "
                + "           OR ('Caja ' || COALESCE(c.numero_caja, t.id_caja, 1)) = ? "
                + "           OR c.id_caja::text = ? "
                + "           OR COALESCE(t.id_caja, 1)::text = ?) "
                + ") sub "
                + "GROUP BY sub.id_caja, sub.nombre_caja, sub.id_usuario, sub.nombre_usuario, sub.rol "
                + "ORDER BY sub.id_caja, total_recaudado DESC";

        Connection c = null;
        try {
            c = getCon();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(fDesde.atStartOfDay()));
                ps.setTimestamp(2, Timestamp.valueOf(fHasta.atStartOfDay()));
                ps.setString(3, f);
                ps.setString(4, f);
                ps.setString(5, f);
                ps.setString(6, f);
                ps.setString(7, f);

                return procesarFilas(ps);
            }
        } catch (SQLException e) {
            // Fallback si la tabla Caja o columna id_caja aún no están disponibles
            String sqlFallback = "SELECT "
                    + "    1 AS id_caja, "
                    + "    'Caja 01 - Taquilla Principal' AS nombre_caja, "
                    + "    u.id_usuario, "
                    + "    u.nombre AS nombre_usuario, "
                    + "    COALESCE(u.rol::text, 'Cajero') AS rol, "
                    + "    COUNT(t.id_ticket) AS tickets_vendidos, "
                    + "    COALESCE(SUM(t.monto), 0) AS total_recaudado, "
                    + "    MIN(t.fecha_hora_compra) AS primera_venta, "
                    + "    MAX(t.fecha_hora_compra) AS ultima_venta "
                    + "FROM Ticket t "
                    + "JOIN Usuario u ON t.id_usuario = u.id_usuario "
                    + "WHERE t.fecha_hora_compra >= ? AND t.fecha_hora_compra < ? "
                    + "GROUP BY u.id_usuario, u.nombre, u.rol "
                    + "ORDER BY total_recaudado DESC";
            try {
                if (c == null || c.isClosed()) c = getCon();
                try (PreparedStatement psFallback = c.prepareStatement(sqlFallback)) {
                    psFallback.setTimestamp(1, Timestamp.valueOf(fDesde.atStartOfDay()));
                    psFallback.setTimestamp(2, Timestamp.valueOf(fHasta.atStartOfDay()));
                    return procesarFilas(psFallback);
                }
            } catch (SQLException ex) {
                throw new AccesoDatosException(e);
            }
        } finally {
            cerrarSiPropia(c);
        }
    }

    private ReporteCajas procesarFilas(PreparedStatement ps) throws SQLException {
        List<UsoCaja> filas = new ArrayList<>();
        long totalTickets = 0;
        BigDecimal totalRecaudado = BigDecimal.ZERO.setScale(2);
        List<Integer> idsCajas = new ArrayList<>();
        List<Integer> usuarios = new ArrayList<>();

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idCaja = rs.getInt("id_caja");
                int idUser = rs.getInt("id_usuario");
                long tks = rs.getLong("tickets_vendidos");
                BigDecimal tot = rs.getBigDecimal("total_recaudado");

                filas.add(new UsoCaja(
                        idCaja,
                        rs.getString("nombre_caja"),
                        idUser,
                        rs.getString("nombre_usuario"),
                        rs.getString("rol"),
                        tks,
                        tot,
                        rs.getTimestamp("primera_venta"),
                        rs.getTimestamp("ultima_venta")
                ));

                if (!idsCajas.contains(idCaja)) idsCajas.add(idCaja);
                if (!usuarios.contains(idUser)) usuarios.add(idUser);
                totalTickets += tks;
                totalRecaudado = totalRecaudado.add(tot);
            }
        }
        return new ReporteCajas(List.copyOf(filas), totalTickets, totalRecaudado, idsCajas.size(), usuarios.size());
    }

    public List<String> obtenerNombresCajas() {
        try {
            new CajaDAO(conexionExterna).asegurarEstructura();
        } catch (Exception ignored) {}
        List<String> lista = new ArrayList<>();
        Connection c = null;
        try {
            c = getCon();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre FROM public.Caja ORDER BY numero_caja")) {
                while (rs.next()) {
                    lista.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            // Retorna nombres predeterminados si falla la consulta
            lista.addAll(List.of(
                    "Caja 01 - Taquilla Principal",
                    "Caja 02 - Taquilla Central",
                    "Caja 03 - Taquilla Lateral",
                    "Caja 04 - Taquilla Preferencial",
                    "Caja 05 - Taquilla Rápida",
                    "Caja 06 - Taquilla Express"
            ));
        } finally {
            cerrarSiPropia(c);
        }
        return lista;
    }
}
