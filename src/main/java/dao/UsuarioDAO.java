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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author axele
 */
public class UsuarioDAO {
    
    private static final String SUPABASE_URL = config.Configuracion.valor("SUPABASE_AUTH_URL", "https://mmsfhfwdovbthxmjxvfe.supabase.co");
    private static final String SUPABASE_ANON_KEY = config.Configuracion.valor("SUPABASE_AUTH_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1tc2ZoZndkb3ZidGh4bWp4dmZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODg3Mjg4ODMsImV4cCI6MjEwNDMwNDg4M30.gEv3A0ZjPphdf7Mzk5hf9_0VcH_ZhC190d4lY7Tf-0M");
    // La contraseña nunca se lee en el listado; el rol y el género se devuelven como texto aunque sean enumeraciones.
    private static final String COLUMNAS = "id_usuario,rol::text AS rol,nombre,username,dui,email,telefono,fecha_nacimiento,"
            + "genero::text AS genero,direccion,fecha_contratacion,imagen_url,estado::text AS estado";

    // El login abre su propia conexión; la gestión de usuarios recibe la conexión de la transacción.
    private final Connection conexion;
    public UsuarioDAO() { this(null); }
    public UsuarioDAO(Connection conexion) { this.conexion = conexion; }

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
        HttpResponse<String> response = enviarAuth("/auth/v1/token?grant_type=password", email, password);
        return interpretarInicio(response.statusCode(), response.body());
    }

    static boolean interpretarInicio(int estado, String cuerpo) {
        if (estado == 200 || estado == 201) return true;
        if (estado == 400 || estado == 422) {
            if (cuerpo != null && cuerpo.contains("email_not_confirmed")) {
                throw new IllegalStateException("Tu correo aún no está confirmado. Abre el enlace que Supabase envió a tu correo y vuelve a intentar.");
            }
            return false;
        }
        throw errorAuth(estado);
    }

    /**
     * Crea la cuenta en Supabase Auth con la clave pública.
     * @return true si Supabase exige confirmar el correo antes del primer inicio de sesión.
     */
    public boolean registrarEnAuth(String email, String password) {
        HttpResponse<String> response = enviarAuth("/auth/v1/signup", email, password);
        return interpretarRegistro(response.statusCode(), response.body());
    }

    static boolean interpretarRegistro(int estado, String respuesta) {
        String cuerpo = respuesta == null ? "" : respuesta;
        if (estado == 200 || estado == 201) {
            // Con la confirmación activa, Supabase responde 200 sin identidades cuando el correo ya tenía cuenta.
            if (cuerpo.matches("(?s).*\"identities\"\\s*:\\s*\\[\\s*\\].*")) throw correoRegistrado();
            return !cuerpo.contains("\"access_token\"");
        }
        if (estado == 429) {
            throw new IllegalStateException("Supabase alcanzó su límite de registros o correos. Espera unos minutos e intenta nuevamente.");
        }
        if (estado == 400 || estado == 422) {
            if (cuerpo.contains("user_already_exists") || cuerpo.contains("already registered")) throw correoRegistrado();
            if (cuerpo.contains("weak_password")) {
                throw new IllegalArgumentException("Supabase rechazó la contraseña por ser débil. Usa una más larga, con letras y números.");
            }
            if (cuerpo.contains("signup_disabled")) {
                throw new IllegalStateException("El registro de cuentas está deshabilitado en Supabase Auth.");
            }
            if (cuerpo.contains("email_address_invalid") || cuerpo.contains("email_address_not_authorized")) {
                throw new IllegalArgumentException("Supabase rechazó el correo. Usa una dirección de correo válida.");
            }
            throw new IllegalArgumentException("Supabase Auth rechazó el registro. Revisa el correo y la contraseña.");
        }
        throw errorAuth(estado);
    }

    private static IllegalArgumentException correoRegistrado() {
        return new IllegalArgumentException("El correo ya tiene una cuenta en Supabase Auth. Usa otro correo.");
    }

    private static IllegalStateException errorAuth(int estado) {
        if (estado == 401 || estado == 403) {
            return new IllegalStateException("Supabase rechazó el acceso. Revisa la clave pública y la configuración de Auth.");
        }
        return new IllegalStateException("Supabase Auth no está disponible. Intenta nuevamente más tarde.");
    }

    private HttpResponse<String> enviarAuth(String endpoint, String email, String password) {
        validarProyecto();
        try {
            String json = "{\"email\":" + utils.Json.texto(email) + ",\"password\":" + utils.Json.texto(password) + "}";
            HttpClient client = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SUPABASE_URL + endpoint))
                    .timeout(java.time.Duration.ofSeconds(20))
                    .header("apikey", SUPABASE_ANON_KEY).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La autenticación fue interrumpida.", e);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("No se pudo conectar con Supabase Auth. Comprueba la conexión.", e);
        }
    }

    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT " + COLUMNAS + " FROM Usuario ORDER BY nombre,id_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    /** Bloquea la fila hasta terminar la transacción. */
    public Usuario bloquear(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT " + COLUMNAS + " FROM Usuario WHERE id_usuario=? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    static Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id_usuario"), rs.getString("rol"), rs.getString("nombre"), rs.getString("username"), null,
                rs.getString("dui"), rs.getString("email"), rs.getString("telefono"), rs.getDate("fecha_nacimiento"),
                rs.getString("genero"), rs.getString("direccion"), rs.getDate("fecha_contratacion"),
                rs.getString("imagen_url"), rs.getString("estado"));
    }

    /** @return "username", "email" o "dui" si otro usuario ya lo usa; null si no hay coincidencias. */
    public String campoDuplicado(Usuario u) {
        String sql = "SELECT COALESCE(bool_or(lower(username)=lower(?)),false),COALESCE(bool_or(lower(email)=lower(?)),false),"
                + "COALESCE(bool_or(dui=?),false) FROM Usuario WHERE id_usuario<>?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, u.getUsername()); ps.setString(2, u.getEmail());
            ps.setString(3, u.getDui()); ps.setInt(4, u.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1) ? "username" : rs.getBoolean(2) ? "email" : rs.getBoolean(3) ? "dui" : null;
            }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public int insertar(Usuario u, String passwordHash) {
        String sql = "INSERT INTO Usuario(rol,nombre,username,password_hash,dui,email,telefono,fecha_nacimiento,genero,direccion,"
                + "fecha_contratacion,imagen_url,estado) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?::estado_usuario) RETURNING id_usuario";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            // Types.OTHER deja que PostgreSQL convierta el valor al tipo de la columna (texto o enumeración).
            ps.setObject(1, u.getRol(), Types.OTHER); ps.setString(2, u.getNombre());
            ps.setString(3, u.getUsername()); ps.setString(4, passwordHash);
            ps.setString(5, u.getDui()); ps.setString(6, u.getEmail()); ps.setString(7, u.getTelefono());
            ps.setDate(8, u.getFechaNacimiento()); ps.setObject(9, u.getGenero(), Types.OTHER);
            ps.setString(10, u.getDireccion()); ps.setDate(11, u.getFechaContratacion());
            ps.setString(12, u.getImagenUrl()); ps.setString(13, u.getEstado());
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    /** El correo y la contraseña pertenecen a Supabase Auth; aquí no se modifican. */
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE Usuario SET rol=?,nombre=?,username=?,dui=?,telefono=?,fecha_nacimiento=?,genero=?,direccion=?,"
                + "fecha_contratacion=?,estado=?::estado_usuario WHERE id_usuario=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setObject(1, u.getRol(), Types.OTHER); ps.setString(2, u.getNombre()); ps.setString(3, u.getUsername());
            ps.setString(4, u.getDui()); ps.setString(5, u.getTelefono()); ps.setDate(6, u.getFechaNacimiento());
            ps.setObject(7, u.getGenero(), Types.OTHER); ps.setString(8, u.getDireccion());
            ps.setDate(9, u.getFechaContratacion()); ps.setString(10, u.getEstado()); ps.setInt(11, u.getIdUsuario());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public boolean eliminar(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM Usuario WHERE id_usuario=?")) {
            ps.setInt(1, id); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public int contarAdministradoresActivos(int excluido) {
        String sql = "SELECT count(*) FROM Usuario WHERE lower(rol::text) IN ('admin','administrador') "
                + "AND estado::text='Activo' AND id_usuario<>?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, excluido);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public boolean tieneVentas(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT EXISTS(SELECT 1 FROM Ticket WHERE id_usuario=?)")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBoolean(1); }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    /** Valores de la enumeración de una columna de Usuario; vacío si la columna es de texto. */
    public List<String> valoresPermitidos(String columna) {
        String sql = "SELECT e.enumlabel FROM pg_attribute a JOIN pg_enum e ON e.enumtypid=a.atttypid "
                + "WHERE a.attrelid=to_regclass('usuario') AND a.attname=? ORDER BY e.enumsortorder";
        List<String> valores = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, columna);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) valores.add(rs.getString(1)); }
            return valores;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}