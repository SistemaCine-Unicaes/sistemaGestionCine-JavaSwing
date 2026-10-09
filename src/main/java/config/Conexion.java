package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    
    // Cambia todo lo que está después del @ por el host que te dio Supabase
    // O copia directamente el String desde "jdbc:postgresql://..." hasta "/postgres"
    private static final String URL = Configuracion.valor("CINE_DB_URL", "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres");
    private static final String USER = Configuracion.valor("CINE_DB_USER", "postgres.jfixhhpchcesqmlffjfx");
    // Coloca la contraseña que creaste en el Paso 1
    private static final String PASSWORD = Configuracion.valor("CINE_DB_PASSWORD", "2FNeH4WUpNClWjnh");

    public static String proyecto() {
        if (USER.startsWith("postgres.")) return USER.substring("postgres.".length());
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("db\\.([a-z0-9]+)\\.supabase\\.co").matcher(URL);
        return m.find() ? m.group(1) : "";
    }

    public static Connection getConexion() {
        java.util.Properties propiedades = new java.util.Properties();
        propiedades.setProperty("user", USER);
        propiedades.setProperty("password", PASSWORD);
        // Evita conflictos de sentencias con nombre (S_1, etc.) al reutilizar sesiones del pooler.
        // Los PreparedStatement de JDBC siguen usando parámetros.
        propiedades.setProperty("prepareThreshold", "0");
        propiedades.setProperty("connectTimeout", "10");
        propiedades.setProperty("socketTimeout", "30");
        propiedades.setProperty("options", "-c statement_timeout=20000 -c lock_timeout=10000");
        try {
            Connection conexion = DriverManager.getConnection(URL, propiedades);
            try (java.sql.PreparedStatement ps = conexion.prepareStatement("SELECT set_config('TimeZone', ?, false)")) {
                String zona = Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador");
                java.time.ZoneId.of(zona);
                ps.setString(1, zona);
                ps.execute();
                return conexion;
            } catch (SQLException | RuntimeException e) {
                try { conexion.close(); } catch (SQLException cierre) { e.addSuppressed(cierre); }
                throw e;
            }
        } catch (SQLException e) {
            throw new dao.AccesoDatosException(e);
        }
    }
}
