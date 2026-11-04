/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views;

import dao.ReporteFuncionDAO;
import dao.ReporteFuncionDAO.ResumenFuncion;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import views.estilos.Tema;

/**
 *
 * @author axele
 */
public class ReporteFuncionView extends JPanel{
    private final ReporteFuncionDAO dao = new ReporteFuncionDAO();

    private final JSpinner selectorFecha;
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JLabel lblResumen;

    private List<ResumenFuncion> funciones = new ArrayList<>();

    public ReporteFuncionView() {

        setLayout(new BorderLayout(16, 16));
        setBackground(Tema.FONDO);
        setBorder(BorderFactory.createEmptyBorder(
                24, 24, 24, 24));

        JLabel titulo = Tema.texto(
                "Reportes por función",
                Tema.TITULO,
                Tema.TEXTO
        );

        JLabel subtitulo = Tema.texto(
                "Consulta las funciones y genera reportes individuales.",
                Tema.CUERPO,
                Tema.SECUNDARIO
        );

        JPanel encabezado = new JPanel(
                new GridLayout(2, 1, 0, 6));
        encabezado.setOpaque(false);
        encabezado.add(titulo);
        encabezado.add(subtitulo);

        selectorFecha = new JSpinner(
                new SpinnerDateModel()
        );

        selectorFecha.setEditor(
                new JSpinner.DateEditor(
                        selectorFecha, "dd/MM/yyyy"
                )
        );

        JButton btnBuscar = Tema.botonPrimario("Buscar funciones");
        JButton btnActualizar = Tema.botonSecundario("Actualizar tabla");

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        filtros.setBackground(Tema.SUPERFICIE);

        filtros.add(new JLabel("Fecha:"));
        filtros.add(selectorFecha);
        filtros.add(btnBuscar);
        filtros.add(btnActualizar);

        JPanel superior = new JPanel(new BorderLayout(0, 16));
        superior.setOpaque(false);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(filtros, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);

        String[] columnas = {
            "ID", "Película", "Fecha", "Hora",
            "Sala", "Estado", "Boletos",
            "Ingresos", "Acción"
        };

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(
                    int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(42);
        tabla.setFont(Tema.CUERPO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        tabla.getTableHeader().setBackground(Tema.TEXTO);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(Tema.ETIQUETA);

        tabla.getColumnModel().getColumn(1)
                .setPreferredWidth(230);

        tabla.getColumnModel().getColumn(8)
                .setPreferredWidth(120);

        tabla.getColumnModel().getColumn(8)
                .setCellRenderer(
                    (t, valor, seleccionado, foco,
                     fila, columna) -> {

                        JLabel etiqueta = new JLabel(
                                "Generar PDF",
                                SwingConstants.CENTER
                        );
                        etiqueta.setOpaque(true);
                        etiqueta.setBackground(Tema.PRIMARIO);
                        etiqueta.setForeground(Color.WHITE);
                        etiqueta.setFont(Tema.ETIQUETA);
                        etiqueta.setBorder(
                                BorderFactory.createEmptyBorder(
                                        4, 8, 4, 8)
                        );
                        return etiqueta;
                    }
                );

        JPanel contenido = Tema.tarjeta();
        contenido.add(new JScrollPane(tabla),
                BorderLayout.CENTER);

        add(contenido, BorderLayout.CENTER);

        lblResumen = Tema.texto(
                "Selecciona una fecha para consultar.",
                Tema.CUERPO,
                Tema.SECUNDARIO
        );

        add(lblResumen, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> cargarFunciones());
        btnActualizar.addActionListener(e -> cargarFunciones());

        tabla.addMouseListener(
            new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e) {

                    int fila = tabla.rowAtPoint(
                            e.getPoint()
                    );
                    int columna = tabla.columnAtPoint(
                            e.getPoint()
                    );

                    if (fila < 0 || columna < 0) {
                        return;
                    }

                    if (tabla.convertColumnIndexToModel(
                            columna) != 8) {
                        return;
                    }

                    int filaModelo =
                            tabla.convertRowIndexToModel(fila);

                    generarReporte(
                            funciones.get(filaModelo)
                    );
                }
            }
        );

        cargarFunciones();
    }

    private void cargarFunciones() {

        Date fechaSeleccionada =
                (Date) selectorFecha.getValue();

        LocalDate fecha = fechaSeleccionada.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        try {

            funciones = dao.listarPorFecha(fecha);
            modelo.setRowCount(0);

            long totalBoletos = 0;
            BigDecimal totalIngresos = BigDecimal.ZERO;

            for (ResumenFuncion f : funciones) {

                modelo.addRow(new Object[]{
                    f.idFuncion(),
                    f.pelicula(),
                    f.fecha(),
                    f.hora(),
                    "Sala " + f.idSala(),
                    f.estado(),
                    f.boletos(),
                    String.format("$ %.2f", f.ingresos()),
                    "Generar PDF"
                });

                totalBoletos += f.boletos();
                totalIngresos =
                        totalIngresos.add(f.ingresos());
            }

            lblResumen.setText(
                    "Funciones: " + funciones.size()
                    + "   |   Boletos vendidos: "
                    + totalBoletos
                    + "   |   Ingresos: $ "
                    + totalIngresos
            );

        } catch (SQLException | RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudieron consultar las funciones:\n"
                    + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void generarReporte(ResumenFuncion funcion) {

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Generar reporte de la función #"
                + funcion.idFuncion() + "?\n"
                + funcion.pelicula(),
                "Confirmar reporte",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        JFileChooser selector = new JFileChooser();

        selector.setSelectedFile(new java.io.File(
                "reporte_funcion_"
                + funcion.idFuncion() + ".pdf"
        ));

        if (selector.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {

            java.io.File archivo =
                    selector.getSelectedFile();

            if (!archivo.getName().toLowerCase()
                    .endsWith(".pdf")) {
                archivo = new java.io.File(
                        archivo.getAbsolutePath() + ".pdf"
                );
            }

            if (archivo.exists()) {
                int sobrescribir =
                        JOptionPane.showConfirmDialog(
                                this,
                                "El archivo ya existe. "
                                + "¿Deseas reemplazarlo?",
                                "Confirmar",
                                JOptionPane.YES_NO_OPTION
                        );

                if (sobrescribir != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            services.ReporteFuncionPDF.generar(
                    funcion.idFuncion(), archivo
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Reporte generado correctamente:\n"
                    + archivo.getAbsolutePath()
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al generar PDF:\n"
                    + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}