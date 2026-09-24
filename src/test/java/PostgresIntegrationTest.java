import config.Conexion;
import config.Sesion;
import dao.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import services.*;
import static org.junit.jupiter.api.Assertions.*;

/** Solo usa tablas TEMPORARY, con sus propias secuencias; nunca escribe datos del cine. */
@EnabledIfSystemProperty(named = "cine.integration", matches = "true")
class PostgresIntegrationTest {
    private Connection c;
    private Supplier<Connection> conexiones;
    private LocalDate fecha;

    @BeforeEach void preparar() throws Exception {
        c = Conexion.getConexion();
        try (Statement s = c.createStatement()) {
            // El pooler puede reutilizar una sesión con tablas temporales de una prueba anterior.
            s.execute("DISCARD TEMP");
            s.execute("""
                SET search_path TO pg_temp, public;
                CREATE TEMP TABLE sala(id_sala serial PRIMARY KEY, capacidad_total int NOT NULL, asientos_especiales int,
                    tiempo_de_limpieza int NOT NULL, asientos_por_fila int NOT NULL, estado estado_sala);
                CREATE TEMP TABLE pelicula(id_pelicula serial PRIMARY KEY,nombre text NOT NULL,sinopsis text,duracion int NOT NULL,
                    genero text,director text,fecha_estreno date,tipo_estreno text,imagen_url text,estado estado_pelicula);
                CREATE TEMP TABLE asiento(id_asiento serial PRIMARY KEY,id_sala int NOT NULL REFERENCES pg_temp.sala,
                    tipo_de_asiento text,fila text NOT NULL,numero int NOT NULL,estado estado_asiento,UNIQUE(id_sala,fila,numero));
                CREATE TEMP TABLE funcion(id_funcion serial PRIMARY KEY,id_pelicula int NOT NULL REFERENCES pg_temp.pelicula,
                    id_sala int NOT NULL REFERENCES pg_temp.sala,fecha_proyeccion date NOT NULL,hora_inicio time NOT NULL,
                    hora_fin time NOT NULL,estado estado_funcion);
                CREATE TEMP TABLE usuario(id_usuario serial PRIMARY KEY,nombre text,rol text,estado estado_usuario);
                CREATE TEMP TABLE ticket(id_ticket serial PRIMARY KEY,id_funcion int NOT NULL REFERENCES pg_temp.funcion,
                    id_asiento int NOT NULL REFERENCES pg_temp.asiento,id_usuario int NOT NULL REFERENCES pg_temp.usuario,
                    monto numeric NOT NULL,fecha_hora_compra timestamp DEFAULT LOCALTIMESTAMP,UNIQUE(id_funcion,id_asiento));
                CREATE TEMP TABLE configuracion_cine(id int PRIMARY KEY CHECK(id=1),precio_boleto numeric(8,2) CHECK(precio_boleto>0));
                INSERT INTO usuario(id_usuario,nombre,rol,estado) VALUES (1,'Administrador prueba','Administrador','Activo');
                INSERT INTO configuracion_cine VALUES (1,4.50);
                """);
            try (ResultSet rs = s.executeQuery("SELECT n.nspname FROM pg_class t JOIN pg_namespace n ON t.relnamespace=n.oid WHERE t.oid='sala'::regclass")) {
                rs.next(); assertTrue(rs.getString(1).startsWith("pg_temp_"));
            }
            try (ResultSet rs = s.executeQuery("SELECT CURRENT_DATE + 2")) { rs.next(); fecha = rs.getDate(1).toLocalDate(); }
        }
        conexiones = () -> (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) return null;
                    try { return method.invoke(c, args); }
                    catch (InvocationTargetException e) { throw e.getCause(); }
                });
        Usuario admin = new Usuario(); admin.setIdUsuario(1); admin.setRol("Administrador"); admin.setNombre("Administrador prueba");
        Sesion.setUsuarioActual(admin);
    }

    @AfterEach void cerrar() throws Exception {
        Sesion.cerrarSesion();
        if (c != null) {
            try {
                if (!c.getAutoCommit()) c.rollback();
                c.setAutoCommit(true);
                try (Statement s = c.createStatement()) { s.execute("DISCARD TEMP"); }
            } finally {
                c.close();
            }
        }
    }

    private Pelicula pelicula() {
        Pelicula p = new Pelicula(0,"Prueba","Sinopsis",120,"Drama","Director",Date.valueOf(fecha),"Estreno","poster.png","CARTELERA");
        new PeliculaDAO(c).insertarPelicula(p);
        return p;
    }

    private Sala sala() {
        Sala s = new Sala(7,2,15,3,"ACTIVA");
        new SalaService(conexiones).guardar(s);
        return s;
    }

    private Funcion funcion(Pelicula p, Sala s, LocalDate dia, String hora) {
        Funcion f = new Funcion(0,p.getIdPelicula(),s.getIdSala(),Date.valueOf(dia),Time.valueOf(hora),null,"Programada");
        new FuncionService(conexiones).programar(f);
        return f;
    }

    @Test void ventaMultipleEsAtomicaYElAsientoPuedeVenderseEnOtraFuncion() throws Exception {
        Pelicula p = pelicula(); Sala s = sala();
        Funcion f = funcion(p,s,fecha,"18:00:00");
        List<Asiento> asientos = new AsientoDAO(c).obtenerAsientosPorSala(s.getIdSala());
        VentaService ventas = new VentaService(conexiones);
        // El primero se inserta, el segundo es inválido: toda la compra debe revertirse.
        assertThrows(IllegalStateException.class, () -> ventas.vender(f.getIdFuncion(),List.of(asientos.get(0).getIdAsiento(),999999),2,new BigDecimal("4.50")));
        assertTrue(new TicketDAO(c).obtenerTicketsPorFuncion(f.getIdFuncion()).isEmpty());
        List<Ticket> tickets = ventas.vender(f.getIdFuncion(),List.of(asientos.get(0).getIdAsiento(),asientos.get(1).getIdAsiento()),2,new BigDecimal("4.50"));
        assertEquals(2,tickets.size()); assertTrue(tickets.get(0).getIdTicket()>0); assertNotNull(tickets.get(0).getFechaHoraCompra());
        assertThrows(IllegalStateException.class, () -> ventas.vender(f.getIdFuncion(),List.of(asientos.get(0).getIdAsiento()),1,new BigDecimal("4.50")));
        assertEquals(2,new TicketDAO(c).obtenerTicketsPorFuncion(f.getIdFuncion()).size());
        Funcion otra = funcion(p,s,fecha,"20:15:00");
        assertEquals(1,ventas.vender(otra.getIdFuncion(),List.of(asientos.get(0).getIdAsiento()),1,new BigDecimal("4.50")).size());
        ReportesDAO.Reporte reporte = new ReportesDAO(c).obtenerReporte(fecha.minusDays(2),fecha.minusDays(1));
        assertEquals(3,reporte.tickets()); assertEquals(0,new BigDecimal("13.50").compareTo(reporte.total()));
        assertEquals(3,reporte.cajeros().get(0).tickets());
        s.setCapacidadTotal(8);
        assertThrows(IllegalStateException.class, () -> new SalaService(conexiones).guardar(s));
        assertEquals(7,new AsientoDAO(c).obtenerAsientosPorSala(s.getIdSala()).size());
    }

    @Test void salaGeneraUltimaFilaParcialYProgramacionRespetaLimpiezaYMedianoche() {
        Pelicula p = pelicula(); Sala s = sala();
        List<Asiento> asientos = new AsientoDAO(c).obtenerAsientosPorSala(s.getIdSala());
        assertEquals(7,asientos.size()); assertEquals("C",asientos.get(6).getFila()); assertEquals(1,asientos.get(6).getNumero());
        assertEquals(2,asientos.stream().filter(a -> "Especial".equals(a.getTipoDeAsiento())).count());
        Funcion f = funcion(p,s,fecha,"23:00:00");
        assertEquals(Time.valueOf("01:00:00"),f.getHoraFin());
        assertThrows(IllegalStateException.class, () -> funcion(p,s,fecha.plusDays(1),"01:14:00"));
        assertDoesNotThrow(() -> funcion(p,s,fecha.plusDays(1),"01:15:00"));
    }

    @Test void precioCambiadoAsientoAveriadoYOtraSalaNoPermitenVenta() throws Exception {
        Pelicula p = pelicula(); Sala s = sala(); Sala otra = sala(); Funcion f = funcion(p,s,fecha,"18:00:00");
        Asiento asiento = new AsientoDAO(c).obtenerAsientosPorSala(s.getIdSala()).get(0);
        Asiento ajeno = new AsientoDAO(c).obtenerAsientosPorSala(otra.getIdSala()).get(0);
        VentaService venta = new VentaService(conexiones);
        assertThrows(IllegalStateException.class, () -> venta.vender(f.getIdFuncion(),List.of(ajeno.getIdAsiento()),1,new BigDecimal("4.50")));
        new ConfiguracionDAO(c).guardarPrecio(new BigDecimal("5.00")); c.commit();
        assertThrows(IllegalStateException.class, () -> venta.vender(f.getIdFuncion(),List.of(asiento.getIdAsiento()),1,new BigDecimal("4.50")));
        try (PreparedStatement ps = c.prepareStatement("UPDATE Asiento SET estado='Averiado' WHERE id_asiento=?")) {
            ps.setInt(1,asiento.getIdAsiento()); ps.executeUpdate(); c.commit();
        }
        assertThrows(IllegalStateException.class, () -> venta.vender(f.getIdFuncion(),List.of(asiento.getIdAsiento()),1,new BigDecimal("5.00")));
        assertTrue(new TicketDAO(c).obtenerTicketsPorFuncion(f.getIdFuncion()).isEmpty());
        Sesion.getUsuarioActual().setRol("Cajero");
        assertThrows(IllegalStateException.class, () -> new ConfiguracionDAO(c).guardarPrecio(new BigDecimal("1.00")));
        assertThrows(IllegalStateException.class, () -> new SalaService(conexiones).guardar(s));
    }

    @Test void peliculasActualizanCamposVisiblesSinPerderPosterYLosReportesSeparanCajerosHomonimos() throws Exception {
        Pelicula p = pelicula();
        p.setNombre("Título actualizado"); p.setFechaEstreno(null); p.setImagenUrl(null);
        new PeliculaDAO(c).actualizarPelicula(p);
        Pelicula actual = new PeliculaDAO(c).obtenerTodas().get(0);
        assertEquals("Título actualizado",actual.getNombre()); assertEquals("poster.png",actual.getImagenUrl()); assertEquals(Date.valueOf(fecha),actual.getFechaEstreno());
        Sala s = sala(); Funcion f = funcion(actual,s,fecha,"18:00:00");
        List<Asiento> asientos = new AsientoDAO(c).obtenerAsientosPorSala(s.getIdSala());
        try (Statement st=c.createStatement()) { st.executeUpdate("INSERT INTO Usuario(id_usuario,nombre,rol,estado) VALUES (2,'Administrador prueba','Cajero','Activo')"); }
        new TicketDAO(c).venderTicket(new Ticket(0,f.getIdFuncion(),asientos.get(0).getIdAsiento(),1,new BigDecimal("4.50"),null));
        new TicketDAO(c).venderTicket(new Ticket(0,f.getIdFuncion(),asientos.get(1).getIdAsiento(),2,new BigDecimal("4.50"),null)); c.commit();
        ReportesDAO.Reporte reporte = new ReportesDAO(c).obtenerReporte(fecha.minusDays(2),fecha.minusDays(1));
        assertEquals(2,reporte.cajeros().size()); assertEquals(2,reporte.tickets());
        assertThrows(AccesoDatosException.class, () -> new PeliculaDAO(c).eliminarPelicula(p.getIdPelicula())); c.rollback();
        assertFalse(new PeliculaDAO(c).obtenerTodas().isEmpty());
    }
}
