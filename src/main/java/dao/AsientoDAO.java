package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Asiento;
import models.Sala;

public class AsientoDAO {
    private final Connection conexion;
    public AsientoDAO(Connection conexion) { this.conexion = conexion; }

    public List<Asiento> obtenerAsientosPorSala(int idSala) {
        List<Asiento> lista = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Asiento WHERE id_sala=? ORDER BY length(fila),fila,numero")) {
            ps.setInt(1, idSala);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new Asiento(rs.getInt("id_asiento"), rs.getInt("id_sala"),
                        rs.getString("tipo_de_asiento"), rs.getString("fila"), rs.getInt("numero"), rs.getString("estado")));
            }
            return lista;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public void generarAsientos(Sala sala) {
        String sql = "INSERT INTO Asiento(id_sala,tipo_de_asiento,fila,numero,estado) VALUES (?,?,?,?,'Disponible')";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (int i = 0; i < sala.getCapacidadTotal(); i++) {
                ps.setInt(1, sala.getIdSala());
                ps.setString(2, i < sala.getAsientosEspeciales() ? "Especial" : "Normal");
                ps.setString(3, nombreFila(i / sala.getAsientosPorFila()));
                ps.setInt(4, i % sala.getAsientosPorFila() + 1); ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public static String nombreFila(int indice) {
        if (indice < 0) throw new IllegalArgumentException("Fila inválida.");
        StringBuilder nombre = new StringBuilder();
        for (int n = indice + 1; n > 0; n = (n - 1) / 26) nombre.append((char) ('A' + (n - 1) % 26));
        return nombre.reverse().toString();
    }

    public void eliminarPorSala(int idSala) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM Asiento WHERE id_sala=?")) {
            ps.setInt(1, idSala); ps.executeUpdate();
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}