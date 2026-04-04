package com.disasterrelief.gui;

import com.disasterrelief.dao.VictimDAO;
import com.disasterrelief.models.Victim;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VictimPanel extends JPanel {
    private JTable victimTable;
    private DefaultTableModel tableModel;
    private VictimDAO victimDAO;

    public VictimPanel() {
        victimDAO = new VictimDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOP: Input Form ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBorder(BorderFactory.createTitledBorder("Registration & Life Safety"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Section A: Personal
        gbc.gridx = 0; gbc.gridy = 0;
        formContainer.add(new JLabel("First Name:"), gbc);
        JTextField txtFirstName = new JTextField(12);
        gbc.gridx = 1;
        formContainer.add(txtFirstName, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Last Name:"), gbc);
        JTextField txtLastName = new JTextField(12);
        gbc.gridx = 3;
        formContainer.add(txtLastName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formContainer.add(new JLabel("Age:"), gbc);
        JTextField txtAge = new JTextField(12);
        gbc.gridx = 1;
        formContainer.add(txtAge, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Gender:"), gbc);
        JComboBox<String> comboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        gbc.gridx = 3;
        formContainer.add(comboGender, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formContainer.add(new JLabel("Phone:"), gbc);
        JTextField txtPhone = new JTextField(12);
        gbc.gridx = 1;
        formContainer.add(txtPhone, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Disaster ID:"), gbc);
        JTextField txtDisasterId = new JTextField("1", 12);
        gbc.gridx = 3;
        formContainer.add(txtDisasterId, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formContainer.add(new JLabel("Injury:"), gbc);
        JComboBox<String> comboInjury = new JComboBox<>(new String[]{"None", "Minor", "Moderate", "Severe", "Critical"});
        gbc.gridx = 1;
        formContainer.add(comboInjury, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Diet:"), gbc);
        JComboBox<String> comboDiet = new JComboBox<>(new String[]{"None", "Vegetarian", "Vegan", "Non-Vegetarian", "Gluten-Free"});
        gbc.gridx = 3;
        formContainer.add(comboDiet, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnAdd = new JButton("Add Victim");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh Table");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        formContainer.add(btnPanel, gbc);

        // Pushing everything to the left
        gbc.gridx = 4; gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formContainer.add(new JPanel(), gbc);

        add(formContainer, BorderLayout.NORTH);

        // --- CENTER: Data Table ---
        String[] columns = {"ID", "First Name", "Last Name", "Age", "Gender", "Injury", "Disaster ID"};
        tableModel = new DefaultTableModel(columns, 0);
        victimTable = new JTable(tableModel);
        add(new JScrollPane(victimTable), BorderLayout.CENTER);

        // --- ACTIONS ---
        btnAdd.addActionListener(e -> {
            if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in First and Last Name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int disasterId, age = 0;
            try {
                disasterId = Integer.parseInt(txtDisasterId.getText().trim());
                if (txtAge.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Age is mandatory.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                age = Integer.parseInt(txtAge.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Disaster ID and Age must be valid numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                Victim v = new Victim();
                v.setFirstName(txtFirstName.getText().trim());
                v.setLastName(txtLastName.getText().trim());
                v.setAge(age);
                v.setInjuryStatus((String) comboInjury.getSelectedItem());
                v.setPhoneNumber(txtPhone.getText().trim());
                v.setEntryDate(LocalDate.now());
                v.setDisasterId(disasterId);
                v.setDietaryRestriction((String) comboDiet.getSelectedItem());
                
                v.setGender((String) comboGender.getSelectedItem());
                v.setAddressBefore("Unknown");
                v.setAddressAfter("Relief Camp");

                victimDAO.addVictim(v);
                JOptionPane.showMessageDialog(this, "Victim added successfully!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding victim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = victimTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a victim to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "CAUTION: Deleting this Victim will also delete all linked records in:\n" +
                "- MEDICAL_RECORD\n" +
                "- FAMILY_RELATION\n" +
                "- VICTIM_DIETARY_RESTRICTIONS\n" +
                "- VICTIM_SUPPLY\n" +
                "- RELIEF_SERVICE (Matches)\n" +
                "Are you sure you want to proceed?", "Confirm Cascaded Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    victimDAO.deleteVictim(id);
                    JOptionPane.showMessageDialog(this, "Victim and all personal records deleted.");
                    loadTableData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnRefresh.addActionListener(e -> loadTableData());

        // Initial Load
        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0); // clear
            List<Victim> victims = victimDAO.getAllVictims();
            for (Victim v : victims) {
                tableModel.addRow(new Object[]{
                        v.getPersonId(),
                        v.getFirstName(),
                        v.getLastName(),
                        v.getAge(),
                        v.getGender(),
                        v.getInjuryStatus(),
                        v.getDisasterId()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load victim data.");
        }
    }
}