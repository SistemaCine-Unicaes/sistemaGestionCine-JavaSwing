package config;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Configuracion {
    private static final java.util.Properties locales = cargarLocales();
    private Configuracion() {}

    private static java.util.Properties cargarLocales() {
        java.util.Properties propiedades = new java.util.Properties();
        java.nio.file.Path archivo = java.nio.file.Path.of("cine.local.properties");
        if (java.nio.file.Files.isRegularFile(archivo)) {
            try (var reader = java.nio.file.Files.newBufferedReader(archivo, java.nio.charset.StandardCharsets.UTF_8)) {
                propiedades.load(reader);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("No se pudo leer cine.local.properties.", e);
            }
        }
        return propiedades;
    }

    public static String valor(String nombre, String defecto) {
        String valor = System.getProperty(nombre);
        if (valor == null || valor.isBlank()) valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) valor = locales.getProperty(nombre);
        return valor == null || valor.isBlank() ? defecto : valor;
    }

    public static BigDecimal precio(String texto) {
        try {
            BigDecimal precio = new BigDecimal(texto.trim()).setScale(2, RoundingMode.UNNECESSARY);
            if (precio.signum() <= 0 || precio.compareTo(new BigDecimal("999999.99")) > 0) {
                throw new IllegalArgumentException();
            }
            return precio;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Ingresa un precio positivo con un máximo de dos decimales.");
        }
    }
}
