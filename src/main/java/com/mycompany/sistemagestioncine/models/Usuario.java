/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemagestioncine.models;

import java.sql.Date;

/**
 *
 * @author axele
 */
public class Usuario {
    private int idUsuario;
    private String rol;
    private String nombre;
    private String username;
    private String passwordHash;
    private String dui;
    private String email;
    private String telefono;
    private Date fechaNacimiento;
    private String genero;
    private String direccion;
    private Date fechaContratacion;
    private String imagenUrl;
    private String estado;

    public Usuario() {
    }

    public Usuario(int idUsuario, String rol, String nombre, String username, String passwordHash, String dui, String email, String telefono, Date fechaNacimiento, String genero, String direccion, Date fechaContratacion, String imagenUrl, String estado) {
        this.idUsuario = idUsuario;
        this.rol = rol;
        this.nombre = nombre;
        this.username = username;
        this.passwordHash = passwordHash;
        this.dui = dui;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.direccion = direccion;
        this.fechaContratacion = fechaContratacion;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    public Usuario(String rol, String nombre, String username, String passwordHash, String dui, String email, String telefono, Date fechaNacimiento, String genero, String direccion, Date fechaContratacion, String imagenUrl, String estado) {
        this.rol = rol;
        this.nombre = nombre;
        this.username = username;
        this.passwordHash = passwordHash;
        this.dui = dui;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.direccion = direccion;
        this.fechaContratacion = fechaContratacion;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getRol() {
        return rol;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDui() {
        return dui;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public String getDireccion() {
        return direccion;
    }

    public Date getFechaContratacion() {
        return fechaContratacion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setFechaContratacion(Date fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Usuario{" + "id=" + idUsuario + ", nombre='" + nombre + "', rol='" + rol + "', estado='" + estado + "'}";
    }
}