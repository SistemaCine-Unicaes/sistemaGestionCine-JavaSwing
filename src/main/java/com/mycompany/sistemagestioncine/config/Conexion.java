package com.mycompany.sistemagestioncine.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    
    // Cambia todo lo que está después del @ por el host que te dio Supabase
    // O copia directamente el String desde "jdbc:postgresql://..." hasta "/postgres"
    private static final String URL = "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres";
    private static final String USER = "postgres.jfixhhpchcesqmlffjfx";
    // Coloca la contraseña que creaste en el Paso 1
    private static final String PASSWORD = "2FNeH4WUpNClWjnh";

    public static Connection getConexion() {
        Connection conexion = null;
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a Supabase.");
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
        return conexion;
    }
}