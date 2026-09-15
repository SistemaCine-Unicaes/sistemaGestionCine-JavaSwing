package com.mycompany.sistemagestioncine.views;

public class CorteCajaView extends javax.swing.JPanel {

    public CorteCajaView() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cbTipoReporte = new javax.swing.JComboBox<>();
        btnGenerarReporte = new javax.swing.JButton();
        lblTicketsVendidos = new javax.swing.JLabel();
        lblTotalIngresos = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVentasCajeros = new javax.swing.JTable();

        jLabel1.setText("Fecha (dd/mm/aaaa):");

        jLabel2.setText("Tipo:");

        cbTipoReporte.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Diario", "Mensual", "Anual" }));

        btnGenerarReporte.setText("Generar Reporte");

        lblTicketsVendidos.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTicketsVendidos.setText("Tickets Vendidos: 0");

        lblTotalIngresos.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotalIngresos.setText("Total Ingresos: $0.00");

        tblVentasCajeros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Cajero", "Tickets Vendidos", "Total Recaudado"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblVentasCajeros);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbTipoReporte, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(btnGenerarReporte))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTicketsVendidos)
                        .addGap(50, 50, 50)
                        .addComponent(lblTotalIngresos)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(cbTipoReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerarReporte))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTicketsVendidos)
                    .addComponent(lblTotalIngresos))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    public String getFecha() {
        return txtFecha.getText();
    }

    public void setFecha(String fecha) {
        txtFecha.setText(fecha);
    }

    public String getTipoReporte() {
        return cbTipoReporte.getSelectedItem().toString();
    }

    public void setTicketsVendidosText(String texto) {
        lblTicketsVendidos.setText(texto);
    }

    public void setTotalIngresosText(String texto) {
        lblTotalIngresos.setText(texto);
    }

    public javax.swing.JTable getTablaVentas() {
        return tblVentasCajeros;
    }

    public void addGenerarReporteListener(java.awt.event.ActionListener listener) {
        btnGenerarReporte.addActionListener(listener);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerarReporte;
    private javax.swing.JComboBox<String> cbTipoReporte;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTicketsVendidos;
    private javax.swing.JLabel lblTotalIngresos;
    private javax.swing.JTable tblVentasCajeros;
    private javax.swing.JTextField txtFecha;
    // End of variables declaration//GEN-END:variables
}