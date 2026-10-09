package dao;

import models.Pelicula;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeliculaDAO {
    private final Connection conexion;
    public PeliculaDAO(Connection conexion) { this.conexion = conexion; }

    public boolean insertarPelicula(Pelicula p) {
        String sql = "INSERT INTO Pelicula (nombre,sinopsis,duracion,genero,director,fecha_estreno,tipo_estreno,imagen_url,estado) "
                + "VALUES (?,?,?,?,?,?,?,?,?::estado_pelicula) RETURNING id_pelicula";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, p.getNombre()); ps.setString(2, p.getSinopsis());
            ps.setInt(3, p.getDuracion()); ps.setString(4, p.getGenero());
            ps.setString(5, p.getDirector()); ps.setDate(6, p.getFechaEstreno());
            ps.setString(7, p.getTipoEstreno()); ps.setString(8, p.getImagenUrl()); ps.setString(9, p.getEstado());
            try (ResultSet rs = ps.executeQuery()) { rs.next(); p.setIdPelicula(rs.getInt(1)); }
            return true;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public List<Pelicula> obtenerTodas() {
        List<Pelicula> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Pelicula ORDER BY nombre,id_pelicula");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    static Pelicula mapear(ResultSet rs) throws SQLException {
        return new Pelicula(rs.getInt("id_pelicula"), rs.getString("nombre"), rs.getString("sinopsis"),
                rs.getInt("duracion"), rs.getString("genero"), rs.getString("director"),
                rs.getDate("fecha_estreno"), rs.getString("tipo_estreno"), rs.getString("imagen_url"), rs.getString("estado"));
    }

    // Conserva los campos de estreno e imagen que no expone el formulario.
    public boolean actualizarPelicula(Pelicula p) {
        String sql = "UPDATE Pelicula SET nombre=?,sinopsis=?,duracion=?,genero=?,director=?,estado=?::estado_pelicula WHERE id_pelicula=?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, p.getNombre()); ps.setString(2, p.getSinopsis()); ps.setInt(3, p.getDuracion());
            ps.setString(4, p.getGenero()); ps.setString(5, p.getDirector());
            ps.setString(6, p.getEstado()); ps.setInt(7, p.getIdPelicula());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public boolean eliminarPelicula(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM Pelicula WHERE id_pelicula=?")) {
            ps.setInt(1, id); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public boolean actualizarImagenPoster(int id, String url) {
        try (PreparedStatement ps = conexion.prepareStatement("UPDATE Pelicula SET imagen_url=? WHERE id_pelicula=?")) {
            ps.setString(1, url); ps.setInt(2, id); return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}