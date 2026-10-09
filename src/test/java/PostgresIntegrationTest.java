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
                    tiempo_de_limpieza int NOT NULL, asientos_por_fila int NOT NULL, estado estado_sala,
                    codigo_plano text,nombre text,motivo_inactividad text);
                CREATE TEMP TABLE pelicula(id_pelicula serial PRIMARY KEY,nombre text NOT NULL,sinopsis text,duracion int NOT NULL,
                    genero text,director text,fecha_estreno date,tipo_estreno text,imagen_url text,estado estado_pelicula);
                CREATE TEMP TABLE asiento(id_asiento serial PRIMARY KEY,id_sala int NOT NULL REFERENCES pg_temp.sala,
                    tipo_de_asiento text,fila text NOT NULL,numero int NOT NULL,estado estado_asiento,
                    columna_plano int,fila_plano int,motivo_inactividad text,UNIQUE(id_sala,fila,numero));
                CREATE TEMP TABLE funcion(id_funcion serial PRIMARY KEY,id_pelicula int NOT NULL REFERENCES pg_temp.pelicula,
                    id_sala int NOT NULL REFERENCES pg_temp.sala,fecha_proyeccion date NOT NULL,hora_inicio time NOT NULL,
                    hora_fin time NOT NULL,estado estado_funcion);
                CREATE TEMP TABLE usuario(id_usuario serial PRIMARY KEY,rol text,nombre text,username text,password_hash text,
                    dui text,email text,telefono text,fecha_nacimiento date,genero text,direccion text,fecha_contratacion date,
                    imagen_url text,estado estado_usuario);
                CREATE TEMP TABLE ticket(id_ticket serial PRIMARY KEY,id_funcion int NOT NULL REFERENCES pg_temp.funcion,
                    id_asiento int NOT NULL REFERENCES pg_temp.asiento,id_usuario int NOT NULL REFERENCES pg_temp.usuario,
                    monto numeric NOT NULL,fecha_hora_compra timestamp DEFAULT LOCALTIMESTAMP,UNIQUE(id_funcion,id_asiento));
                CREATE TEMP TABLE configuracion_cine(id int PRIMARY KEY CHECK(id=1),precio_boleto numeric(8,2) CHECK(precio_boleto>0));
                INSERT INTO usuario(id_usuario,nombre,rol,estado) VALUES (1,'Administrador prueba','Administrador','Activo');
                SELECT setval(pg_get_serial_sequence('usuario','id_usuario'),100);
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

    @Test void catalogoSeInstalaUnaVezYConservaSalasYEstados() throws Exception {
        Sala anterior=sala();
        String sql=java.nio.file.Files.readString(java.nio.file.Path.of("database/002_salas_predefinidas.sql"))
                .replace("ALTER TYPE estado_sala ADD VALUE IF NOT EXISTS 'MANTENIMIENTO';", "");
        try (Statement st=c.createStatement()) { st.execute(sql); }
        List<Sala> catalogo=new SalaDAO(c).listarSalas();
        assertEquals(7,catalogo.size());
        assertEquals(7,new AsientoDAO(c).obtenerAsientosPorSala(anterior.getIdSala()).size());
        assertEquals(List.of(48,72,96,108,144,180),catalogo.stream().filter(s->s.getCodigoPlano()!=null).map(Sala::getCapacidadTotal).toList());
        for(Sala sala:catalogo.stream().filter(s->s.getCodigoPlano()!=null).toList()) {
            List<Asiento> butacas=new AsientoDAO(c).obtenerAsientosPorSala(sala.getIdSala());
            assertEquals(sala.getCapacidadTotal(),butacas.size());
            assertEquals(sala.getAsientosEspeciales(),butacas.stream().filter(a->"Especial".equals(a.getTipoDeAsiento())).count());
            assertEquals(butacas.size(),butacas.stream().map(a->a.getFilaPlano()+":"+a.getColumnaPlano()).distinct().count());
            assertTrue(butacas.stream().allMatch(a->a.getFilaPlano()>=0 && a.getColumnaPlano()>=0));
            assertTrue(butacas.stream().mapToInt(Asiento::getColumnaPlano).max().orElseThrow()>=sala.getAsientosPorFila());
            assertThrows(IllegalStateException.class,()->new SalaService(conexiones).guardar(sala));
        }
        Sala primera=catalogo.get(1);
        List<Asiento> butacas=new AsientoDAO(c).obtenerAsientosPorSala(primera.getIdSala());
        new SalaService(conexiones).cambiarEstado(primera.getIdSala(),butacas.getFirst().getIdAsiento(),false,"Tapizado");
        try(Statement st=c.createStatement()) { st.execute(sql); }
        assertEquals(7,new SalaDAO(c).listarSalas().size());
        List<Asiento> repetidos=new AsientoDAO(c).obtenerAsientosPorSala(primera.getIdSala());
        assertEquals(butacas.stream().map(Asiento::getIdAsiento).toList(),repetidos.stream().map(Asiento::getIdAsiento).toList());
        assertEquals("Averiado",repetidos.getFirst().getEstado());
        assertEquals("Tapizado",repetidos.getFirst().getMotivoInactividad());
    }

    @Test void mantenimientoBloqueaVentaYProgramacionYRespetaBoletosPendientes() throws Exception {
        Pelicula pelicula=pelicula(); Sala sala=sala(); Sala otra=sala();
        Funcion funcion=funcion(pelicula,sala,fecha,"18:00:00");
        List<Asiento> asientos=new AsientoDAO(c).obtenerAsientosPorSala(sala.getIdSala());
        SalaService servicio=new SalaService(conexiones);
        VentaService ventas=new VentaService(conexiones);
        int asiento=asientos.getFirst().getIdAsiento();
        servicio.cambiarEstado(sala.getIdSala(),asiento,false,"Butaca rota");
        assertThrows(IllegalStateException.class,()->ventas.vender(funcion.getIdFuncion(),List.of(asiento),1,new BigDecimal("4.50")));
        servicio.cambiarEstado(sala.getIdSala(),null,false,"Limpieza profunda");
        assertTrue(new FuncionDAO(c).listarDisponibles().isEmpty());
        assertThrows(IllegalStateException.class,()->funcion(pelicula,sala,fecha.plusDays(1),"18:00:00"));
        assertThrows(IllegalStateException.class,()->ventas.vender(funcion.getIdFuncion(),List.of(asientos.get(1).getIdAsiento()),1,new BigDecimal("4.50")));
        servicio.cambiarEstado(sala.getIdSala(),null,true,null);
        assertEquals("Averiado",new AsientoDAO(c).obtenerAsientosPorSala(sala.getIdSala()).getFirst().getEstado());
        servicio.cambiarEstado(sala.getIdSala(),asiento,true,null);
        assertNull(new AsientoDAO(c).obtenerAsientosPorSala(sala.getIdSala()).getFirst().getMotivoInactividad());
        assertThrows(IllegalArgumentException.class,()->servicio.cambiarEstado(otra.getIdSala(),asiento,false,"Incorrecto"));
        ventas.vender(funcion.getIdFuncion(),List.of(asiento),1,new BigDecimal("4.50"));
        assertThrows(IllegalStateException.class,()->servicio.cambiarEstado(sala.getIdSala(),asiento,false,"Avería"));
        assertThrows(IllegalStateException.class,()->servicio.cambiarEstado(sala.getIdSala(),null,false,"Mantenimiento"));
        assertEquals("ACTIVA",new SalaDAO(c).obtenerSalaPorId(sala.getIdSala()).getEstado());
        assertEquals(1,new TicketDAO(c).obtenerTicketsPorFuncion(funcion.getIdFuncion()).size());
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

    @Test void ventaRevalidaHuecosConLasVentasActualesYSinInsertarBoletosParciales() {
        Pelicula p=pelicula(); Sala sala=sala(); Funcion f=funcion(p,sala,fecha,"18:00:00");
        List<Asiento> asientos=new AsientoDAO(c).obtenerAsientosPorSala(sala.getIdSala());
        int uno=asientos.get(0).getIdAsiento(),dos=asientos.get(1).getIdAsiento(),tres=asientos.get(2).getIdAsiento();
        VentaService ventas=new VentaService(conexiones);
        assertThrows(IllegalStateException.class,()->ventas.vender(f.getIdFuncion(),List.of(uno,tres),2,new BigDecimal("4.50")));
        assertTrue(new TicketDAO(c).obtenerTicketsPorFuncion(f.getIdFuncion()).isEmpty());
        // La caja abrió un mapa sin ventas; después otra caja vende A-1.
        assertDoesNotThrow(()->AsientosContiguos.validar(asientos,java.util.Set.of(),List.of(tres)));
        ventas.vender(f.getIdFuncion(),List.of(uno),1,new BigDecimal("4.50"));
        IllegalStateException error=assertThrows(IllegalStateException.class,
                ()->ventas.vender(f.getIdFuncion(),List.of(tres),1,new BigDecimal("4.50")));
        assertTrue(error.getMessage().contains("A-2"));
        assertEquals(1,new TicketDAO(c).obtenerTicketsPorFuncion(f.getIdFuncion()).size());
        assertEquals(2,ventas.vender(f.getIdFuncion(),List.of(dos,tres),2,new BigDecimal("4.50")).size());
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

    @Test void peliculasActualizanLaFichaCompletaYLosReportesSeparanCajerosHomonimos() throws Exception {
        Pelicula p = pelicula();
        p.setNombre("Título actualizado"); p.setFechaEstreno(Date.valueOf(fecha.minusDays(1)));
        p.setImagenUrl("https://example.com/nuevo-poster.jpg"); p.setTipoEstreno("MUNDIAL");
        new PeliculaDAO(c).actualizarPelicula(p);
        Pelicula actual = new PeliculaDAO(c).obtenerTodas().get(0);
        assertEquals("Título actualizado",actual.getNombre());
        assertEquals("https://example.com/nuevo-poster.jpg", actual.getImagenUrl());
        assertEquals(Date.valueOf(fecha.minusDays(1)), actual.getFechaEstreno());
        assertEquals("MUNDIAL", actual.getTipoEstreno());
        p.setFechaEstreno(null); p.setImagenUrl(null); p.setTipoEstreno(null);
        assertTrue(new PeliculaDAO(c).actualizarPelicula(p));
        actual = new PeliculaDAO(c).obtenerTodas().get(0);
        assertNull(actual.getImagenUrl()); assertNull(actual.getFechaEstreno()); assertNull(actual.getTipoEstreno());
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

    /** Solo consulta el catálogo: comprueba que la tabla real tiene los campos que usa el módulo de usuarios. */
    @Test void tablaRealDeUsuarioTieneElRolYLosCamposDelModulo() throws Exception {
        java.util.Map<String, String> columnas = new java.util.HashMap<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT column_name,udt_name FROM information_schema.columns "
                + "WHERE table_schema='public' AND table_name='usuario'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) columnas.put(rs.getString(1), rs.getString(2));
        }
        for (String columna : List.of("id_usuario", "rol", "nombre", "username", "password_hash", "dui", "email", "telefono",
                "fecha_nacimiento", "genero", "direccion", "fecha_contratacion", "imagen_url", "estado")) {
            assertTrue(columnas.containsKey(columna), "public.usuario no tiene la columna " + columna + ": " + columnas);
        }
        assertEquals("estado_usuario", columnas.get("estado"));
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT array_agg(e::text) FROM unnest(enum_range(NULL::estado_usuario)) e")) {
            rs.next(); assertTrue(List.of((Object[]) rs.getArray(1).getArray()).contains("Activo"));
        }
    }

    private final List<String> registradosEnAuth = new java.util.ArrayList<>();
    private UsuarioService usuarios() {
        return new UsuarioService(conexiones, (email, clave) -> { registradosEnAuth.add(email); return true; });
    }
    private static Usuario nuevoUsuario(String username, String rol) {
        Usuario u = new Usuario(); u.setNombre("Persona " + username); u.setUsername(username);
        u.setEmail(username + "@cine.com"); u.setRol(rol); u.setEstado("Activo");
        return u;
    }
    private static char[] clave() { return "Clave1234".toCharArray(); }
    /** Estado sin acceso según la enumeración real; no se presupone su nombre. */
    private String inactivo() throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT e::text FROM unnest(enum_range(NULL::estado_usuario)) e WHERE e::text<>'Activo' LIMIT 1")) {
            assertTrue(rs.next(), "estado_usuario necesita un estado distinto de Activo");
            return rs.getString(1);
        }
    }

    @Test void crudDeUsuariosGuardaEnUnaTransaccionConAuthYRechazaDuplicados() throws Exception {
        c.setAutoCommit(false);
        UsuarioService servicio = usuarios();
        Usuario cajero = nuevoUsuario("cajero1", "Cajero"); cajero.setDui("01234567-8");
        cajero.setFechaNacimiento(Date.valueOf("1999-05-10")); cajero.setGenero("Femenino");
        UsuarioService.Creado creado = servicio.crear(cajero, clave(), clave());
        assertTrue(creado.idUsuario() > 1); assertTrue(creado.requiereConfirmacion());
        assertEquals(List.of("cajero1@cine.com"), registradosEnAuth);
        try (PreparedStatement ps = c.prepareStatement("SELECT password_hash,estado::text,rol FROM usuario WHERE id_usuario=?")) {
            ps.setInt(1, creado.idUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(org.mindrot.jbcrypt.BCrypt.checkpw("Clave1234", rs.getString(1)));
                assertEquals("Activo", rs.getString(2)); assertEquals("Cajero", rs.getString(3));
            }
        }
        UsuarioService.Catalogo catalogo = servicio.cargar();
        assertEquals(2, catalogo.usuarios().size());
        assertTrue(catalogo.usuarios().stream().allMatch(u -> u.getPasswordHash() == null));
        assertEquals(List.of("Cajero", "Administrador"), catalogo.roles());
        assertTrue(catalogo.estados().contains("Activo"));

        Usuario mismoUsuario = nuevoUsuario("CAJERO1", "Cajero"); mismoUsuario.setEmail("otro@cine.com");
        Usuario mismoCorreo = nuevoUsuario("otro", "Cajero"); mismoCorreo.setEmail("Cajero1@Cine.com");
        Usuario mismoDui = nuevoUsuario("tercero", "Cajero"); mismoDui.setDui("01234567-8");
        for (Usuario repetido : List.of(mismoUsuario, mismoCorreo, mismoDui)) {
            assertThrows(IllegalArgumentException.class, () -> servicio.crear(repetido, clave(), clave()), repetido.getUsername());
        }
        // Si Supabase rechaza la cuenta, la fila tampoco queda en la tabla Usuario.
        UsuarioService rechazado = new UsuarioService(conexiones, (email, clave) -> {
            throw new IllegalArgumentException("El correo ya tiene una cuenta en Supabase Auth. Usa otro correo.");
        });
        assertThrows(IllegalArgumentException.class, () -> rechazado.crear(nuevoUsuario("sinauth", "Cajero"), clave(), clave()));
        assertEquals(2, new UsuarioDAO(c).listar().size());
        assertEquals(1, registradosEnAuth.size());

        cajero.setNombre("Cajera actualizada"); cajero.setEmail("cambiado@cine.com"); cajero.setEstado(inactivo());
        assertTrue(servicio.actualizar(cajero));
        Usuario guardado = new UsuarioDAO(c).listar().stream().filter(u -> u.getIdUsuario() == creado.idUsuario()).findFirst().orElseThrow();
        assertEquals("Cajera actualizada", guardado.getNombre()); assertEquals(inactivo(), guardado.getEstado());
        assertEquals("cajero1@cine.com", guardado.getEmail(), "El correo de Supabase Auth no se modifica");
        assertEquals(Date.valueOf("1999-05-10"), guardado.getFechaNacimiento()); assertEquals("Femenino", guardado.getGenero());
        Usuario inexistente = nuevoUsuario("fantasma", "Cajero"); inexistente.setIdUsuario(999999);
        assertFalse(servicio.actualizar(inexistente));
        assertFalse(servicio.eliminar(999999));
    }

    @Test void siempreQuedaUnAdministradorActivoYNoSeBorranUsuariosConVentas() throws Exception {
        c.setAutoCommit(false);
        UsuarioService servicio = usuarios();
        Usuario yo = new UsuarioDAO(c).bloquear(1); c.commit();
        yo.setUsername("admin"); yo.setEmail("admin@cine.com");
        yo.setRol("Cajero");
        assertThrows(IllegalStateException.class, () -> servicio.actualizar(yo));
        yo.setRol("Administrador"); yo.setEstado(inactivo());
        assertThrows(IllegalStateException.class, () -> servicio.actualizar(yo));
        assertThrows(IllegalStateException.class, () -> servicio.eliminar(1));

        Usuario otro = nuevoUsuario("admin2", "Administrador");
        servicio.crear(otro, clave(), clave());
        otro.setRol("Cajero");
        assertTrue(servicio.actualizar(otro), "Puede cambiar el rol porque el administrador actual sigue activo");
        otro.setRol("Administrador"); assertTrue(servicio.actualizar(otro));
        // La sesión pertenece a un administrador que otra caja desactivó: admin2 es el único activo.
        try (PreparedStatement ps = c.prepareStatement("UPDATE usuario SET estado=?::estado_usuario WHERE id_usuario=1")) {
            ps.setString(1, inactivo()); ps.executeUpdate();
        }
        c.commit();
        otro.setEstado(inactivo());
        assertThrows(IllegalStateException.class, () -> servicio.actualizar(otro));
        assertThrows(IllegalStateException.class, () -> servicio.eliminar(otro.getIdUsuario()));
        try (Statement st = c.createStatement()) { st.executeUpdate("UPDATE usuario SET estado='Activo' WHERE id_usuario=1"); }
        c.commit();

        Usuario cajero = nuevoUsuario("cajero2", "Cajero");
        servicio.crear(cajero, clave(), clave());
        Pelicula p = pelicula(); Sala s = sala(); Funcion f = funcion(p, s, fecha, "18:00:00");
        int asiento = new AsientoDAO(c).obtenerAsientosPorSala(s.getIdSala()).getFirst().getIdAsiento();
        new TicketDAO(c).venderTicket(new Ticket(0, f.getIdFuncion(), asiento, cajero.getIdUsuario(), new BigDecimal("4.50"), null));
        c.commit();
        IllegalStateException conVentas = assertThrows(IllegalStateException.class, () -> servicio.eliminar(cajero.getIdUsuario()));
        assertTrue(conVentas.getMessage().contains("Inactivo"));
        assertTrue(servicio.eliminar(otro.getIdUsuario()));
        assertEquals(List.of(1, cajero.getIdUsuario()), new UsuarioDAO(c).listar().stream().map(Usuario::getIdUsuario).sorted().toList());
    }

    @Test void rolYGeneroComoEnumeracionUsanLosValoresDeLaBaseDeDatos() throws Exception {
        c.setAutoCommit(false);
        try (Statement st = c.createStatement()) {
            st.execute("CREATE TYPE pg_temp.rol_prueba AS ENUM ('Administrador','Cajero','Cliente')");
            st.execute("CREATE TYPE pg_temp.genero_prueba AS ENUM ('F','M')");
            st.execute("ALTER TABLE usuario ALTER COLUMN rol TYPE pg_temp.rol_prueba USING rol::pg_temp.rol_prueba");
            st.execute("ALTER TABLE usuario ALTER COLUMN genero TYPE pg_temp.genero_prueba USING genero::pg_temp.genero_prueba");
        }
        c.commit();
        UsuarioService servicio = usuarios();
        UsuarioService.Catalogo catalogo = servicio.cargar();
        assertEquals(List.of("Administrador", "Cajero"), catalogo.roles(), "Se omiten los roles que el sistema no usa");
        assertEquals(List.of("F", "M"), catalogo.generos());
        Usuario cajero = nuevoUsuario("enum1", "Cajero"); cajero.setGenero("M");
        servicio.crear(cajero, clave(), clave());
        cajero.setRol("Administrador"); cajero.setGenero(null);
        assertTrue(servicio.actualizar(cajero));
        Usuario guardado = new UsuarioDAO(c).bloquear(cajero.getIdUsuario()); c.commit();
        assertEquals("Administrador", guardado.getRol()); assertNull(guardado.getGenero());
        assertEquals(1, new UsuarioDAO(c).contarAdministradoresActivos(1));
    }
}
