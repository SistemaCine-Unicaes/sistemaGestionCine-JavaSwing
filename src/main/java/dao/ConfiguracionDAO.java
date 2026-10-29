package dao;

import java.math.BigDecimal;
import java.sql.*;

public class ConfiguracionDAO {
    
    // Contenedor inmutable para transportar las tres tarifas
    public record Precios(BigDecimal adulto, BigDecimal nino, BigDecimal adultoMayor) {
        public boolean estanCompletos() {
            return adulto != null && nino != null && adultoMayor != null;
        }
    }

    private final Connection conexion;
    public ConfiguracionDAO(Connection conexion) { this.conexion = conexion; }

    // --- MÉTODOS PARA LAS 3 TARIFAS ---
    public Precios obtenerPrecios() { return obtenerPrecios(false); }

    public Precios obtenerPrecios(boolean bloquear) {
        String sql = "SELECT precio_adulto, precio_nino, precio_adulto_mayor FROM configuracion_cine WHERE id=1" 
                   + (bloquear ? " FOR SHARE" : "");
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Precios(
                    rs.getBigDecimal("precio_adulto"),
                    rs.getBigDecimal("precio_nino"),
                    rs.getBigDecimal("precio_adulto_mayor")
                );
            }
            return null;
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    public void guardarPrecios(BigDecimal adulto, BigDecimal nino, BigDecimal adultoMayor) {
        config.Sesion.exigirAdministrador();
        
        adulto = config.Configuracion.precio(adulto.toPlainString());
        nino = config.Configuracion.precio(nino.toPlainString());
        adultoMayor = config.Configuracion.precio(adultoMayor.toPlainString());

        String sql = "INSERT INTO configuracion_cine(id, precio_boleto, precio_adulto, precio_nino, precio_adulto_mayor) "
                   + "VALUES (1, ?, ?, ?, ?) "
                   + "ON CONFLICT (id) DO UPDATE SET "
                   + "precio_boleto = EXCLUDED.precio_boleto, "
                   + "precio_adulto = EXCLUDED.precio_adulto, "
                   + "precio_nino = EXCLUDED.precio_nino, "
                   + "precio_adulto_mayor = EXCLUDED.precio_adulto_mayor";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBigDecimal(1, adulto);       // precio_boleto (respaldo)
            ps.setBigDecimal(2, adulto);       // precio_adulto
            ps.setBigDecimal(3, nino);         // precio_nino
            ps.setBigDecimal(4, adultoMayor);  // precio_adulto_mayor
            ps.executeUpdate();
        } catch (SQLException e) { throw new AccesoDatosException(e); }
    }

    // --- MÉTODOS DE COMPATIBILIDAD (EVITAN ERRORES MIENTRAS SE MIGRA LA VISTA) ---
    public BigDecimal obtenerPrecio() { return obtenerPrecio(false); }

    public BigDecimal obtenerPrecio(boolean bloquear) {
        Precios p = obtenerPrecios(bloquear);
        return p != null ? p.adulto() : null;
    }

    public void guardarPrecio(BigDecimal precio) {
        guardarPrecios(precio, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}