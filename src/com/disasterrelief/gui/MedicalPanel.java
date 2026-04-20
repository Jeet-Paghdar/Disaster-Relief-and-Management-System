package com.disasterrelief.gui;

import com.disasterrelief.dao.MedicalDAO;
import com.disasterrelief.dao.VictimDAO;
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
    private VictimDAO victimDAO;

    public MedicalPanel() {
        medicalDAO = new MedicalDAO();
        victimDAO = new VictimDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Medical Record Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtVictimId = new JTextField();
        txtVictimId.setPreferredSize(fieldSize);
        JComboBox<String> comboBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"});
        comboBlood.setPreferredSize(fieldSize);
        JTextField txtPrescriptions = new JTextField();
        txtPrescriptions.setPreferredSize(fieldSize);
        JTextField txtTreatments = new JTextField();
        txtTreatments.setPreferredSize(fieldSize);
        JTextField txtWorkerId = new JTextField();
        txtWorkerId.setPreferredSize(fieldSize);
        JTextField txtDate = new JTextField();
        txtDate.setPreferredSize(fieldSize);

        // Auto-fetch Blood Type when Victim ID is entered
        txtVictimId.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                String idStr = txtVictimId.getText().trim();
                if (ValidationUtils.isValidNumber(idStr)) {
                    try {
                        String blood = victimDAO.getBloodType(Integer.parseInt(idStr));
                        if (blood != null) {
                            comboBlood.setSelectedItem(blood);
                        } else {
                            JOptionPane.showMessageDialog(MedicalPanel.this, "Victim ID not found in system.", "System Warning", JOptionPane.WARNING_MESSAGE);
                            comboBlood.setSelectedIndex(-1);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Row 0: Victim ID & Blood Type
        gbc.gridx = 0; gbc.gridy = 0;
        formContainer.add(new JLabel("Victim ID:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtVictimId, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Blood Type:"), gbc);
        gbc.gridx = 3; 
        comboBlood.setEnabled(false); // Make it read-only since it's fetched from the Victim table
        formContainer.add(comboBlood, gbc);

        // Row 1: Prescriptions & Treatments
        gbc.gridy = 1;
        gbc.gridx = 0;
        formContainer.add(new JLabel("Prescriptions:"), gbc);
        gbc.gridx = 1;
        formContainer.add(txtPrescriptions, gbc);
        gbc.gridx = 2;
        formContainer.add(new JLabel("Treatments:"), gbc);
        gbc.gridx = 3;
        formContainer.add(txtTreatments, gbc);

        // Row 2: Worker ID & Date
        gbc.gridy = 2;
        gbc.gridx = 0;
        formContainer.add(new JLabel("Worker ID:"), gbc);
        gbc.gridx = 1;
        formContainer.add(txtWorkerId, gbc);
        gbc.gridx = 2;
        formContainer.add(new JLabel("Date (yyyy-mm-dd):"), gbc);
        gbc.gridx = 3;
        formContainer.add(txtDate, gbc);

        // Row 3: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Record");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Record #", "Victim ID", "Age", "Blood", "Prescriptions", "Treatments", "Date", "Worker ID"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        medicalTable = new JTable(tableModel);
        medicalTable.setRowHeight(30);
        medicalTable.setFillsViewportHeight(true);
        
        medicalTable.getTableHeader().setBackground(new Color(30, 48, 80));
        medicalTable.getTableHeader().setForeground(Color.WHITE);
        medicalTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String vIdStr = txtVictimId.getText().trim();
            String blood = (String) comboBlood.getSelectedItem();
            String pres = txtPrescriptions.getText().trim();
            String treats = txtTreatments.getText().trim();
            String wIdStr = txtWorkerId.getText().trim();
            String dateStr = txtDate.getText().trim();

            if (!ValidationUtils.isValidNumber(vIdStr) || !ValidationUtils.isNotEmpty(blood) || 
                !ValidationUtils.isNotEmpty(treats) || !ValidationUtils.isValidNumber(wIdStr)) {
                JOptionPane.showMessageDialog(this, "Please check Victim ID, Blood Type, Treatments, and Worker ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate treatmentDate;
            try {
                treatmentDate = LocalDate.parse(dateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                MedicalRecord record = new MedicalRecord(
                        0, Integer.parseInt(vIdStr), blood, pres, 
                        treats, treatmentDate, Integer.parseInt(wIdStr)
                );
                medicalDAO.addMedicalRecord(record);
                JOptionPane.showMessageDialog(this, "Medical Record Added Successfully!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = medicalTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a record to update.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Medical Record", true);
            dialog.setSize(400, 250);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JComboBox<String> updateBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"});
            updateBlood.setSelectedItem(tableModel.getValueAt(row, 3).toString());
            JTextField updatePres = new JTextField(tableModel.getValueAt(row, 4).toString());
            JTextField updateTreats = new JTextField(tableModel.getValueAt(row, 5).toString());
            String existingDate = tableModel.getValueAt(row, 6) != null ? tableModel.getValueAt(row, 6).toString() : "";
            JTextField updateDate = new JTextField(existingDate);

            form.add(new JLabel("Blood Type:")); form.add(updateBlood);
            form.add(new JLabel("Prescriptions:")); form.add(updatePres);
            form.add(new JLabel("Treatments:")); form.add(updateTreats);
            form.add(new JLabel("Date (yyyy-mm-dd):")); form.add(updateDate);

            dialog.add(form, BorderLayout.CENTER);

            JPanel btnGrid = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            btnGrid.add(saveBtn);
            btnGrid.add(cancelBtn);
            dialog.add(btnGrid, BorderLayout.SOUTH);

            cancelBtn.addActionListener(ev -> dialog.dispose());

            saveBtn.addActionListener(ev -> {
                String blood = (String) updateBlood.getSelectedItem();
                String pres = updatePres.getText().trim();
                String treats = updateTreats.getText().trim();
                String dateStr = updateDate.getText().trim();

                if (!ValidationUtils.isNotEmpty(blood) || !ValidationUtils.isNotEmpty(treats)) {
                    JOptionPane.showMessageDialog(dialog, "Blood Type and Treatments are required for update.");
                    return;
                }
                
                LocalDate treatmentDate;
                try {
                    treatmentDate = LocalDate.parse(dateStr);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid Date format. Use YYYY-MM-DD.");
                    return;
                }

                try {
                    int recordId = (int) tableModel.getValueAt(row, 0);
                    MedicalRecord record = new MedicalRecord();
                    record.setRecordNumber(recordId);
                    record.setVictimId((int) tableModel.getValueAt(row, 1));
                    record.setBloodType(blood);
                    record.setPrescriptions(pres);
                    record.setTreatmentDetails(treats);
                    record.setTreatmentDate(treatmentDate);

                    medicalDAO.updateMedicalRecord(record);
                    JOptionPane.showMessageDialog(dialog, "Medical Record Updated Successfully!");
                    dialog.dispose();
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            dialog.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int row = medicalTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a record to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this record?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    medicalDAO.deleteMedicalRecord(id);
                    loadTableData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnRefresh.addActionListener(e -> loadTableData());

        medicalTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = medicalTable.getSelectedRow();
                if (row >= 0) {
                    txtVictimId.setText(tableModel.getValueAt(row, 1).toString());
                    comboBlood.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                    txtPrescriptions.setText(tableModel.getValueAt(row, 4).toString());
                    txtTreatments.setText(tableModel.getValueAt(row, 5).toString());
                    txtDate.setText(tableModel.getValueAt(row, 6) != null ? tableModel.getValueAt(row, 6).toString() : "");
                    txtWorkerId.setText(tableModel.getValueAt(row, 7).toString());
                }
            }
        });

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<MedicalRecord> records = medicalDAO.getAllMedicalRecords();
            for (MedicalRecord m : records) {
                tableModel.addRow(new Object[]{
                        m.getRecordNumber(), m.getVictimId(), m.getAge(), m.getBloodType(),
                        m.getPrescriptions(), m.getTreatmentDetails(), m.getTreatmentDate(), m.getWorkerId()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}