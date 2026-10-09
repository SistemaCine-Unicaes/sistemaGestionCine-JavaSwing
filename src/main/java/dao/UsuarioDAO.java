/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import config.Conexion;
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
    
    private static final String SUPABASE_URL = config.Configuracion.valor("SUPABASE_AUTH_URL", "https://mmsfhfwdovbthxmjxvfe.supabase.co");
    private static final String SUPABASE_ANON_KEY = config.Configuracion.valor("SUPABASE_AUTH_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tc2ZoZndkb3ZidGh4bWp4dmZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3Mjg4ODMsImV4cCI6MjEwNDMwNDg4M30.gEv3A0ZjPphdf7Mzk5hf9_0VcH_ZhC190d4lY7Tf-0M");
    
    public Usuario autenticarUsuario(String username, String password) {
        validarProyecto();
        Usuario usuarioTemp = null;
        String emailAsociado = null;
        
        // PASO 1: Buscar al usuario por su username para obtener su correo
        String sql = "SELECT * FROM Usuario WHERE username = ? AND estado = 'Activo'";        
        
        try (Connection conn = Conexion.getConexion();
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
        } catch (SQLException e) { throw new AccesoDatosException(e); }
        
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
    
    private void validarProyecto() {
        String proyecto = Conexion.proyecto();
        String host = URI.create(SUPABASE_URL).getHost();
        if (!proyecto.isBlank() && !(proyecto + ".supabase.co").equalsIgnoreCase(host)) {
            throw new IllegalStateException("La base de datos y Supabase Auth apuntan a proyectos distintos. Corrige SUPABASE_AUTH_URL y SUPABASE_AUTH_KEY en cine.local.properties.");
        }
        if (SUPABASE_ANON_KEY.startsWith("eyJ")) {
            try {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(SUPABASE_ANON_KEY.split("\\.")[1]), java.nio.charset.StandardCharsets.UTF_8);
                var m = java.util.regex.Pattern.compile("\"ref\"\\s*:\\s*\"([^\"]+)\"").matcher(payload);
                if (m.find() && !(m.group(1) + ".supabase.co").equalsIgnoreCase(host)) {
                    throw new IllegalStateException("La clave pública de Supabase Auth pertenece a otro proyecto. Corrige SUPABASE_AUTH_KEY.");
                }
            } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("La clave pública de Supabase Auth no es válida.", e);
            }
        }
    }

    private boolean validarCredenciales(String email, String password) {
        return enviarAuth("/auth/v1/token?grant_type=password", email, password);
    }

    private boolean enviarAuth(String endpoint, String email, String password) {
        validarProyecto();
        try {
            String json = "{\"email\":" + utils.Json.texto(email) + ",\"password\":" + utils.Json.texto(password) + "}";
            HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SUPABASE_URL + endpoint))
                    .timeout(java.time.Duration.ofSeconds(20))
                    .header("apikey", SUPABASE_ANON_KEY).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) return true;
            if (response.statusCode() == 400 || response.statusCode() == 422) return false;
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                throw new IllegalStateException("Supabase rechazó el acceso. Revisa la clave pública y la configuración de Auth.");
            }
            throw new IllegalStateException("Supabase Auth no está disponible. Intenta nuevamente más tarde.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La autenticación fue interrumpida.", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo conectar con Supabase Auth. Comprueba la conexión.", e);
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
                        
        try (Connection conn = Conexion.getConexion();
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
        return enviarAuth("/auth/v1/signup", email, password);
    }
}