package services;

import config.Conexion;
import dao.AccesoDatosException;
import java.sql.Connection;
import java.sql.SQLException;

public final class Transacciones {
    private Transacciones() {}
    @FunctionalInterface public interface Trabajo<T> { T ejecutar(Connection c) throws SQLException; }

    public static <T> T ejecutar(Trabajo<T> trabajo) {
        return ejecutar(Conexion::getConexion, trabajo);
    }

    public static <T> T ejecutar(java.util.function.Supplier<Connection> conexiones, Trabajo<T> trabajo) {
        try (Connection c = conexiones.get()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
