# Salas predefinidas

Ejecutar `002_salas_predefinidas.sql` completo en la base configurada antes de usar mantenimiento.
El script es transaccional y repetible. Agrega seis salas identificadas por `CINE-01` a `CINE-06`,
conserva las salas anteriores y sus identificadores, y nunca reemplaza asientos ni boletos.
Los números reales de sala son los asignados por PostgreSQL; no se presupone que estén libres del 1 al 6.

| Plano | Nombre | Capacidad | Filas | Pasillos longitudinales | Especiales |
| --- | --- | ---: | --- | ---: | ---: |
| CINE-01 | Boutique | 48 | A–F | 1 | 2 |
| CINE-02 | Clásica | 72 | A–H | 1 | 2 |
| CINE-03 | Panorámica | 96 | A–H | 2 | 4 |
| CINE-04 | Central | 108 | A–I | 1 | 4 |
| CINE-05 | Gran Formato | 144 | A–L | 2 + transversal | 4 |
| CINE-06 | Estrenos | 180 | A–L | 2 + transversal | 4 |

Son adaptaciones para este proyecto, no reproducciones exactas ni planos de construcción.
Se tomaron como referencia la pantalla frontal, la división en bloques y pasillos y las filas
de diferente longitud de los siguientes planos públicos:

- [Watershed, planos de sus tres salas (2020)](https://www.watershed.co.uk/sites/default/files/downloads/Watershed-CinemaSeatingPlans_Sept2020.pdf).
  Sus bloqueos históricos por distanciamiento no se trasladan al catálogo.
- [Prince Charles Cinema, sala inferior](https://princecharlescinema.com/wp-content/uploads/2024/05/Seating-Plan-Downstairs.pdf).
- [Prince Charles Cinema, sala superior](https://princecharlescinema.com/wp-content/uploads/2024/05/Seating-Plan-Upstairs.pdf).
- [BFI NFT3](https://www.bfi.org.uk/venue-hire/hiring-bfi-southbank/nft3): referencia de escala (108 localidades).

La fila A está junto a la pantalla. Los espacios vacíos son pasillos, nunca asientos seleccionables.
Administración y taquilla leen las mismas coordenadas persistidas. Las salas anteriores usan su
distribución original. Desactivar una sala conserva sus funciones y bloquea programación/ventas;
si tiene boletos de funciones pendientes se exige resolverlos antes. Lo mismo se aplica a un asiento.
Reactivar una sala no reactiva sus asientos averiados. El motivo es obligatorio al desactivar.
