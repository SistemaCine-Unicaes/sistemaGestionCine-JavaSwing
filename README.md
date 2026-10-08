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

### Cuentas creadas desde el módulo Usuarios

El módulo registra la cuenta con la clave pública de Supabase Auth (no requiere la clave de servicio).
Con **Confirm email** activo en Supabase (configuración de Auth, proveedor Email), el nuevo usuario debe abrir el enlace
que recibe por correo antes de iniciar sesión; el login lo indica si intenta entrar sin confirmar. El enlace confirma la cuenta
aunque la página de destino no cargue. El servidor de correo incluido en Supabase limita los envíos por hora;
si aparece el aviso de límite, esperar o configurar un SMTP propio, o desactivar **Confirm email** si el equipo lo decide.
Eliminar un usuario de la tabla no borra su cuenta de Auth; ese correo no se puede reutilizar hasta borrarla en el panel de Supabase.

## Precio de los boletos

Ejecutar una vez `database/001_configuracion_cine.sql` en el proyecto de base de datos elegido.
El script crea una tabla de configuración; no modifica películas, salas, usuarios ni ventas y no fija un precio inicial.
La tabla ya se creó durante esta integración en la base de datos que estaba configurada. Si se cambia de proyecto, ejecutar allí el script.

En el MDI, entrar como administrador a **Administración → Configuración** y guardar el precio general por boleto.
Se comparte entre todas las cajas y se vuelve a comprobar al confirmar cada compra. Los boletos ya vendidos conservan su monto.
Si otra caja tiene el precio anterior, deberá volver a abrir **Venta de boletos**.

## Recorridos

El lateral del MDI reúne **Cartelera**, **Películas**, **Salas**, **Funciones**, **Venta de boletos**, **Corte de caja**,
**Configuración**, **Usuarios** y **Cerrar Sesión**. Los ocho módulos se muestran en el panel central.
Los accesos del menú **Administración** abren los mismos módulos. El cajero solo ve la sección **Operación**
(cartelera y venta): la sección **Administración** del lateral, el menú **Administración** y los módulos administrativos
del menú **Módulos** no se le muestran. Además, cada módulo vuelve a comprobar el rol antes de abrirse.
El administrador puede utilizar todos los módulos. El título de la ventana indica el módulo y el usuario actual.

En **Cartelera** se muestran únicamente los pósteres de películas con estado `CARTELERA` y su botón
**Ver horarios**, con búsqueda por título sin distinguir acentos. Ese botón abre una pantalla independiente
con la información de la película, todas las fechas con funciones futuras disponibles y los horarios agrupados por sala.
Elegir un horario abre taquilla con esa película y función seleccionadas; la disponibilidad se consulta de nuevo
antes de continuar. **Volver a cartelera** regresa al catálogo y **Actualizar horarios** recarga las funciones.
**Actualizar cartelera** recarga las películas. Las imágenes admiten rutas locales o URL HTTP(S).
La vista utiliza los colores, tipografía, tarjetas, campos y botones compartidos de `views.estilos.Tema`.

- **Películas:** **Guardar película** agrega una ficha nueva; seleccionar una fila permite **Guardar cambios** o eliminarla.
  **Nueva película** limpia la selección y el formulario. Incluye título, sinopsis, duración, género, director,
  estado, fecha opcional en `dd/mm/aaaa`, tipo de estreno (`MUNDIAL`, `ESTANDAR` o sin especificar) y URL/ruta del póster.
  **Ver póster** muestra la imagen en segundo plano. Al editar se actualizan también el estreno y el póster;
  dejar vacíos esos campos elimina sus valores. **Actualizar listado** recarga las películas registradas.
  PostgreSQL impide eliminar películas con funciones relacionadas; se pueden marcar como `ARCHIVADA`.
- **Salas:** catálogo de tarjetas; **Ver sala y asientos** abre un modal con el plano compartido con taquilla.
  Solo permite desactivar/reactivar la sala completa o asientos individuales, con motivo obligatorio al desactivar.
  Los mapas son fijos. No permite crear, eliminar ni redistribuir salas desde el módulo.
  Los boletos vendidos de funciones pendientes impiden desactivar la sala o el asiento afectado.
  Reactivar una sala conserva sus asientos averiados. **Actualizar salas** recarga estados y motivos.
- **Administración → Programar función:** seleccionar película, sala, fecha y hora. El fin se calcula usando la duración.
  Se comprueba el cruce de horarios y la limpieza, incluyendo funciones que terminan al día siguiente.
- **Venta de boletos:** seleccionar una película con funciones futuras, horario y cantidad; continuar al mapa y elegir los asientos.
  El resumen muestra el póster de la película seleccionada, los datos de la función y el importe de la compra.
  Rojo deshabilitado indica vendido; gris indica averiado. Los asientos especiales se identifican en el texto de ayuda al pasar el cursor.
  Confirmar la compra guarda todos los boletos o ninguno, y abre el recibo con sus identificadores reales y la opción de imprimir.
  La selección no puede crear un asiento libre aislado entre asientos vendidos o seleccionados de la misma fila y bloque.
  El mapa señala el hueco y bloquea **Confirmar compra** hasta corregirlo; permite completar la selección en cualquier orden.
  Se respetan pasillos, posiciones inexistentes y asientos averiados. No bloquea huecos anteriores ni asientos en los extremos.
  La regla se comprueba otra vez dentro de la transacción de venta con los boletos actuales de esa función.
- **Administración → Corte de caja:** reportes diarios, mensuales o anuales; el mes/año se obtiene de la fecha introducida.
  Incluye cantidad e importe por cajero y totales del mismo período.
- **Administración → Usuarios:** crea, lista, edita y elimina cajeros y administradores (tabla `Usuario`, campo `rol`).
  **Guardar usuario** crea la cuenta en Supabase Auth y la fila en `Usuario` dentro de la misma transacción:
  si Supabase la rechaza, no queda ninguna fila. La contraseña exige 8 caracteres con letras y números y se confirma dos veces.
  Se rechazan usuario, correo o DUI repetidos (sin distinguir mayúsculas). Al editar se cambian nombre, usuario, rol, estado
  y datos personales; el correo y la contraseña pertenecen a Supabase Auth y no se modifican desde la aplicación.
  Para impedir el acceso de alguien basta con cambiarlo a **Inactivo**; las ventas de un cajero inactivo se conservan
  en el corte de caja. **Eliminar usuario** solo se permite si no tiene ventas. Ningún administrador puede quitarse
  su propio rol, desactivarse ni eliminarse, y siempre debe quedar al menos un administrador activo.
  Los roles, estados y géneros se leen de las enumeraciones de PostgreSQL cuando la columna es de ese tipo.
  Los usuarios anteriores con rol `Admin` se siguen reconociendo como administradores.
- **Cerrar Sesión:** limpia el usuario actual, cierra el MDI y abre un login conectado.

Se conservaron los formularios existentes de películas, taquilla y reportes. Salas utiliza un catálogo programático. La programación de funciones
y el precio general tienen paneles propios dentro del MDI. El mapa de asientos y el recibo se abren como
diálogos durante la venta. Los botones laterales del MDI también están definidos en su archivo `.form`.
Películas, el login y el menú lateral aplican `Tema` después de inicializar los controles de NetBeans. Salas comparte el mismo tema.
El lateral agrupa operación y administración, resalta el módulo activo y muestra el nombre y rol del usuario.
El login incluye un motivo de sala de cine y permite mostrar u ocultar la contraseña. La ventana de carga
utiliza una barra indeterminada con el mismo tema, conservando las operaciones en segundo plano.

## Salas predefinidas y mantenimiento

Ejecutar una vez **`database/002_salas_predefinidas.sql`** completo en la base del cine.
Agrega seis distribuciones de 48, 72, 96, 108, 144 y 180 asientos, con pasillos y localidades especiales.
Es repetible y conserva las salas anteriores, sus funciones, ventas y asientos. Por eso, si ya había salas,
el catálogo mostrará las anteriores además de las seis nuevas. No presupone identificadores del 1 al 6.
Las referencias de planos y las características están en [database/SALAS.md](database/SALAS.md).
La migración debe instalarse también al cambiar de base de datos; la aplicación no modifica el esquema al arrancar.

## Ventanas pequeñas

Los formularios y tarjetas reorganizan sus columnas según el ancho; las páginas permiten desplazamiento vertical.
El MDI admite ventanas desde 480 × 360 y oculta el lateral automáticamente por debajo de 1050 píxeles.
**Módulos** mantiene disponibles todos los accesos autorizados y el cierre de sesión; **Mostrar / ocultar menú** alterna el lateral.
El login oculta su ilustración en ventanas estrechas. Los diálogos se limitan al tamaño de la pantalla.
Las tablas y los planos grandes conservan desplazamiento horizontal para mantener legibles columnas y asientos.
Los pasillos del mapa nunca cambian al redimensionar.

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
También prueba el formulario y las reglas de usuarios: datos obligatorios, formatos de correo/DUI/teléfono/fechas,
contraseña y confirmación, roles heredados y que un cajero no pueda gestionar usuarios ni abrir una conexión.
Las pruebas PostgreSQL se omiten por defecto para que este comando no dependa de una conexión remota.

Para comprobar transacciones y consultas con PostgreSQL:

```powershell
mvn '-Dcine.integration=true' '-Dtest=PostgresIntegrationTest' test
```

Estas pruebas crean exclusivamente tablas temporales con secuencias propias y se eliminan al cerrar la conexión.
No insertan ni cambian registros reales del cine. Comprueban rollback de compras parciales, doble venta,
asientos ajenos/averiados, cambios de precio, limpieza, medianoche, reportes y edición completa de fichas de películas.
También prueban la instalación repetible de los seis planos, sus capacidades/coordenadas, la conservación de salas
anteriores y el bloqueo de mantenimiento por boletos pendientes. Requieren haber instalado el estado
`MANTENIMIENTO` de la migración; las pruebas no alteran los tipos compartidos de la base.
Para usuarios comprueban (solo leyendo el catálogo) que `public.usuario` tiene `rol` y los demás campos del módulo,
y sobre tablas temporales: creación con rollback si Auth rechaza la cuenta, duplicados, que siempre quede un administrador
activo, que no se eliminen usuarios con ventas y que `rol`/`genero` funcionen también como enumeraciones.
Supabase Auth se sustituye por un registro simulado: estas pruebas no crean cuentas ni envían correos.

Para comprobar los formularios en un entorno con escritorio (sin mostrar ventanas):

```powershell
mvn '-Dcine.swing=true' '-Dtest=SwingIntegrationTest' test
```

Comprueba el botón del login, el reemplazo de paneles del MDI, los permisos, la selección de asientos por identificador
y el modal de mantenimiento. Para el cajero verifica que no aparezcan la sección ni los accesos de administración. Las pruebas de paneles comprueban el cambio entre anchos de 480, 800 y 1280 píxeles.
