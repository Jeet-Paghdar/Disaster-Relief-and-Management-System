package com.disasterrelief.gui;

import com.disasterrelief.dao.DisasterDAO;
import com.disasterrelief.models.Disaster;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DisasterPanel extends JPanel {
    private JTable disasterTable;
    private DefaultTableModel tableModel;
    private DisasterDAO disasterDAO;

    public DisasterPanel() {
        disasterDAO = new DisasterDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form
        JPanel formContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formContainer.setBorder(BorderFactory.createTitledBorder("Log New Disaster"));

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 10));

        JTextField txtType = new JTextField(12);
        JComboBox<String> comboAgency = new JComboBox<>(new String[]{
            "1 - NDMA", "2 - NDRF", "3 - NIDM"
        });
        JTextField txtRegions = new JTextField(12);
        JComboBox<String> comboSeverity = new JComboBox<>(new String[]{"High", "Moderate", "Low"});

        formPanel.add(new JLabel("Type (e.g. Earthquake):"));
        formPanel.add(txtType);
        formPanel.add(new JLabel("Severity:"));
        formPanel.add(comboSeverity);
        formPanel.add(new JLabel("Affected Regions:"));
        formPanel.add(txtRegions);
        formPanel.add(new JLabel("Control Agency:"));
        formPanel.add(comboAgency);

        JButton btnAdd = new JButton("Add Disaster");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        formPanel.add(btnAdd);
        formPanel.add(btnDelete);
        formPanel.add(btnRefresh);

        formContainer.add(formPanel);
        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Disaster ID", "Type", "Severity", "Affected Regions", "Handling Agency"};
        tableModel = new DefaultTableModel(cols, 0);
        disasterTable = new JTable(tableModel);
        add(new JScrollPane(disasterTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            if (txtType.getText().trim().isEmpty() || txtRegions.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Type and Regions.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Disaster d = new Disaster();
                d.setType(txtType.getText().trim());
                d.setSeverity((String) comboSeverity.getSelectedItem());
                d.setAffectedRegions(txtRegions.getText().trim());
                
                // Parse agency ID from the combo box choice (e.g., "1 - NDMA" -> 1)
                String selectedAgency = (String) comboAgency.getSelectedItem();
                int agencyId = 1;
                if (selectedAgency != null) {
                    agencyId = Integer.parseInt(selectedAgency.split(" - ")[0]);
                }
                d.setAgencyId(agencyId); 

                disasterDAO.addDisaster(d);
                JOptionPane.showMessageDialog(this, "Disaster Added! You can now link Victims to this new Disaster ID.");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            int row = disasterTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a disaster to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "CAUTION: Deleting this Disaster will also delete all linked records in:\n" +
                "- VICTIM\n" +
                "- MEDICAL_RECORD\n" +
                "- RELIEF_SERVICE\n" +
                "- FAMILY_RELATION\n" +
                "Are you sure you want to proceed?", "Confirm Cascaded Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    disasterDAO.deleteDisaster(id);
                    JOptionPane.showMessageDialog(this, "Disaster and all linked data deleted.");
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
            List<Disaster> disasters = disasterDAO.getAllDisasters();
            for (Disaster d : disasters) {
                tableModel.addRow(new Object[]{
                        d.getDisasterId(), d.getType(), d.getSeverity(), d.getAffectedRegions(), d.getAgencyName()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}