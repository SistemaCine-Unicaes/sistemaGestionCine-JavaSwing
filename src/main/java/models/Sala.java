/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author axele
 */
public class Sala {
    private int idSala;
    private int capacidadTotal;
    private int asientosEspeciales;
    private int tiempoDeLimpieza;
    private int asientosPorFila;
    private String estado;
    private String nombre;
    private String codigoPlano;
    private String motivoInactividad;
    public String getNombre() { return nombre == null ? "Sala " + idSala : nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoPlano() { return codigoPlano; }
    public void setCodigoPlano(String codigo) { codigoPlano = codigo; }
    public String getMotivoInactividad() { return motivoInactividad; }
    public void setMotivoInactividad(String motivo) { motivoInactividad = motivo; }

    public Sala() {
    }

    public Sala(int capacidadTotal, int asientosEspeciales, int tiempoDeLimpieza, int asientosPorFila, String estado) {
        this.capacidadTotal = capacidadTotal;
        this.asientosEspeciales = asientosEspeciales;
        this.tiempoDeLimpieza = tiempoDeLimpieza;
        this.asientosPorFila = asientosPorFila;
        this.estado = estado;
    }

    public Sala(int idSala, int capacidadTotal, int asientosEspeciales, int tiempoDeLimpieza, int asientosPorFila, String estado) {
        this.idSala = idSala;
        this.capacidadTotal = capacidadTotal;
        this.asientosEspeciales = asientosEspeciales;
        this.tiempoDeLimpieza = tiempoDeLimpieza;
        this.asientosPorFila = asientosPorFila;
        this.estado = estado;
    }

    public int getIdSala() {
        return idSala;
    }

    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    public int getAsientosEspeciales() {
        return asientosEspeciales;
    }

    public int getTiempoDeLimpieza() {
        return tiempoDeLimpieza;
    }

    public int getAsientosPorFila() {
        return asientosPorFila;
    }

    public String getEstado() {
        return estado;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public void setCapacidadTotal(int capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }

    public void setAsientosEspeciales(int asientosEspeciales) {
        this.asientosEspeciales = asientosEspeciales;
    }

    public void setTiempoDeLimpieza(int tiempoDeLimpieza) {
        this.tiempoDeLimpieza = tiempoDeLimpieza;
    }

    public void setAsientosPorFila(int asientosPorFila) {
        this.asientosPorFila = asientosPorFila;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Sala{" + "id=" + idSala + ", capacidad=" + capacidadTotal + 
               ", asientosEspeciales=" + asientosEspeciales + 
               ", limpieza=" + tiempoDeLimpieza + "min, asientosFila=" + asientosPorFila + 
               ", estado='" + estado + "'}";
    }
}
