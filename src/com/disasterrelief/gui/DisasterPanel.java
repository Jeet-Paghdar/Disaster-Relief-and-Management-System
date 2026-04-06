package com.disasterrelief.gui;

import com.disasterrelief.dao.DisasterDAO;
import com.disasterrelief.models.Disaster;
import com.disasterrelief.utils.ValidationUtils;

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
        setBackground(Color.WHITE);

        // --- TOP: Input Form ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Log New Disaster"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;

        // Adaptive Pinning: Right Glue (Column 4)
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);

        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtType = new JTextField();
        txtType.setPreferredSize(fieldSize);
        txtType.setMinimumSize(fieldSize);
        txtType.setMargin(new Insets(5, 8, 5, 8));

        JComboBox<String> comboAgency = new JComboBox<>(new String[]{"1 - NDMA", "2 - NDRF", "3 - NIDM"});
        comboAgency.setPreferredSize(fieldSize);
        comboAgency.setMinimumSize(fieldSize);

        JTextField txtRegions = new JTextField();
        txtRegions.setPreferredSize(fieldSize);
        txtRegions.setMinimumSize(fieldSize);
        txtRegions.setMargin(new Insets(5, 8, 5, 8));

        JComboBox<String> comboSeverity = new JComboBox<>(new String[]{"High", "Moderate", "Low"});
        comboSeverity.setPreferredSize(fieldSize);
        comboSeverity.setMinimumSize(fieldSize);

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Type (e.g. Earthquake):"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtType, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Severity:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(comboSeverity, gbc);

        // Row 1
        gbc.gridy = 1;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Affected Regions:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtRegions, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Control Agency:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(comboAgency, gbc);

        // Row 2: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Disaster");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Disaster ID", "Type", "Severity", "Affected Regions", "Handling Agency"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        disasterTable = new JTable(tableModel);
        disasterTable.setRowHeight(30);
        disasterTable.setFillsViewportHeight(true);
        
        // Brighter Header
        disasterTable.getTableHeader().setBackground(new Color(30, 48, 80));
        disasterTable.getTableHeader().setForeground(Color.WHITE);
        disasterTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(disasterTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String type = txtType.getText().trim();
            String regions = txtRegions.getText().trim();

            if (!ValidationUtils.isNotEmpty(type) || !ValidationUtils.isNotEmpty(regions)) {
                JOptionPane.showMessageDialog(this, "Please fix validation errors.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Disaster d = new Disaster();
                d.setType(type);
                d.setSeverity((String) comboSeverity.getSelectedItem());
                d.setAffectedRegions(regions);
                
                String selectedAgency = (String) comboAgency.getSelectedItem();
                int agencyId = 1;
                if (selectedAgency != null) {
                    agencyId = Integer.parseInt(selectedAgency.split(" - ")[0]);
                }
                d.setAgencyId(agencyId); 

                disasterDAO.addDisaster(d);
                JOptionPane.showMessageDialog(this, "Disaster Added Successfully!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = disasterTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a disaster to update.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Disaster", true);
            dialog.setSize(400, 300);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JTextField updateType = new JTextField(tableModel.getValueAt(row, 1).toString());
            JComboBox<String> updateSeverity = new JComboBox<>(new String[]{"High", "Moderate", "Low"});
            updateSeverity.setSelectedItem(tableModel.getValueAt(row, 2).toString());
            JTextField updateRegions = new JTextField(tableModel.getValueAt(row, 3).toString());
            JComboBox<String> updateAgency = new JComboBox<>(new String[]{"1 - NDMA", "2 - NDRF", "3 - NIDM"});
            
            String currentAgencyName = tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "";
            for (int i = 0; i < updateAgency.getItemCount(); i++) {
                if (updateAgency.getItemAt(i).contains(currentAgencyName)) {
                    updateAgency.setSelectedIndex(i);
                    break;
                }
            }

            form.add(new JLabel("Type:")); form.add(updateType);
            form.add(new JLabel("Severity:")); form.add(updateSeverity);
            form.add(new JLabel("Affected Regions:")); form.add(updateRegions);
            form.add(new JLabel("Control Agency:")); form.add(updateAgency);

            dialog.add(form, BorderLayout.CENTER);

            JPanel btnGrid = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            btnGrid.add(saveBtn);
            btnGrid.add(cancelBtn);
            dialog.add(btnGrid, BorderLayout.SOUTH);

            cancelBtn.addActionListener(ev -> dialog.dispose());

            saveBtn.addActionListener(ev -> {
                String type = updateType.getText().trim();
                String regions = updateRegions.getText().trim();

                if (!ValidationUtils.isNotEmpty(type) || !ValidationUtils.isNotEmpty(regions)) {
                    JOptionPane.showMessageDialog(dialog, "Please fix validation errors before updating.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int id = (int) tableModel.getValueAt(row, 0);
                    Disaster d = new Disaster();
                    d.setDisasterId(id);
                    d.setType(type);
                    d.setSeverity((String) updateSeverity.getSelectedItem());
                    d.setAffectedRegions(regions);
                    
                    String selectedAgency = (String) updateAgency.getSelectedItem();
                    if (selectedAgency != null) {
                        d.setAgencyId(Integer.parseInt(selectedAgency.split(" - ")[0]));
                    }

                    disasterDAO.updateDisaster(d);
                    JOptionPane.showMessageDialog(dialog, "Disaster Updated Successfully!");
                    dialog.dispose();
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            dialog.setVisible(true);
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

        // Row Selection Listener
        disasterTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = disasterTable.getSelectedRow();
                if (row >= 0) {
                    txtType.setText(tableModel.getValueAt(row, 1).toString());
                    comboSeverity.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    txtRegions.setText(tableModel.getValueAt(row, 3).toString());
                    
                    // Note: Agency matching might be tricky if names change, but ID is usually stable.
                    // For now, we'll just leave it or match by name if possible.
                }
            }
        });

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