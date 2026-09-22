# Guía visual del equipo

## Abrir la pantalla

En NetBeans, abrir `src/main/java/views/GuiaEstilosView.java` y elegir **Run File / Ejecutar archivo** (`Shift + F6`).
La clase tiene su propio `main`; funciona sin login, credenciales ni base de datos.

También se puede ejecutar desde la raíz del proyecto, con JDK 25:

```powershell
mvn compile
java -cp target/classes views.GuiaEstilosView
```

La guía tiene tres secciones:

- **Componentes:** colores, tipografía, botones, campos, mensajes, espaciado y tablas.
- **Pantalla ejemplo:** una composición de cartelera con tarjetas y horarios ficticios.
- **Trabajo en equipo:** reparto de pantallas, responsabilidad de archivos y lista de revisión temporal.

Los botones demuestran estados locales. No guardan películas, generan ventas ni abren funciones reales.
Esta guía está escrita en Java; no tiene un archivo `.form` para el diseñador visual.

## Reutilizar el estilo

La fuente compartida es `src/main/java/views/estilos/Tema.java`. Cada persona puede usarla en sus vistas:

```java
import views.estilos.Tema;

JPanel tarjeta = Tema.tarjeta();
JLabel titulo = Tema.texto("Películas", Tema.TITULO, Tema.TEXTO);
JTextField nombre = Tema.campo("", 20);
JButton guardar = Tema.botonPrimario("Guardar película");
JButton cancelar = Tema.botonSecundario("Cancelar");
JScrollPane listado = Tema.tabla(miTabla);
JLabel mensaje = Tema.mensaje("Éxito · Película guardada.", Tema.EXITO);
```

Las fábricas crean componentes Swing normales: añadan sus eventos y sus layouts en cada vista.
`Tema.tabla` aplica el estilo a la tabla recibida y devuelve el contenedor con desplazamiento.
En formularios existentes se pueden aplicar las constantes con `setFont`, `setForeground` y `setBackground`
después de `initComponents()`, sin editar manualmente los bloques generados por NetBeans.

| Elemento | Acuerdo inicial |
| --- | --- |
| Fuente | SansSerif (fuente lógica de Java) |
| Título | 28, negrita |
| Sección | 18, negrita |
| Texto | 14, normal |
| Etiqueta | 12, negrita |
| Espaciado | 8, 16, 24 y 32 unidades de Swing |
| Campo | 40 unidades de alto |
| Fila de tabla | 44 unidades de alto |
| Acción principal | Granate `#B4233C` |
| Texto / cabecera | Azul oscuro `#182235` |
| Fondo / tarjeta | `#F4F5F7` / `#FFFFFF` |

Las medidas son unidades lógicas de Swing: el sistema operativo puede escalarlas según la pantalla.
La familia SansSerif puede verse ligeramente distinta en cada sistema operativo.

## Coordinar cambios

Cada persona trabaja en las vistas que figuran en la pestaña **Trabajo en equipo**.
La persona 1 coordina cambios de `Tema.java` e integra la navegación en `MDI`.
La guía no aplica estilos automáticamente al resto del sistema; sirve de base para esa adaptación.
La cartelera es una muestra visual: su pantalla funcional sigue pendiente.

Antes de integrar, revisen el recorrido login → cartelera (cuando exista) → venta → asientos → ticket,
las pantallas administrativas, el foco mediante Tab, los mensajes, la selección de tablas y el tamaño de ventana.
Mantengan sincronizados `.java` y `.form` en las pantallas gestionadas con el diseñador de NetBeans.
