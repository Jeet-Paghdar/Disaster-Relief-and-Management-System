package com.disasterrelief.gui;

import com.disasterrelief.dao.MedicalDAO;
import com.disasterrelief.models.MedicalRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MedicalPanel extends JPanel {
    private JTable medicalTable;
    private DefaultTableModel tableModel;
    private MedicalDAO medicalDAO;

    public MedicalPanel() {
        medicalDAO = new MedicalDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form
        JPanel formContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formContainer.setBorder(BorderFactory.createTitledBorder("Add Medical Record"));

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 10));

        JTextField txtVictimId = new JTextField(12);
        JTextField txtBloodType = new JTextField(12);
        JTextField txtTreatments = new JTextField(12);
        JTextField txtWorkerId = new JTextField(12);

        formPanel.add(new JLabel("Victim ID:"));
        formPanel.add(txtVictimId);
        formPanel.add(new JLabel("Blood Type:"));
        formPanel.add(txtBloodType);
        formPanel.add(new JLabel("Treatments:"));
        formPanel.add(txtTreatments);
        formPanel.add(new JLabel("Worker ID:"));
        formPanel.add(txtWorkerId);

        JButton btnAdd = new JButton("Add Record");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        formPanel.add(btnAdd);
        formPanel.add(btnDelete);
        formPanel.add(btnRefresh);

        formContainer.add(formPanel);
        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Record Number", "Victim ID", "Blood Type", "Treatments", "Date", "Worker ID"};
        tableModel = new DefaultTableModel(cols, 0);
        medicalTable = new JTable(tableModel);
        add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            if (txtBloodType.getText().trim().isEmpty() || txtTreatments.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Blood Type and Treatments.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int victimId;
            int workerId = 0;
            try {
                victimId = Integer.parseInt(txtVictimId.getText().trim());
                if (!txtWorkerId.getText().trim().isEmpty()) {
                    workerId = Integer.parseInt(txtWorkerId.getText().trim());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Victim ID and Worker ID must be valid integers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                MedicalRecord record = new MedicalRecord(
                        0, victimId, txtBloodType.getText().trim(), "Prescription TBD", 
                        txtTreatments.getText().trim(), LocalDate.now(), workerId
                );

                medicalDAO.addMedicalRecord(record);
                JOptionPane.showMessageDialog(this, "Medical Record Added");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            int row = medicalTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a medical record to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this medical record?\nThis action cannot be undone.", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    medicalDAO.deleteMedicalRecord(id);
                    JOptionPane.showMessageDialog(this, "Medical Record deleted.");
                    loadTableData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnRefresh.addActionListener(e -> loadTableData());

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<MedicalRecord> records = medicalDAO.getAllMedicalRecords();
            for (MedicalRecord m : records) {
                tableModel.addRow(new Object[]{
                        m.getRecordNumber(), m.getVictimId(), m.getBloodType(), 
                        m.getTreatmentDetails(), m.getTreatmentDate(), m.getWorkerId()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}