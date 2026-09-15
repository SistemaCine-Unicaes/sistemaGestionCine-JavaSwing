/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.ConexionDB;
import models.Usuario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author axele
 */
public class UsuarioDAO {
    
    private static final String SUPABASE_URL = "https://mmsfhfwdovbthxmjxvfe.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tc2ZoZndkb3ZidGh4bWp4dmZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3Mjg4ODMsImV4cCI6MjEwNDMwNDg4M30.gEv3A0ZjPphdf7Mzk5hf9_0VcH_ZhC190d4lY7Tf-0M";
    
    public Usuario autenticarUsuario(String username, String password) {
        Usuario usuarioTemp = null;
        String emailAsociado = null;
        
        // PASO 1: Buscar al usuario por su username para obtener su correo
        String sql = "SELECT * FROM Usuario WHERE username = ? AND estado = 'Activo'";        
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    emailAsociado = rs.getString("email");
                    
                    usuarioTemp = new Usuario();
                    usuarioTemp.setIdUsuario(rs.getInt("id_usuario"));
                    usuarioTemp.setRol(rs.getString("rol"));
                    usuarioTemp.setNombre(rs.getString("nombre"));
                    usuarioTemp.setUsername(rs.getString("username"));
                    usuarioTemp.setPasswordHash(rs.getString("password_hash"));
                    usuarioTemp.setDui(rs.getString("dui"));
                    usuarioTemp.setEmail(rs.getString("email"));
                    usuarioTemp.setTelefono(rs.getString("telefono"));
                    usuarioTemp.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                    usuarioTemp.setGenero(rs.getString("genero"));
                    usuarioTemp.setDireccion(rs.getString("direccion"));
                    usuarioTemp.setFechaContratacion(rs.getDate("fecha_contratacion"));
                    usuarioTemp.setImagenUrl(rs.getString("imagen_url"));
                    usuarioTemp.setEstado(rs.getString("estado"));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Falla al buscar el username en la BD.");
            System.err.println("Detalle: " + e.getMessage());
            return null;
        }
        
        if (emailAsociado == null || emailAsociado.isEmpty()) {
            System.out.println("El usuario no existe o no se encontro un correo asociado.");
            return null;
        }
        
        // PASO 2: Enviar el correo encontrado y el password a Supabase Auth
        boolean authExitoso = validarCredenciales(emailAsociado, password);
        
        if (!authExitoso) {
            System.out.println("Credenciales invalidas verificadas por Supabase Auth.");
            return null;
        }
        
        // Si la API de Supabase aprueba el login, retornamos los datos cargados en el Paso 1
        return usuarioTemp;
    }
    
    private boolean validarCredenciales(String email, String password) {
        try {
            String jsonBody = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/auth/v1/token?grant_type=password"))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            System.err.println("ERROR: Falla al conectar con el servidor de autenticación de Supabase.");
            return false;
        }
    }
    
    public boolean registrarUsuario(Usuario usuario, String passwordHash) {
        
        boolean authExitoso = crearUsuarioEnSupabaseAuth(usuario.getEmail(), passwordHash);
        
        if (!authExitoso) {
            System.out.println("Error: No se pudo registrar el usuario en Supabase Auth.");
            return false;
        }

        String hash = BCrypt.hashpw(passwordHash, BCrypt.gensalt(12));
        
        String sql = "INSERT INTO Usuario (rol, nombre, username, password_hash, dui, email, telefono, fecha_nacimiento, genero, direccion, fecha_contratacion, imagen_url, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::estado_usuario)";
                        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usuario.getRol());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getUsername());
            ps.setString(4, hash);
            ps.setString(5, usuario.getDui());
            ps.setString(6, usuario.getEmail());
            ps.setString(7, usuario.getTelefono());
            ps.setDate(8, usuario.getFechaNacimiento());
            ps.setString(9, usuario.getGenero());
            ps.setString(10, usuario.getDireccion());
            ps.setDate(11, usuario.getFechaContratacion());
            ps.setString(12, usuario.getImagenUrl());
            ps.setString(13, usuario.getEstado());
            
            int filas = ps.executeUpdate();
            return filas > 0;
            
        } catch (SQLException e) {
            System.err.println("ERROR: Falla al insertar el usuario en la tabla pública.");
            System.err.println("Detalle: " + e.getMessage());
            return false;
        }
    }

    private boolean crearUsuarioEnSupabaseAuth(String email, String password) {
        try {
            String jsonBody = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/auth/v1/signup"))
                    .header("apikey", SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("ERROR: Falla de red al conectar con Supabase Auth.");
            return false;
        }
    }
    
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();
        
        // Prueba ahora enviando el username en lugar del email
        String testUsername = "admin"; // Asegúrate de que exista un usuario con este username y correo asignado en tu tabla
        String testPass = "123456"; 
        
        System.out.println("Intentando iniciar sesion con el username: " + testUsername);
        Usuario userLogueado = dao.autenticarUsuario(testUsername, testPass);
        
        if (userLogueado != null) {
            System.out.println("¡Login Exitoso a traves del puente de Supabase Auth!");
            System.out.println("Bienvenido " + userLogueado.getNombre() + " | Correo utilizado internamente: " + userLogueado.getEmail());
        } else {
            System.out.println("Acceso denegado.");
        }
    }
}