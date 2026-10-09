package models;

import java.sql.Date;
import java.sql.Time;

public class Funcion {
    private int idFuncion;
    private int idPelicula;
    private int idSala;
    private Date fechaProyeccion;
    private Time horaInicio;
    private Time horaFin;
    private String estado;

    public Funcion() {}

    public Funcion(int idFuncion, int idPelicula, int idSala, Date fechaProyeccion, Time horaInicio, Time horaFin, String estado) {
        this.idFuncion = idFuncion;
        this.idPelicula = idPelicula;
        this.idSala = idSala;
        this.fechaProyeccion = fechaProyeccion;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    public int getIdFuncion() { return idFuncion; }
    public void setIdFuncion(int idFuncion) { this.idFuncion = idFuncion; }

    public int getIdPelicula() { return idPelicula; }
    public void setIdPelicula(int idPelicula) { this.idPelicula = idPelicula; }

    public int getIdSala() { return idSala; }
    public void setIdSala(int idSala) { this.idSala = idSala; }

    public Date getFechaProyeccion() { return fechaProyeccion; }
    public void setFechaProyeccion(Date fechaProyeccion) { this.fechaProyeccion = fechaProyeccion; }

    public Time getHoraInicio() { return horaInicio; }
    public void setHoraInicio(Time horaInicio) { this.horaInicio = horaInicio; }

    public Time getHoraFin() { return horaFin; }
    public void setHoraFin(Time horaFin) { this.horaFin = horaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}