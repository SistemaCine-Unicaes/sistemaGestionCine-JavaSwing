package controlllers;

import java.sql.Date;
import java.util.List;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import models.Usuario;
import org.junit.jupiter.api.Test;
import views.UsuariosView;
import static org.junit.jupiter.api.Assertions.*;

class UsuariosFormularioTest {
    private static UsuariosView completa() {
        UsuariosView vista = new UsuariosView();
        vista.setNombre("  Ana López  "); vista.setUsername(" ana.lopez "); vista.setEmail(" Ana.Lopez@Cine.COM ");
        vista.setRol("Cajero"); vista.setEstado("Activo");
        return vista;
    }

    @Test void leeYNormalizaLosDatosAlCrearYEditar() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UsuariosView vista = completa();
            vista.setDui("01234567-8"); vista.setTelefono("7777-8888"); vista.setGenero("Femenino");
            vista.setDireccion("  Santa Ana  "); vista.setFechaNacimiento("29/02/2000"); vista.setFechaContratacion("01/10/2026");
            Usuario nuevo = UsuariosController.leerFormulario(vista, 0);
            assertEquals("Ana López", nuevo.getNombre()); assertEquals("ana.lopez", nuevo.getUsername());
            assertEquals("ana.lopez@cine.com", nuevo.getEmail()); assertEquals("Cajero", nuevo.getRol());
            assertEquals("Activo", nuevo.getEstado()); assertEquals("01234567-8", nuevo.getDui());
            assertEquals("Femenino", nuevo.getGenero()); assertEquals("Santa Ana", nuevo.getDireccion());
            assertEquals(Date.valueOf("2000-02-29"), nuevo.getFechaNacimiento());
            assertEquals(Date.valueOf("2026-10-01"), nuevo.getFechaContratacion());
            assertNull(nuevo.getPasswordHash());
            vista.setDui(" "); vista.setTelefono(""); vista.setDireccion(""); vista.setGenero(null);
            vista.setFechaNacimiento(""); vista.setFechaContratacion(""); vista.setRol("Administrador");
            Usuario editado = UsuariosController.leerFormulario(vista, 15);
            assertEquals(15, editado.getIdUsuario()); assertEquals("Administrador", editado.getRol());
            assertNull(editado.getDui()); assertNull(editado.getTelefono()); assertNull(editado.getDireccion());
            assertNull(editado.getGenero()); assertNull(editado.getFechaNacimiento()); assertNull(editado.getFechaContratacion());
        });
    }

    @Test void rechazaDatosInvalidosAntesDeGuardar() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UsuariosView vista = completa();
            vista.setNombre(" ");
            assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0));
            vista.setNombre("a".repeat(101));
            assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0));
            vista.setNombre("Ana López");
            for (String username : List.of("", "ab", "ana lopez", "añá", "a".repeat(31), "ana@cine")) {
                vista.setUsername(username);
                assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0), username);
            }
            vista.setUsername("ana_lopez-2");
            for (String correo : List.of("", "ana", "ana@cine", "ana @cine.com", "@cine.com")) {
                vista.setEmail(correo);
                assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0), correo);
            }
            vista.setEmail("ana@cine.com");
            vista.setRol("Cliente");
            assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0));
            vista.setRol("Cajero");
            for (String dui : List.of("123456789", "1234567-8", "abcdefgh-i")) {
                vista.setDui(dui);
                assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0), dui);
            }
            vista.setDui("");
            for (String telefono : List.of("12", "teléfono", "7777-8888-9999-0000-1")) {
                vista.setTelefono(telefono);
                assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0), telefono);
            }
            vista.setTelefono("+503 7777-8888");
            for (String fecha : List.of("31/02/2000", "2000-01-01", "01/01/2999", "01/01/1800")) {
                vista.setFechaNacimiento(fecha);
                assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0), fecha);
            }
            vista.setFechaNacimiento("10/05/2000"); vista.setFechaContratacion("01/01/1999");
            assertThrows(IllegalArgumentException.class, () -> UsuariosController.leerFormulario(vista, 0));
            vista.setFechaContratacion("01/01/2024");
            assertDoesNotThrow(() -> UsuariosController.leerFormulario(vista, 0));
        });
    }

    @Test void edicionProtegeCorreoYContraseñaYConservaRolesAnteriores() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            UsuariosView vista = new UsuariosView();
            vista.setOpciones(List.of("Cajero", "Administrador"), List.of("Activo", "Inactivo"), List.of("Femenino", "Masculino"));
            vista.setPassword("Clave1234"); vista.setConfirmacion("Clave1234");
            vista.setModoEdicion(true);
            assertEquals(0, vista.getPassword().length); assertEquals(0, vista.getConfirmacion().length);
            List<JTextField> campos = componentes(vista);
            JTextField correo = campos.stream().filter(c -> c.getToolTipText() != null && c.getToolTipText().contains("Supabase")).findFirst().orElseThrow();
            assertFalse(correo.isEnabled());
            assertTrue(campos.stream().filter(c -> c instanceof JPasswordField).noneMatch(JTextField::isEnabled));
            vista.setRol("Admin"); assertEquals("Admin", vista.getRol());
            vista.setGenero("Otro"); assertEquals("Otro", vista.getGenero());
            vista.setGenero(null); assertNull(vista.getGenero());
            vista.setModoEdicion(false);
            assertTrue(correo.isEnabled());
            assertTrue(campos.stream().filter(c -> c instanceof JPasswordField).allMatch(JTextField::isEnabled));
        });
    }

    private static List<JTextField> componentes(java.awt.Container raiz) {
        List<JTextField> lista = new java.util.ArrayList<>();
        for (java.awt.Component hijo : raiz.getComponents()) {
            if (hijo instanceof JTextField campo) lista.add(campo);
            if (hijo instanceof java.awt.Container contenedor) lista.addAll(componentes(contenedor));
        }
        return lista;
    }
}
