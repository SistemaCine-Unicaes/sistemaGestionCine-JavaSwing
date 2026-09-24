package dao;

import models.Funcion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionDAO {
    private final Connection conexion;
    public FuncionDAO(Connection conexion) { this.conexion = conexion; }

    public boolean programarFuncion(Funcion f) {
        String sql = "INSERT INTO Funcion(id_pelicula,id_sala,fecha_proyeccion,hora_inicio,hora_fin,estado) VALUES (?,?,?,?,?,?::estado_funcion) RETURNING id_funcion";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, f.getIdPelicula()); ps.setInt(2, f.getIdSala()); ps.setDate(3, f.getFechaProyeccion());
            ps.setTime(4, f.getHoraInicio()); ps.setTime(5, f.getHoraFin()); ps.setString(6, f.getEstado());
            try (ResultSet rs = ps.executeQuery()) { rs.next(); f.setIdFuncion(rs.getInt(1)); }
            return true;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public List<Funcion> listarFuncionesDelDia(Date fecha) {
        return listar("SELECT * FROM Funcion WHERE fecha_proyeccion=? ORDER BY hora_inicio,id_funcion", fecha);
    }

    public List<Funcion> listarDisponibles() {
        return listar("SELECT f.* FROM Funcion f JOIN Sala s USING(id_sala) JOIN Pelicula p USING(id_pelicula) "
                + "WHERE f.estado='Programada' AND s.estado='ACTIVA' AND p.estado='CARTELERA' "
                + "AND f.fecha_proyeccion + f.hora_inicio > LOCALTIMESTAMP ORDER BY f.fecha_proyeccion,f.hora_inicio,f.id_funcion", null);
    }

    private List<Funcion> listar(String sql, Date fecha) {
        List<Funcion> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (fecha != null) ps.setDate(1, fecha);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
            return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public Funcion bloquear(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Funcion WHERE id_funcion=? FOR UPDATE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    static Funcion mapear(ResultSet rs) throws SQLException {
        return new Funcion(rs.getInt("id_funcion"), rs.getInt("id_pelicula"), rs.getInt("id_sala"),
                rs.getDate("fecha_proyeccion"), rs.getTime("hora_inicio"), rs.getTime("hora_fin"), rs.getString("estado"));
    }
}