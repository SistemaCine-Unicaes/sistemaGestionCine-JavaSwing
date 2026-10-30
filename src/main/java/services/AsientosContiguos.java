package services;

import java.util.*;
import models.Asiento;

/** Impide crear huecos de una butaca entre localidades ocupadas del mismo bloque. */
public final class AsientosContiguos {
    private AsientosContiguos() {}
    private record Fila(int sala, String nombre, int posicion) {}

    public static List<Asiento> huecosNuevos(List<Asiento> asientos, Set<Integer> vendidos,
            Collection<Integer> seleccionados) {
        Set<Integer> ocupados = new HashSet<>(vendidos);
        ocupados.addAll(seleccionados);
        Map<Fila, List<Asiento>> filas = new LinkedHashMap<>();
        for (Asiento asiento : asientos) {
            Fila fila = new Fila(asiento.getIdSala(), asiento.getFila(), asiento.getFilaPlano());
            filas.computeIfAbsent(fila, clave -> new ArrayList<>()).add(asiento);
        }
        List<Asiento> huecos = new ArrayList<>();
        for (List<Asiento> fila : filas.values()) {
            fila.sort(Comparator.comparingInt(Asiento::getColumnaPlano));
            for (int i = 1; i < fila.size() - 1; i++) {
                Asiento anterior = fila.get(i - 1), libre = fila.get(i), siguiente = fila.get(i + 1);
                if (!"Disponible".equals(libre.getEstado()) || ocupados.contains(libre.getIdAsiento())) continue;
                // Un pasillo o una posición inexistente interrumpe la continuidad física.
                if (anterior.getColumnaPlano() + 1 != libre.getColumnaPlano()
                        || libre.getColumnaPlano() + 1 != siguiente.getColumnaPlano()) continue;
                if (ocupados.contains(anterior.getIdAsiento()) && ocupados.contains(siguiente.getIdAsiento())
                        && !(vendidos.contains(anterior.getIdAsiento()) && vendidos.contains(siguiente.getIdAsiento()))) {
                    huecos.add(libre);
                }
            }
        }
        return List.copyOf(huecos);
    }

    public static String mensaje(List<Asiento> huecos) {
        String ubicaciones = huecos.stream().limit(5).map(a -> a.getFila() + "-" + a.getNumero())
                .collect(java.util.stream.Collectors.joining(", "));
        return "La selección deja " + (huecos.size() == 1 ? "un asiento libre aislado: " : "asientos libres aislados: ")
                + ubicaciones + (huecos.size() > 5 ? "…" : ".")
                + " Ajusta la selección para no dejar un asiento solo entre asientos vendidos o seleccionados.";
    }

    public static void validar(List<Asiento> asientos, Set<Integer> vendidos, Collection<Integer> seleccionados) {
        List<Asiento> huecos = huecosNuevos(asientos, vendidos, seleccionados);
        if (!huecos.isEmpty()) throw new IllegalStateException(mensaje(huecos));
    }
}
