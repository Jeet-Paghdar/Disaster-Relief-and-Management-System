package com.disasterrelief.gui;

import com.disasterrelief.dao.MedicalDAO;
import com.disasterrelief.models.MedicalRecord;
import com.disasterrelief.utils.ValidationUtils;

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
        setBackground(Color.WHITE);

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Add Medical Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;

        // Adaptive Pinning: Right Glue (Column 4)
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);

        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtVictimId = new JTextField();
        txtVictimId.setPreferredSize(fieldSize);
        txtVictimId.setMinimumSize(fieldSize);
        txtVictimId.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtTreatments = new JTextField();
        txtTreatments.setPreferredSize(fieldSize);
        txtTreatments.setMinimumSize(fieldSize);
        txtTreatments.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtWorkerId = new JTextField();
        txtWorkerId.setPreferredSize(fieldSize);
        txtWorkerId.setMinimumSize(fieldSize);
        txtWorkerId.setMargin(new Insets(5, 8, 5, 8));

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Victim ID:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtVictimId, gbc);
        
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Treatments:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtTreatments, gbc);

        // Row 1
        gbc.gridy = 1;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Worker ID:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtWorkerId, gbc);

        // Row 2: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Record");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Record Number", "Victim ID", "Treatments", "Date", "Worker ID"};
        tableModel = new DefaultTableModel(cols, 0);
        medicalTable = new JTable(tableModel);
        medicalTable.setRowHeight(30);
        medicalTable.setFillsViewportHeight(true);
        
        // Brighter Header
        medicalTable.getTableHeader().setBackground(new Color(30, 48, 80));
        medicalTable.getTableHeader().setForeground(Color.WHITE);
        medicalTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String vIdStr = txtVictimId.getText().trim();
            String treats = txtTreatments.getText().trim();
            String wIdStr = txtWorkerId.getText().trim();

            if (!ValidationUtils.isValidNumber(vIdStr)) {
                JOptionPane.showMessageDialog(this, "Invalid Victim ID (must be a number).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isNotEmpty(treats)) {
                JOptionPane.showMessageDialog(this, "Invalid Treatments (cannot be empty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidNumber(wIdStr)) {
                JOptionPane.showMessageDialog(this, "Invalid Worker ID (must be a number).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                MedicalRecord record = new MedicalRecord(
                        0, Integer.parseInt(vIdStr), "Blood Type from Victim", "Prescription TBD", 
                        treats, LocalDate.now(), Integer.parseInt(wIdStr)
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
                        m.getRecordNumber(), m.getVictimId(),
                        m.getTreatmentDetails(), m.getTreatmentDate(), m.getWorkerId()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}