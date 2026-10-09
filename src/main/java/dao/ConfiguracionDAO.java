package dao;

import java.math.BigDecimal;
import java.sql.*;

public class ConfiguracionDAO {
    private final Connection conexion;
    public ConfiguracionDAO(Connection conexion) { this.conexion = conexion; }

    public BigDecimal obtenerPrecio() { return obtenerPrecio(false); }

    public BigDecimal obtenerPrecio(boolean bloquear) {
        try (PreparedStatement ps = conexion.prepareStatement("SELECT precio_boleto FROM configuracion_cine WHERE id=1" + (bloquear ? " FOR SHARE" : ""));
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : null;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public void guardarPrecio(BigDecimal precio) {
        config.Sesion.exigirAdministrador();
        precio = config.Configuracion.precio(precio.toPlainString());
        String sql = "INSERT INTO configuracion_cine(id,precio_boleto) VALUES (1,?) "
                + "ON CONFLICT (id) DO UPDATE SET precio_boleto=EXCLUDED.precio_boleto";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBigDecimal(1, precio); ps.executeUpdate();
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }
}
