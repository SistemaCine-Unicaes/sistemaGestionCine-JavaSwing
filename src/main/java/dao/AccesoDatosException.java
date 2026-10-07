package dao;

import java.sql.SQLException;

public class AccesoDatosException extends RuntimeException {
    public AccesoDatosException(SQLException causa) {
        super(mensaje(causa), causa);
    }

    private static String mensaje(SQLException e) {
        return switch (e.getSQLState() == null ? "" : e.getSQLState()) {
            case "23505" -> "El registro ya existe o uno de los asientos acaba de venderse. Actualiza e intenta nuevamente.";
            case "23503" -> "No se puede completar la operación porque existen registros relacionados.";
            case "42703" -> "La base de datos necesita actualizarse. Ejecuta database/002_salas_predefinidas.sql y vuelve a intentar.";
            default -> "No se pudo completar la operación en la base de datos. Comprueba la conexión y vuelve a intentar.";
        };
    }
}
