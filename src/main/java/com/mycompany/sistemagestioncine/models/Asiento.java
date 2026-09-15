/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemagestioncine.models;

/**
 *
 * @author axele
 */
public class Asiento {
    private int idAsiento;
    private int idSala;
    private String tipoDeAsiento;
    private String fila;
    private int numero;
    private String estado;

    public Asiento() {
    }

    public Asiento(int idSala, String tipoDeAsiento, String fila, int numero, String estado) {
        this.idSala = idSala;
        this.tipoDeAsiento = tipoDeAsiento;
        this.fila = fila;
        this.numero = numero;
        this.estado = estado;
    }

    public Asiento(int idAsiento, int idSala, String tipoDeAsiento, String fila, int numero, String estado) {
        this.idAsiento = idAsiento;
        this.idSala = idSala;
        this.tipoDeAsiento = tipoDeAsiento;
        this.fila = fila;
        this.numero = numero;
        this.estado = estado;
    }

    public int getIdAsiento() {
        return idAsiento;
    }

    public int getIdSala() {
        return idSala;
    }

    public String getTipoDeAsiento() {
        return tipoDeAsiento;
    }

    public String getFila() {
        return fila;
    }

    public int getNumero() {
        return numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setIdAsiento(int idAsiento) {
        this.idAsiento = idAsiento;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public void setTipoDeAsiento(String tipoDeAsiento) {
        this.tipoDeAsiento = tipoDeAsiento;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Asiento{" + "id=" + idAsiento + ", sala=" + idSala + 
               ", ubicacion='" + fila + "-" + numero + "', tipo='" + tipoDeAsiento + 
               "', estado='" + estado + "'}";
    }
}