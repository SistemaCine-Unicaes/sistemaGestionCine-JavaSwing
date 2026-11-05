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
            case "23502" -> "Falta un dato obligatorio para la base de datos. Completa el formulario y vuelve a intentar.";
            case "22001" -> "Uno de los textos supera la longitud permitida por la base de datos.";
            case "22P02" -> "Uno de los valores elegidos no es válido para la base de datos. Actualiza el listado e intenta nuevamente.";
            case "42703" -> "La base de datos necesita actualizarse. Ejecuta database/002_salas_predefinidas.sql y vuelve a intentar.";
            default -> "No se pudo completar la operación en la base de datos. Comprueba la conexión y vuelve a intentar.";
        };
    }
}
