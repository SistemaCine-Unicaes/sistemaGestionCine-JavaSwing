/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemagestioncine.controllers;

import com.mycompany.sistemagestioncine.config.Sesion;
import com.mycompany.sistemagestioncine.models.Usuario;
import javax.swing.JButton;

/**
 *
 * @author axele
 */
public class DashboardController {
    private JButton btnSalas;
    private JButton btnPeliculas;
    private JButton btnCorteCaja;
    private JButton btnTaquilla;

    // El constructor recibe los componentes de la vista que necesitan ser controlados
    public DashboardController(JButton btnSalas, JButton btnPeliculas, JButton btnCorteCaja, JButton btnTaquilla) {
        this.btnSalas = btnSalas;
        this.btnPeliculas = btnPeliculas;
        this.btnCorteCaja = btnCorteCaja;
        this.btnTaquilla = btnTaquilla;
    }

    /**
     * Lee la sesión global y oculta o muestra botones dependiendo del rol del usuario.
     */
    public void aplicarPermisosPorRol() {
        Usuario usuario = Sesion.getUsuarioActual();

        if (usuario != null) {
            String rol = usuario.getRol();

            // Lógica descrita en los Criterios de Aceptación
            if ("Cajero".equalsIgnoreCase(rol)) {
                btnSalas.setVisible(false);
                btnPeliculas.setVisible(false);
                btnCorteCaja.setVisible(false);
                
                // El cajero solo debe ver la Taquilla
                btnTaquilla.setVisible(true);
                
            } else if ("Administrador".equalsIgnoreCase(rol) || "Admin".equalsIgnoreCase(rol)) {
                // El administrador ve todo el menú
                btnSalas.setVisible(true);
                btnPeliculas.setVisible(true);
                btnCorteCaja.setVisible(true);
                btnTaquilla.setVisible(true);
            }
        } else {
            System.err.println("Advertencia: Se intentó aplicar permisos pero no hay ninguna sesión activa.");
        }
    }
}
