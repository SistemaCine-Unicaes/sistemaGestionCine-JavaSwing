package com.mycompany.sistemagestioncine.models;

import java.sql.Date;

public class Pelicula {
    private int idPelicula;
    private String nombre;
    private String sinopsis;
    private int duracion;
    private String genero;
    private String director;
    private Date fechaEstreno;
    private String tipoEstreno;
    private String imagenUrl;
    private String estado;

    public Pelicula() {}

    public Pelicula(int idPelicula, String nombre, String sinopsis, int duracion, String genero, 
                    String director, Date fechaEstreno, String tipoEstreno, String imagenUrl, String estado) {
        this.idPelicula = idPelicula;
        this.nombre = nombre;
        this.sinopsis = sinopsis;
        this.duracion = duracion;
        this.genero = genero;
        this.director = director;
        this.fechaEstreno = fechaEstreno;
        this.tipoEstreno = tipoEstreno;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    // Getters
    public int getIdPelicula() { return idPelicula; }
    public String getNombre() { return nombre; }
    public String getSinopsis() { return sinopsis; }
    public int getDuracion() { return duracion; }
    public String getGenero() { return genero; }
    public String getDirector() { return director; }
    public Date getFechaEstreno() { return fechaEstreno; }
    public String getTipoEstreno() { return tipoEstreno; }
    public String getImagenUrl() { return imagenUrl; }
    public String getEstado() { return estado; }

    // Setters
    public void setIdPelicula(int idPelicula) { this.idPelicula = idPelicula; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setDirector(String director) { this.director = director; }
    public void setFechaEstreno(Date fechaEstreno) { this.fechaEstreno = fechaEstreno; }
    public void setTipoEstreno(String tipoEstreno) { this.tipoEstreno = tipoEstreno; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public void setEstado(String estado) { this.estado = estado; }
}