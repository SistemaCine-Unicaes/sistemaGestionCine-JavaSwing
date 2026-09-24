package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    
    private static final String URL = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres";
    private static final String USER = "postgres.jfixhhpchcesqmlffjfx";
    private static final String PASSWORD = "2FNeH4WUpNClWjnh";
    
    private static Connection conexion = null;

    public static Connection conectar() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("org.postgresql.Driver");
                
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ ¡Éxito! Conectado a PostgreSQL en Supabase.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERROR: No se encontró el driver JDBC. ¿Agregaste el .jar a las librerías?");
        } catch (SQLException e) {
            System.err.println("❌ ERROR DE CONEXIÓN");
            System.err.println("Detalle: " + e.getMessage());
        }
        return conexion;
    }

    public static void desconectar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("🔌 Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al cerrar la conexión: " + e.getMessage());
        }
    }


    public static void main(String[] args) {

        conectar();
        
      
        desconectar();
    }
}