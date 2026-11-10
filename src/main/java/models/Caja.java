package models;

import java.sql.Timestamp;

/**
 * Modelo para las cajas de atención / taquillas preestablecidas del cine.
 */
public class Caja {
    private int idCaja;
    private int numeroCaja;
    private String nombre;
    private String estado;
    private Integer idUsuarioActual;
    private Timestamp fechaApertura;

    public Caja() {}

    public Caja(int idCaja, int numeroCaja, String nombre, String estado, Integer idUsuarioActual, Timestamp fechaApertura) {
        this.idCaja = idCaja;
        this.numeroCaja = numeroCaja;
        this.nombre = nombre;
        this.estado = estado;
        this.idUsuarioActual = idUsuarioActual;
        this.fechaApertura = fechaApertura;
    }

    public int getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public int getNumeroCaja() {
        return numeroCaja;
    }

    public void setNumeroCaja(int numeroCaja) {
        this.numeroCaja = numeroCaja;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdUsuarioActual() {
        return idUsuarioActual;
    }

    public void setIdUsuarioActual(Integer idUsuarioActual) {
        this.idUsuarioActual = idUsuarioActual;
    }

    public Timestamp getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Timestamp fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public boolean isDisponible() {
        return "DISPONIBLE".equalsIgnoreCase(estado);
    }

    public boolean isOcupada() {
        return "OCUPADA".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return nombre != null && !nombre.isBlank() ? nombre : "Caja " + numeroCaja;
    }
}
