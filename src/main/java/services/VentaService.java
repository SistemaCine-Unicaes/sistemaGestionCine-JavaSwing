package services;

import config.Sesion;
import dao.ConfiguracionDAO;
import dao.FuncionDAO;
import dao.SalaDAO;
import dao.TicketDAO;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import models.Funcion;
import models.Sala;
import models.Ticket;
import models.Usuario;

public class VentaService {
    private final java.util.function.Supplier<Connection> conexiones;
    public VentaService() { this(config.Conexion::getConexion); }
    public VentaService(java.util.function.Supplier<Connection> conexiones) { this.conexiones = conexiones; }

    // Método principal con desglose de tarifas
    public List<Ticket> vender(int idFuncion, List<Integer> asientos, int cantAdulto, int cantNino, int cantMayor, ConfiguracionDAO.Precios preciosMostrados) {
        int cantidadTotal = cantAdulto + cantNino + cantMayor;
        Usuario usuario = Sesion.exigirVenta();
        validarSeleccion(asientos, cantidadTotal);
        List<Integer> ordenados = asientos.stream().sorted().toList();

        return Transacciones.ejecutar(conexiones, c -> {
            Funcion funcion = new FuncionDAO(c).bloquear(idFuncion);
            if (funcion == null || !"Programada".equals(funcion.getEstado())) throw new IllegalStateException("La función ya no está disponible.");
            Sala sala = new SalaDAO(c).bloquear(funcion.getIdSala());
            if (sala == null || !"ACTIVA".equals(sala.getEstado())) throw new IllegalStateException("La sala no está activa.");
            try (PreparedStatement ps = c.prepareStatement("SELECT estado FROM Pelicula WHERE id_pelicula=? FOR SHARE")) {
                ps.setInt(1, funcion.getIdPelicula());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"CARTELERA".equals(rs.getString(1))) throw new IllegalStateException("La película ya no está en cartelera.");
                }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT estado,rol FROM Usuario WHERE id_usuario=? FOR SHARE")) {
                ps.setInt(1, usuario.getIdUsuario());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"Activo".equals(rs.getString(1))
                            || !Set.of("cajero", "admin", "administrador").contains(rs.getString(2).toLowerCase(Locale.ROOT))) {
                        throw new IllegalStateException("El usuario ya no está autorizado para vender.");
                    }
                }
            }
            try (Statement ps = c.createStatement(); ResultSet rs = ps.executeQuery("SELECT LOCALTIMESTAMP")) {
                rs.next();
                if (!funcion.getFechaProyeccion().toLocalDate().atTime(funcion.getHoraInicio().toLocalTime())
                        .isAfter(rs.getTimestamp(1).toLocalDateTime())) throw new IllegalStateException("La función ya comenzó. Selecciona otra función.");
            }

            // Validar que las tarifas de la BD sigan coincidiendo
            ConfiguracionDAO.Precios preciosBD = new ConfiguracionDAO(c).obtenerPrecios(true);
            if (preciosBD == null || !preciosBD.estanCompletos() || preciosMostrados == null || !preciosMostrados.estanCompletos()
                    || preciosBD.adulto().compareTo(preciosMostrados.adulto()) != 0
                    || preciosBD.nino().compareTo(preciosMostrados.nino()) != 0
                    || preciosBD.adultoMayor().compareTo(preciosMostrados.adultoMayor()) != 0) {
                throw new IllegalStateException("Los precios cambiaron o no están configurados. Vuelve a abrir Taquilla para actualizarlos.");
            }

            // Crear lista de precios asignados a cada asiento
            List<BigDecimal> preciosAsignados = new ArrayList<>();
            for (int i = 0; i < cantAdulto; i++) preciosAsignados.add(preciosBD.adulto());
            for (int i = 0; i < cantNino; i++) preciosAsignados.add(preciosBD.nino());
            for (int i = 0; i < cantMayor; i++) preciosAsignados.add(preciosBD.adultoMayor());

            Set<Integer> vendidos = new HashSet<>();
            TicketDAO tickets = new TicketDAO(c);
            for (Ticket ticket : tickets.obtenerTicketsPorFuncion(idFuncion)) vendidos.add(ticket.getIdAsiento());
            List<Ticket> venta = new ArrayList<>();

            try (PreparedStatement ps = c.prepareStatement("SELECT id_sala,estado FROM Asiento WHERE id_asiento=? FOR UPDATE")) {
                for (int i = 0; i < ordenados.size(); i++) {
                    int id = ordenados.get(i);
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) != funcion.getIdSala() || !"Disponible".equals(rs.getString(2))) {
                            throw new IllegalStateException("Uno de los asientos no está disponible en esta sala. Vuelve a cargar el mapa.");
                        }
                    }
                    if (vendidos.contains(id)) throw new IllegalStateException("Uno de los asientos acaba de venderse. Cierra el mapa y vuelve a seleccionar.");
                    
                    BigDecimal montoTicket = preciosAsignados.get(i);
                    Ticket ticket = new Ticket(0, idFuncion, id, usuario.getIdUsuario(), montoTicket, null);
                    tickets.venderTicket(ticket);
                    venta.add(ticket);
                }
            }
            return List.copyOf(venta);
        });
    }

    // Sobrecarga de compatibilidad anterior
    public List<Ticket> vender(int idFuncion, List<Integer> asientos, int cantidad, BigDecimal precioMostrado) {
        return vender(idFuncion, asientos, cantidad, 0, 0, new ConfiguracionDAO.Precios(precioMostrado, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    public static void validarSeleccion(List<Integer> asientos, int cantidad) {
        if (asientos == null || cantidad <= 0 || asientos.size() != cantidad
                || asientos.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(asientos).size() != cantidad) {
            throw new IllegalArgumentException("Selecciona exactamente " + cantidad + " asientos distintos.");
        }
    }
}