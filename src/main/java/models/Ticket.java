package models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Ticket {
    private int idTicket;
    private int idFuncion;
    private int idAsiento;
    private int idUsuario;
    private BigDecimal monto;
    private Timestamp fechaHoraCompra;

    public Ticket() {}

    public Ticket(int idTicket, int idFuncion, int idAsiento, int idUsuario, BigDecimal monto, Timestamp fechaHoraCompra) {
        this.idTicket = idTicket;
        this.idFuncion = idFuncion;
        this.idAsiento = idAsiento;
        this.idUsuario = idUsuario;
        this.monto = monto;
        this.fechaHoraCompra = fechaHoraCompra;
    }

    public int getIdTicket() { return idTicket; }
    public void setIdTicket(int idTicket) { this.idTicket = idTicket; }

    public int getIdFuncion() { return idFuncion; }
    public void setIdFuncion(int idFuncion) { this.idFuncion = idFuncion; }

    public int getIdAsiento() { return idAsiento; }
    public void setIdAsiento(int idAsiento) { this.idAsiento = idAsiento; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Timestamp getFechaHoraCompra() { return fechaHoraCompra; }
    public void setFechaHoraCompra(Timestamp fechaHoraCompra) { this.fechaHoraCompra = fechaHoraCompra; }
}