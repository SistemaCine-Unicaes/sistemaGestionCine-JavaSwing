import java.util.*;
import models.Asiento;
import org.junit.jupiter.api.Test;
import services.AsientosContiguos;
import static org.junit.jupiter.api.Assertions.*;

class AsientosContiguosTest {
    private List<Asiento> fila(int cantidad) {
        List<Asiento> asientos = new ArrayList<>();
        for (int n=1;n<=cantidad;n++) asientos.add(new Asiento(n*10,1,"Normal","A",n,"Disponible"));
        return asientos;
    }
    @Test void rechazaAsientoEntreVendidoYSeleccionadosEnAmbosSentidos() {
        List<Asiento> fila=fila(4);
        assertEquals(List.of(fila.get(1)),AsientosContiguos.huecosNuevos(fila,Set.of(10),List.of(30,40)));
        assertEquals(List.of(fila.get(2)),AsientosContiguos.huecosNuevos(fila,Set.of(40),List.of(10,20)));
        assertTrue(assertThrows(IllegalStateException.class,()->AsientosContiguos.validar(fila,Set.of(10),List.of(30,40)))
                .getMessage().contains("A-2"));
    }
    @Test void detectaHuecoEntreSeleccionadosYPermiteRellenarloSinImportarOrden() {
        List<Asiento> fila=fila(5); Collections.reverse(fila);
        assertEquals(1,AsientosContiguos.huecosNuevos(fila,Set.of(),List.of(10,30)).size());
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(),List.of(30,10,20)));
    }
    @Test void noBloqueaExtremosNiDosAsientosLibresJuntos() {
        assertDoesNotThrow(()->AsientosContiguos.validar(fila(4),Set.of(10),List.of(20,30)));
        assertDoesNotThrow(()->AsientosContiguos.validar(fila(5),Set.of(10),List.of(40,50)));
        assertDoesNotThrow(()->AsientosContiguos.validar(fila(1),Set.of(),List.of(10)));
    }
    @Test void pasillosYPosicionesAusentesInterrumpenBloques() {
        List<Asiento> fila=fila(4); fila.get(2).setColumnaPlano(3); fila.get(3).setColumnaPlano(4);
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(10),List.of(30)));
        List<Asiento> incompleta=fila(5); incompleta.remove(1);
        assertTrue(AsientosContiguos.huecosNuevos(incompleta,Set.of(10),List.of(40)).isEmpty());
    }
    @Test void averiadoNoEsHuecoNiEquivaleAAsientoVendido() {
        List<Asiento> fila=fila(4); fila.get(1).setEstado("Averiado");
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(10),List.of(30)));
        fila.get(1).setEstado("Disponible"); fila.get(0).setEstado("Averiado");
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(),List.of(30)));
    }
    @Test void huecoPreexistenteNoImpideOtraCompraYEspecialCuentaComoDisponible() {
        List<Asiento> fila=fila(6); fila.get(1).setTipoDeAsiento("Especial");
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(10,30),List.of(40)));
        assertEquals(1,AsientosContiguos.huecosNuevos(fila,Set.of(10),List.of(30)).size());
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(10,30),List.of(20)));
    }
    @Test void noMezclaFilasOSalas() {
        List<Asiento> fila=fila(4); fila.get(0).setFila("B");
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(10),List.of(30)));
        fila.get(0).setFila("A"); fila.get(0).setIdSala(2);
        assertDoesNotThrow(()->AsientosContiguos.validar(fila,Set.of(10),List.of(30)));
    }
}
