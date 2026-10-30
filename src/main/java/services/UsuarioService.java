package services;

import config.Sesion;
import dao.UsuarioDAO;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import models.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/** Gestión de cajeros y administradores. Solo un administrador activo puede usarla. */
public class UsuarioService {
    /** Crea la cuenta de Supabase Auth; las pruebas la sustituyen para no registrar cuentas reales. */
    @FunctionalInterface public interface RegistroAuth {
        /** @return true si el correo debe confirmarse antes del primer inicio de sesión. */
        boolean registrar(String email, String clave);
    }
    public record Catalogo(List<Usuario> usuarios, List<String> roles, List<String> estados, List<String> generos) {}
    public record Creado(int idUsuario, boolean requiereConfirmacion) {}

    public static final String ACTIVO = "Activo";
    private static final int BLOQUEO_USUARIOS = 20261008;
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._-]{3,30}");
    private static final Pattern CORREO = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    private static final Pattern DUI = Pattern.compile("\\d{8}-\\d");
    private static final Pattern TELEFONO = Pattern.compile("\\+?[0-9][0-9 -]{6,19}");

    private final Supplier<Connection> conexiones;
    private final RegistroAuth auth;

    public UsuarioService() { this(config.Conexion::getConexion, (email, clave) -> new UsuarioDAO().registrarEnAuth(email, clave)); }
    public UsuarioService(Supplier<Connection> conexiones, RegistroAuth auth) {
        this.conexiones = conexiones;
        this.auth = auth;
    }

    public Catalogo cargar() {
        Sesion.exigirAdministrador();
        return Transacciones.ejecutar(conexiones, c -> {
            UsuarioDAO dao = new UsuarioDAO(c);
            List<String> roles = new ArrayList<>();
            for (String rol : dao.valoresPermitidos("rol")) {
                Usuario prueba = new Usuario(); prueba.setRol(rol);
                if (Sesion.esAdministrador(prueba) || Sesion.esCajero(prueba)) roles.add(rol);
            }
            List<String> estados = dao.valoresPermitidos("estado");
            List<String> generos = dao.valoresPermitidos("genero");
            return new Catalogo(dao.listar(), roles.isEmpty() ? List.of("Cajero", "Administrador") : roles,
                    estados.contains(ACTIVO) ? estados : List.of(ACTIVO, "Inactivo"),
                    generos.isEmpty() ? List.of("Femenino", "Masculino", "Otro") : generos);
        });
    }

    public Creado crear(Usuario usuario, char[] clave, char[] confirmacion) {
        Sesion.exigirAdministrador();
        validar(usuario);
        String texto = validarClave(clave, confirmacion);
        // Se guarda el hash como en el registro original; el inicio de sesión lo valida Supabase Auth.
        String hash = BCrypt.hashpw(texto, BCrypt.gensalt(12));
        return Transacciones.ejecutar(conexiones, c -> {
            bloquear(c);
            UsuarioDAO dao = new UsuarioDAO(c);
            usuario.setIdUsuario(0);
            verificarDuplicados(dao, usuario);
            int id = dao.insertar(usuario, hash);
            // Si Supabase rechaza la cuenta, la excepción revierte también el registro en la tabla Usuario.
            boolean confirmar = auth.registrar(usuario.getEmail(), texto);
            usuario.setIdUsuario(id);
            return new Creado(id, confirmar);
        });
    }

    public boolean actualizar(Usuario usuario) {
        Sesion.exigirAdministrador();
        Usuario actual = Sesion.exigirSesion();
        validar(usuario);
        return Transacciones.ejecutar(conexiones, c -> {
            bloquear(c);
            UsuarioDAO dao = new UsuarioDAO(c);
            Usuario anterior = dao.bloquear(usuario.getIdUsuario());
            if (anterior == null) return false;
            boolean sigueAdministrador = administradorActivo(usuario);
            if (usuario.getIdUsuario() == actual.getIdUsuario() && !sigueAdministrador) {
                throw new IllegalStateException("No puedes quitarte el rol de administrador ni desactivar tu propio usuario.");
            }
            if (administradorActivo(anterior) && !sigueAdministrador && dao.contarAdministradoresActivos(usuario.getIdUsuario()) == 0) {
                throw new IllegalStateException("Debe quedar al menos un administrador activo.");
            }
            usuario.setEmail(anterior.getEmail());
            verificarDuplicados(dao, usuario);
            return dao.actualizar(usuario);
        });
    }

    public boolean eliminar(int idUsuario) {
        Sesion.exigirAdministrador();
        if (idUsuario == Sesion.exigirSesion().getIdUsuario()) throw new IllegalStateException("No puedes eliminar tu propio usuario.");
        return Transacciones.ejecutar(conexiones, c -> {
            bloquear(c);
            UsuarioDAO dao = new UsuarioDAO(c);
            Usuario anterior = dao.bloquear(idUsuario);
            if (anterior == null) return false;
            if (administradorActivo(anterior) && dao.contarAdministradoresActivos(idUsuario) == 0) {
                throw new IllegalStateException("Debe quedar al menos un administrador activo.");
            }
            if (dao.tieneVentas(idUsuario)) {
                throw new IllegalStateException("El usuario tiene ventas registradas que se conservan en el corte de caja. "
                        + "Cámbialo a Inactivo para impedir su acceso.");
            }
            return dao.eliminar(idUsuario);
        });
    }

    /** Serializa los cambios de usuarios para que dos administradores no dejen el sistema sin administradores. */
    private static void bloquear(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT pg_advisory_xact_lock(?, 1)")) {
            ps.setInt(1, BLOQUEO_USUARIOS); ps.execute();
        }
    }

    private static boolean administradorActivo(Usuario usuario) {
        return Sesion.esAdministrador(usuario) && ACTIVO.equals(usuario.getEstado());
    }

    private static void verificarDuplicados(UsuarioDAO dao, Usuario usuario) {
        String campo = dao.campoDuplicado(usuario);
        if (campo == null) return;
        throw new IllegalArgumentException(switch (campo) {
            case "username" -> "El nombre de usuario «" + usuario.getUsername() + "» ya está registrado.";
            case "email" -> "El correo " + usuario.getEmail() + " ya está registrado para otro usuario.";
            default -> "El DUI " + usuario.getDui() + " ya está registrado para otro usuario.";
        });
    }

    public static void validar(Usuario u) {
        if (u.getNombre() == null || u.getNombre().isBlank()) throw new IllegalArgumentException("Ingresa el nombre completo.");
        longitud(u.getNombre(), "El nombre", 100);
        if (u.getUsername() == null || !USERNAME.matcher(u.getUsername()).matches()) {
            throw new IllegalArgumentException("El usuario debe tener de 3 a 30 letras sin acentos, números, puntos, guiones o guiones bajos, sin espacios.");
        }
        if (u.getEmail() == null || u.getEmail().length() > 254 || !CORREO.matcher(u.getEmail()).matches()) {
            throw new IllegalArgumentException("Ingresa un correo válido, por ejemplo cajero@cine.com.");
        }
        if (!Sesion.esAdministrador(u) && !Sesion.esCajero(u)) throw new IllegalArgumentException("Elige el rol Cajero o Administrador.");
        if (u.getEstado() == null || u.getEstado().isBlank()) throw new IllegalArgumentException("Elige el estado del usuario.");
        if (u.getDui() != null && !DUI.matcher(u.getDui()).matches()) {
            throw new IllegalArgumentException("Ingresa el DUI con el formato 12345678-9 o déjalo vacío.");
        }
        if (u.getTelefono() != null && !TELEFONO.matcher(u.getTelefono()).matches()) {
            throw new IllegalArgumentException("Ingresa un teléfono válido, por ejemplo 7777-8888, o déjalo vacío.");
        }
        longitud(u.getDireccion(), "La dirección", 200);
        LocalDate hoy = LocalDate.now(java.time.ZoneId.of(config.Configuracion.valor("CINE_ZONA_HORARIA", "America/El_Salvador")));
        LocalDate nacimiento = u.getFechaNacimiento() == null ? null : u.getFechaNacimiento().toLocalDate();
        if (nacimiento != null && (!nacimiento.isBefore(hoy) || nacimiento.getYear() < 1900)) {
            throw new IllegalArgumentException("La fecha de nacimiento debe ser anterior a hoy.");
        }
        if (u.getFechaContratacion() != null && nacimiento != null && !u.getFechaContratacion().toLocalDate().isAfter(nacimiento)) {
            throw new IllegalArgumentException("La fecha de contratación debe ser posterior a la fecha de nacimiento.");
        }
    }

    /** Supabase Auth y BCrypt admiten como máximo 72 bytes. */
    public static String validarClave(char[] clave, char[] confirmacion) {
        if (clave == null || clave.length < 8) throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        if (!Arrays.equals(clave, confirmacion)) throw new IllegalArgumentException("La contraseña y su confirmación no coinciden.");
        String texto = new String(clave);
        if (texto.getBytes(StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("La contraseña admite hasta 72 caracteres.");
        if (texto.chars().noneMatch(Character::isLetter) || texto.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("La contraseña debe combinar letras y números.");
        }
        return texto;
    }

    private static void longitud(String texto, String campo, int limite) {
        if (texto != null && texto.codePointCount(0, texto.length()) > limite) {
            throw new IllegalArgumentException(campo + " admite hasta " + limite + " caracteres.");
        }
    }
}
