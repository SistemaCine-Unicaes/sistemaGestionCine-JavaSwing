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
    private static volatile models.Caja cajaActual = null;

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

    public static void setCajaActual(models.Caja caja) {
        cajaActual = caja;
    }

    public static models.Caja getCajaActual() {
        return cajaActual;
    }

    public static int getIdCajaActual() {
        return cajaActual != null ? cajaActual.getIdCaja() : 1;
    }

    // Método para limpiar la sesión cuando se hace Logout
    public static void cerrarSesion() {
        if (cajaActual != null && usuarioActual != null) {
            try {
                new dao.CajaDAO().cerrarCaja(cajaActual.getIdCaja(), usuarioActual.getIdUsuario());
            } catch (Exception ignored) {}
        }
        usuarioActual = null;
        cajaActual = null;
    }
    
    // Método de utilidad para saber si hay alguien logueado
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static boolean esAdministrador() {
        return esAdministrador(usuarioActual);
    }

    // Roles aceptados por el sistema; "Admin" se conserva por los usuarios registrados antes del módulo de usuarios.
    public static boolean esAdministrador(Usuario usuario) {
        return usuario != null && ("Admin".equalsIgnoreCase(usuario.getRol())
                || "Administrador".equalsIgnoreCase(usuario.getRol()));
    }

    public static boolean esCajero(Usuario usuario) {
        return usuario != null && "Cajero".equalsIgnoreCase(usuario.getRol());
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
        if (!esAdministrador(usuario) && !esCajero(usuario)) {
            throw new IllegalStateException("Tu usuario no tiene permiso para vender boletos.");
        }
        return usuario;
    }
}
