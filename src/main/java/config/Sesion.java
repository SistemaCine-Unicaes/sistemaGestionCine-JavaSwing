/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import models.Usuario;

/**
 *
 * @author axele
 */
public class Sesion {
    // Variable estática que almacena al usuario que inició sesión
    private static volatile Usuario usuarioActual = null;

    // Constructor privado para evitar que alguien use "new Sesion()" por error
    private Sesion() {
    }

    // Método para guardar el usuario una vez que el login es exitoso
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    // Método para consultar quién está logueado desde cualquier parte del programa
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    // Método para limpiar la sesión cuando se hace Logout
    public static void cerrarSesion() {
        usuarioActual = null;
    }
    
    // Método de utilidad para saber si hay alguien logueado
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static boolean esAdministrador() {
        Usuario usuario = usuarioActual;
        return usuario != null && ("Admin".equalsIgnoreCase(usuario.getRol())
                || "Administrador".equalsIgnoreCase(usuario.getRol()));
    }

    public static Usuario exigirSesion() {
        Usuario usuario = usuarioActual;
        if (usuario == null) throw new IllegalStateException("Debes iniciar sesión.");
        return usuario;
    }

    public static void exigirAdministrador() {
        exigirSesion();
        if (!esAdministrador()) throw new IllegalStateException("Esta operación requiere un administrador.");
    }

    public static Usuario exigirVenta() {
        Usuario usuario = exigirSesion();
        if (!esAdministrador() && !"Cajero".equalsIgnoreCase(usuario.getRol())) {
            throw new IllegalStateException("Tu usuario no tiene permiso para vender boletos.");
        }
        return usuario;
    }
}
