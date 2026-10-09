package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Sala;

public class SalaDAO {
    private final Connection conexion;
    public SalaDAO(Connection conexion) { this.conexion = conexion; }

    public boolean registrarSala(Sala sala) {
        String sql = "INSERT INTO Sala(capacidad_total,asientos_especiales,tiempo_de_limpieza,asientos_por_fila,estado) "
                + "VALUES (?,?,?,?,?::estado_sala) RETURNING id_sala";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            llenar(ps, sala);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); sala.setIdSala(rs.getInt(1)); }
            return true;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    private void llenar(PreparedStatement ps, Sala sala) throws SQLException {
        ps.setInt(1, sala.getCapacidadTotal()); ps.setInt(2, sala.getAsientosEspeciales());
        ps.setInt(3, sala.getTiempoDeLimpieza()); ps.setInt(4, sala.getAsientosPorFila()); ps.setString(5, sala.getEstado());
    }

    public List<Sala> listarSalas() {
        List<Sala> salas = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Sala ORDER BY id_sala");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) salas.add(mapear(rs));
            return salas;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    static Sala mapear(ResultSet rs) throws SQLException {
        return new Sala(rs.getInt("id_sala"), rs.getInt("capacidad_total"), rs.getInt("asientos_especiales"),
                rs.getInt("tiempo_de_limpieza"), rs.getInt("asientos_por_fila"), rs.getString("estado"));
    }

    public Sala obtenerSalaPorId(int id) { return obtener(id, false); }
    public Sala bloquear(int id) { return obtener(id, true); }

    private Sala obtener(int id, boolean bloquear) {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Sala WHERE id_sala=?" + (bloquear ? " FOR UPDATE" : ""))) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public boolean actualizarSala(Sala sala) {
        String sql = "UPDATE Sala SET capacidad_total=?,asientos_especiales=?,tiempo_de_limpieza=?,asientos_por_fila=?,estado=?::estado_sala WHERE id_sala=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            llenar(ps, sala); ps.setInt(6, sala.getIdSala()); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public boolean eliminarSala(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM Sala WHERE id_sala=?")) {
            ps.setInt(1, id); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}