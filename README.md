# Sistema de gestión de cine

Aplicación Java Swing con PostgreSQL y autenticación de Supabase. Requiere JDK 25 y Maven.

## Ejecutar

La clase principal es `com.mycompany.sistemagestioncine.SistemaGestionCine` (Run Project en NetBeans).
Abre el login; el MDI solo se crea después de autenticar un usuario con rol `Administrador`, `Admin` o `Cajero`.
Ejecutar el `main` del MDI también conduce al login.

```powershell
mvn compile
```

## Configuración de conexión y autenticación

1. Copiar `cine.local.properties.example` a `cine.local.properties` en la raíz del proyecto.
2. Completar la conexión JDBC y la URL/clave **pública** de Supabase Auth del **mismo proyecto**.
3. Reiniciar la aplicación después de cambiar esos parámetros.

Se puede configurar mediante propiedades JVM, variables de entorno o el archivo local, en ese orden de prioridad.
Los valores no definidos conservan los valores existentes en el código. `cine.local.properties` está ignorado por Git.

| Parámetro | Uso |
| --- | --- |
| `CINE_DB_URL` | URL JDBC del proyecto que contiene las tablas del cine |
| `CINE_DB_USER` | Usuario JDBC del pooler, normalmente `postgres.REFERENCIA` |
| `CINE_DB_PASSWORD` | Contraseña de la base de datos |
| `SUPABASE_AUTH_URL` | `https://REFERENCIA.supabase.co` del mismo proyecto |
| `SUPABASE_AUTH_KEY` | Clave pública anon/publishable de ese proyecto |
| `CINE_ZONA_HORARIA` | Zona del cine; predeterminada `America/El_Salvador` |

El login busca el `username` activo en la tabla `Usuario` y autentica su correo con Supabase Auth.
Por tanto, el correo también debe existir en Auth y cumplir su configuración de confirmación.
No se sustituye Auth por una validación local de la contraseña.

**Configuración heredada:** la conexión JDBC y Auth del código original apuntaban a proyectos diferentes.
La aplicación detecta esa inconsistencia y solicita corregirla antes de enviar credenciales.

## Precio de los boletos

Ejecutar una vez `database/001_configuracion_cine.sql` en el proyecto de base de datos elegido.
El script crea una tabla de configuración; no modifica películas, salas, usuarios ni ventas y no fija un precio inicial.
La tabla ya se creó durante esta integración en la base de datos que estaba configurada. Si se cambia de proyecto, ejecutar allí el script.

En el MDI, entrar como administrador a **Administración → Configuración** y guardar el precio general por boleto.
Se comparte entre todas las cajas y se vuelve a comprobar al confirmar cada compra. Los boletos ya vendidos conservan su monto.
Si otra caja tiene el precio anterior, deberá volver a abrir **Venta Tickets**.

## Recorridos

El lateral del MDI reúne **Peliculas**, **Salas**, **Funciones**, **Venta Tickets**, **Corte de caja**,
**Configuración** y **Cerrar Sesión**. Los seis módulos se muestran en el panel central.
Los accesos del menú **Administración** abren los mismos módulos. El cajero tiene habilitada la venta;
el administrador puede utilizar todos los módulos. El título de la ventana indica el módulo y el usuario actual.

- **Películas:** guardar, seleccionar una fila para actualizar o eliminar, limpiar y filtrar.
  La actualización conserva los campos de estreno y póster que no aparecen en el formulario.
  PostgreSQL impide eliminar películas con funciones relacionadas; se pueden marcar como `ARCHIVADA`.
- **Salas:** `Nueva Sala` limpia la selección. `Guardar Cambios` crea una sala o actualiza la seleccionada.
  La sala y sus asientos se guardan juntos. Se permite una última fila incompleta; los primeros asientos se marcan como especiales.
  Guardar una sala antigua con una cantidad incompleta de asientos regenera el mapa si no hay boletos vendidos.
  No se redistribuyen asientos con ventas ni se aumenta el tiempo de limpieza con funciones pendientes.
- **Administración → Programar función:** seleccionar película, sala, fecha y hora. El fin se calcula usando la duración.
  Se comprueba el cruce de horarios y la limpieza, incluyendo funciones que terminan al día siguiente.
- **Venta Tickets:** seleccionar una película con funciones futuras, horario y cantidad; continuar al mapa y elegir los asientos.
  Rojo deshabilitado indica vendido; gris indica averiado. Los asientos especiales se identifican en el texto de ayuda al pasar el cursor.
  Confirmar la compra guarda todos los boletos o ninguno, y abre el recibo con sus identificadores reales y la opción de imprimir.
- **Administración → Corte de caja:** reportes diarios, mensuales o anuales; el mes/año se obtiene de la fecha introducida.
  Incluye cantidad e importe por cajero y totales del mismo período.
- **Cerrar Sesión:** limpia el usuario actual, cierra el MDI y abre un login conectado.

Se conservaron los formularios existentes de películas, salas, taquilla y reportes. La programación de funciones
y el precio general tienen paneles propios dentro del MDI. El mapa de asientos y el recibo se abren como
diálogos durante la venta. Los botones laterales del MDI también están definidos en su archivo `.form`.

## Organización

```text
Login → LoginController → UsuarioDAO → Supabase Auth
                  ↓
                Sesion → MDI → controladores de cada módulo
                                 ↓
                          servicios → DAO → PostgreSQL
```

- `models`: entidades relacionadas por identificadores.
- `views`: formularios Swing; sus bloques generados y archivos `.form` conservan el diseño existente.
- `controlllers`: eventos, validación de entrada y presentación (se conserva el nombre del paquete existente).
- `services`: transacciones de sala/asientos, programación y venta, con comprobaciones de permisos y disponibilidad.
- `dao`: consultas y mapeo de entidades. Reciben una conexión que cierra quien la abre.
- `utils.Tareas`: consultas fuera del hilo de Swing y entrega de resultados en ese hilo, usando `LoadingView`.

## Pruebas

```powershell
mvn test
```

Prueba permisos, cierre de sesión, selección de asientos, importes, períodos, validaciones de sala, JSON y recibos.
Las pruebas PostgreSQL se omiten por defecto para que este comando no dependa de una conexión remota.

Para comprobar transacciones y consultas con PostgreSQL:

```powershell
mvn '-Dcine.integration=true' '-Dtest=PostgresIntegrationTest' test
```

Estas pruebas crean exclusivamente tablas temporales con secuencias propias y se eliminan al cerrar la conexión.
No insertan ni cambian registros reales del cine. Comprueban rollback de compras parciales, doble venta,
asientos ajenos/averiados, cambios de precio, limpieza, medianoche, reportes y conservación de campos de películas.

Para comprobar los formularios en un entorno con escritorio (sin mostrar ventanas):

```powershell
mvn '-Dcine.swing=true' '-Dtest=SwingIntegrationTest' test
```

Comprueba el botón del login, el reemplazo de paneles del MDI, los permisos y la selección de asientos por identificador.
