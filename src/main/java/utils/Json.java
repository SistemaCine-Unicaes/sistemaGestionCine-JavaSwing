package utils;

/** Escape de valores de texto para las solicitudes de autenticación. */
public final class Json {
    private Json() {}
    public static String texto(String valor) {
        StringBuilder resultado = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> resultado.append("\\\"");
                case '\\' -> resultado.append("\\\\");
                case '\n' -> resultado.append("\\n");
                case '\r' -> resultado.append("\\r");
                case '\t' -> resultado.append("\\t");
                default -> {
                    if (c < 32) resultado.append(String.format("\\u%04x", (int) c));
                    else resultado.append(c);
                }
            }
        }
        return resultado.append('"').toString();
    }
}
