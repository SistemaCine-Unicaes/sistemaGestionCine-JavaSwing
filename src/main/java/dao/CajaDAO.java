package dao;

import config.Conexion;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Caja;

/**
 * Acceso a datos para la gestión y asignación de Cajas de atención.
 */
public class CajaDAO {
    private final Connection conexionExterna;

    public CajaDAO() {
        this(null);
    }

    public CajaDAO(Connection conexion) {
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

    /**
     * Garantiza que la tabla Caja y las cajas predefinidas existan.
     */
    public void asegurarEstructura() {
        Connection c = null;
        try {
            c = getCon();
            try (Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS public.Caja ("
                        + "id_caja SERIAL PRIMARY KEY, "
                        + "numero_caja integer NOT NULL UNIQUE, "
                        + "nombre text NOT NULL, "
                        + "estado text NOT NULL DEFAULT 'DISPONIBLE', "
                        + "id_usuario_actual integer, "
                        + "fecha_apertura timestamp without time zone)");

                st.execute("CREATE TABLE IF NOT EXISTS public.SesionCaja ("
                        + "id_sesion_caja SERIAL PRIMARY KEY, "
                        + "id_caja integer NOT NULL, "
                        + "id_usuario integer NOT NULL, "
                        + "fecha_inicio timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "fecha_fin timestamp without time zone, "
                        + "total_vendido numeric(10,2) DEFAULT 0.00, "
                        + "tickets_vendidos integer DEFAULT 0)");

                try {
                    st.execute("ALTER TABLE public.Ticket ADD COLUMN IF NOT EXISTS id_caja integer");
                } catch (Exception ignored) {}

                // Sembrar cajas iniciales si está vacía
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM public.Caja")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        st.execute("INSERT INTO public.Caja (numero_caja, nombre, estado) VALUES "
                                + "(1, 'Caja 01 - Taquilla Principal', 'DISPONIBLE'), "
                                + "(2, 'Caja 02 - Taquilla Central', 'DISPONIBLE'), "
                                + "(3, 'Caja 03 - Taquilla Lateral', 'DISPONIBLE'), "
                                + "(4, 'Caja 04 - Taquilla Preferencial', 'DISPONIBLE'), "
                                + "(5, 'Caja 05 - Taquilla Rápida', 'DISPONIBLE'), "
                                + "(6, 'Caja 06 - Taquilla Express', 'DISPONIBLE')");
                    }
                }
            }
        } catch (Exception e) {
            // Silencioso para no romper si no hay conexión en pruebas aisladas
        } finally {
            cerrarSiPropia(c);
        }
    }

    public List<Caja> listarTodas() {
        asegurarEstructura();
        List<Caja> lista = new ArrayList<>();
        Connection c = null;
        String sql = "SELECT id_caja, numero_caja, nombre, estado, id_usuario_actual, fecha_apertura "
                   + "FROM public.Caja ORDER BY numero_caja";
        try {
            c = getCon();
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
        return lista;
    }

    public List<Caja> listarDisponibles(int idUsuario) {
        asegurarEstructura();
        List<Caja> lista = new ArrayList<>();
        Connection c = null;
        String sql = "SELECT id_caja, numero_caja, nombre, estado, id_usuario_actual, fecha_apertura "
                   + "FROM public.Caja "
                   + "WHERE estado = 'DISPONIBLE' OR id_usuario_actual = ? "
                   + "ORDER BY numero_caja";
        try {
            c = getCon();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
        return lista;
    }

    public Caja obtenerPorId(int idCaja) {
        asegurarEstructura();
        Connection c = null;
        String sql = "SELECT id_caja, numero_caja, nombre, estado, id_usuario_actual, fecha_apertura "
                   + "FROM public.Caja WHERE id_caja = ?";
        try {
            c = getCon();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idCaja);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
    }

    public Caja obtenerCajaActivaDeUsuario(int idUsuario) {
        asegurarEstructura();
        Connection c = null;
        String sql = "SELECT id_caja, numero_caja, nombre, estado, id_usuario_actual, fecha_apertura "
                   + "FROM public.Caja WHERE id_usuario_actual = ? AND estado = 'OCUPADA'";
        try {
            c = getCon();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
    }

    public boolean abrirCaja(int idCaja, int idUsuario) {
        asegurarEstructura();
        Connection c = null;
        String updateCaja = "UPDATE public.Caja SET estado = 'OCUPADA', id_usuario_actual = ?, "
                          + "fecha_apertura = CURRENT_TIMESTAMP WHERE id_caja = ?";
        String insertSesion = "INSERT INTO public.SesionCaja (id_caja, id_usuario, fecha_inicio) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try {
            c = getCon();
            boolean autocommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(updateCaja)) {
                    ps.setInt(1, idUsuario);
                    ps.setInt(2, idCaja);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(insertSesion)) {
                    ps.setInt(1, idCaja);
                    ps.setInt(2, idUsuario);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(autocommit);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
    }

    public boolean cerrarCaja(int idCaja, int idUsuario) {
        asegurarEstructura();
        Connection c = null;
        String updateCaja = "UPDATE public.Caja SET estado = 'DISPONIBLE', id_usuario_actual = NULL, "
                          + "fecha_apertura = NULL WHERE id_caja = ?";
        String cerrarSesion = "UPDATE public.SesionCaja "
                            + "SET fecha_fin = CURRENT_TIMESTAMP, "
                            + "total_vendido = COALESCE((SELECT SUM(monto) FROM public.Ticket WHERE id_caja = ? AND id_usuario = ? AND fecha_hora_compra >= fecha_inicio), 0), "
                            + "tickets_vendidos = COALESCE((SELECT COUNT(*) FROM public.Ticket WHERE id_caja = ? AND id_usuario = ? AND fecha_hora_compra >= fecha_inicio), 0) "
                            + "WHERE id_caja = ? AND id_usuario = ? AND fecha_fin IS NULL";
        try {
            c = getCon();
            boolean autocommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(cerrarSesion)) {
                    ps.setInt(1, idCaja);
                    ps.setInt(2, idUsuario);
                    ps.setInt(3, idCaja);
                    ps.setInt(4, idUsuario);
                    ps.setInt(5, idCaja);
                    ps.setInt(6, idUsuario);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(updateCaja)) {
                    ps.setInt(1, idCaja);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(autocommit);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
    }

    public boolean forzarLiberacion(int idCaja) {
        asegurarEstructura();
        Connection c = null;
        String updateCaja = "UPDATE public.Caja SET estado = 'DISPONIBLE', id_usuario_actual = NULL, "
                          + "fecha_apertura = NULL WHERE id_caja = ?";
        String cerrarSesion = "UPDATE public.SesionCaja "
                            + "SET fecha_fin = CURRENT_TIMESTAMP, "
                            + "total_vendido = COALESCE((SELECT SUM(monto) FROM public.Ticket WHERE id_caja = ? AND fecha_hora_compra >= fecha_inicio), 0), "
                            + "tickets_vendidos = COALESCE((SELECT COUNT(*) FROM public.Ticket WHERE id_caja = ? AND fecha_hora_compra >= fecha_inicio), 0) "
                            + "WHERE id_caja = ? AND fecha_fin IS NULL";
        try {
            c = getCon();
            boolean autocommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(cerrarSesion)) {
                    ps.setInt(1, idCaja);
                    ps.setInt(2, idCaja);
                    ps.setInt(3, idCaja);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(updateCaja)) {
                    ps.setInt(1, idCaja);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(autocommit);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
    }

    public record InfoCaja(
            int idCaja,
            int numeroCaja,
            String nombre,
            String estado,
            Integer idUsuarioActual,
            String nombreUsuarioActual,
            Timestamp fechaApertura,
            long ticketsHoy,
            BigDecimal recaudacionHoy
    ) {}

    public List<InfoCaja> listarConResumenHoy() {
        asegurarEstructura();
        List<InfoCaja> lista = new ArrayList<>();
        String sql = "SELECT c.id_caja, c.numero_caja, c.nombre, c.estado, c.id_usuario_actual, c.fecha_apertura, "
                   + "       u.nombre AS nombre_usuario, "
                   + "       COUNT(t.id_ticket) AS tickets_hoy, "
                   + "       COALESCE(SUM(t.monto), 0) AS recaudacion_hoy "
                   + "FROM public.Caja c "
                   + "LEFT JOIN public.Usuario u ON c.id_usuario_actual = u.id_usuario "
                   + "LEFT JOIN public.Ticket t ON c.id_caja = t.id_caja AND t.fecha_hora_compra >= CURRENT_DATE "
                   + "GROUP BY c.id_caja, c.numero_caja, c.nombre, c.estado, c.id_usuario_actual, c.fecha_apertura, u.nombre "
                   + "ORDER BY c.numero_caja";
        Connection c = null;
        try {
            c = getCon();
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    lista.add(new InfoCaja(
                            rs.getInt("id_caja"),
                            rs.getInt("numero_caja"),
                            rs.getString("nombre"),
                            rs.getString("estado"),
                            (Integer) rs.getObject("id_usuario_actual"),
                            rs.getString("nombre_usuario"),
                            rs.getTimestamp("fecha_apertura"),
                            rs.getLong("tickets_hoy"),
                            rs.getBigDecimal("recaudacion_hoy")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        } finally {
            cerrarSiPropia(c);
        }
        return lista;
    }

    private static Caja mapear(ResultSet rs) throws SQLException {
        Integer idUsuario = rs.getInt("id_usuario_actual");
        if (rs.wasNull()) idUsuario = null;
        return new Caja(
                rs.getInt("id_caja"),
                rs.getInt("numero_caja"),
                rs.getString("nombre"),
                rs.getString("estado"),
                idUsuario,
                rs.getTimestamp("fecha_apertura")
        );
    }
}
