package com.disasterrelief.gui;

import com.disasterrelief.dao.VictimDAO;
import com.disasterrelief.models.Victim;
import com.disasterrelief.utils.ValidationUtils;

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
    private List<Victim> cachedVictims;

    public VictimPanel() {
        victimDAO = new VictimDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // --- TOP: Input Form ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Registration & Life Safety"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;

        Dimension fieldSize = new Dimension(150, 30);

        // Row 0: Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("First Name:"), gbc);
        JTextField txtFirstName = new JTextField();
        txtFirstName.setPreferredSize(fieldSize);
        txtFirstName.setMinimumSize(fieldSize);
        txtFirstName.setMargin(new Insets(5, 8, 5, 8));
        gbc.gridx = 1; 
        formContainer.add(txtFirstName, gbc);
 
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Last Name:"), gbc);
        JTextField txtLastName = new JTextField();
        txtLastName.setPreferredSize(fieldSize);
        txtLastName.setMinimumSize(fieldSize);
        txtLastName.setMargin(new Insets(5, 8, 5, 8));
        gbc.gridx = 3; 
        formContainer.add(txtLastName, gbc);
        
        // Adaptive Pinning: Right Glue (1.0) absorbs all extra space
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);
 
        // Row 1: DOB & Gender
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("DOB (yyyy-mm-dd):"), gbc);
        JTextField txtDob = new JTextField();
        txtDob.setPreferredSize(fieldSize);
        txtDob.setMinimumSize(fieldSize);
        txtDob.setMargin(new Insets(5, 8, 5, 8));
        gbc.gridx = 1; 
        formContainer.add(txtDob, gbc);
 
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Gender:"), gbc);
        JComboBox<String> comboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        comboGender.setPreferredSize(fieldSize);
        comboGender.setMinimumSize(fieldSize);
        gbc.gridx = 3; 
        formContainer.add(comboGender, gbc);
 
        // Row 2: Phone & Disaster ID
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Phone:"), gbc);
        JTextField txtPhone = new JTextField();
        txtPhone.setPreferredSize(fieldSize);
        txtPhone.setMinimumSize(fieldSize);
        txtPhone.setMargin(new Insets(5, 8, 5, 8));
        gbc.gridx = 1; 
        formContainer.add(txtPhone, gbc);
 
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Disaster ID:"), gbc);
        JTextField txtDisasterId = new JTextField("1");
        txtDisasterId.setPreferredSize(fieldSize);
        txtDisasterId.setMinimumSize(fieldSize);
        txtDisasterId.setMargin(new Insets(5, 8, 5, 8));
        gbc.gridx = 3; 
        formContainer.add(txtDisasterId, gbc);
 
        // Row 3: Injury & Diet
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Injury Status:"), gbc);
        JComboBox<String> comboInjury = new JComboBox<>(new String[]{"None", "Minor", "Moderate", "Severe", "Critical"});
        comboInjury.setPreferredSize(fieldSize);
        comboInjury.setMinimumSize(fieldSize);
        gbc.gridx = 1; 
        formContainer.add(comboInjury, gbc);
 
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Dietary Need:"), gbc);
        JComboBox<String> comboDiet = new JComboBox<>(new String[]{"None", "Vegetarian", "Vegan", "Non-Vegetarian", "Gluten-Free"});
        comboDiet.setPreferredSize(fieldSize);
        comboDiet.setMinimumSize(fieldSize);
        gbc.gridx = 3; 
        formContainer.add(comboDiet, gbc);

        // Row 4: Blood Type
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Blood Type:"), gbc);
        JComboBox<String> comboBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"});
        comboBlood.setPreferredSize(fieldSize);
        comboBlood.setMinimumSize(fieldSize);
        gbc.gridx = 1; 
        formContainer.add(comboBlood, gbc);

        // Buttons Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Victim");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh Table");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // --- CENTER: Data Table ---
        String[] columns = {"ID", "First Name", "Last Name", "DOB", "Gender", "Blood", "Phone", "Injury", "Diet", "Disaster ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        victimTable = new JTable(tableModel);
        victimTable.setRowHeight(30);
        victimTable.setFillsViewportHeight(true);
        
        // Brighter Header
        victimTable.getTableHeader().setBackground(new Color(30, 48, 80));
        victimTable.getTableHeader().setForeground(Color.WHITE);
        victimTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(victimTable), BorderLayout.CENTER);

        // --- ACTIONS ---
        btnAdd.addActionListener(e -> {
            String fName = txtFirstName.getText().trim();
            String lName = txtLastName.getText().trim();
            String dobStr = txtDob.getText().trim();
            String phone = txtPhone.getText().trim();
            String dIdStr = txtDisasterId.getText().trim();

            if (!ValidationUtils.isValidName(fName)) {
                JOptionPane.showMessageDialog(this, "Invalid First Name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidName(lName)) {
                JOptionPane.showMessageDialog(this, "Invalid Last Name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate dobDate;
            try {
                dobDate = LocalDate.parse(dobStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid DOB. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(this, "Invalid Phone.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidNumber(dIdStr)) {
                JOptionPane.showMessageDialog(this, "Invalid Disaster ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Victim v = new Victim();
                v.setFirstName(fName);
                v.setLastName(lName);
                v.setDob(dobDate);
                v.setGender((String) comboGender.getSelectedItem());
                v.setPhoneNumber(phone);
                v.setDisasterId(Integer.parseInt(dIdStr));
                v.setInjuryStatus((String) comboInjury.getSelectedItem());
                v.setDietaryRestriction((String) comboDiet.getSelectedItem());
                v.setBloodType((String) comboBlood.getSelectedItem());
                v.setEntryDate(LocalDate.now());

                victimDAO.addVictim(v);
                JOptionPane.showMessageDialog(this, "Victim Added Successfully!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = victimTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a victim to update.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Victim", true);
            dialog.setSize(500, 450);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            Victim v = cachedVictims.get(row);

            JTextField updateFName = new JTextField(v.getFirstName());
            JTextField updateLName = new JTextField(v.getLastName());
            JTextField updateDob = new JTextField(v.getDob() != null ? v.getDob().toString() : "");
            JTextField updatePhone = new JTextField(v.getPhoneNumber());
            JTextField updateDId = new JTextField(String.valueOf(v.getDisasterId()));
            
            JComboBox<String> updateGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
            updateGender.setSelectedItem(v.getGender());
            
            JComboBox<String> updateInjury = new JComboBox<>(new String[]{"None", "Minor", "Moderate", "Severe", "Critical"});
            updateInjury.setSelectedItem(v.getInjuryStatus());
            
            JComboBox<String> updateDiet = new JComboBox<>(new String[]{"None", "Vegetarian", "Vegan", "Non-Vegetarian", "Gluten-Free"});
            updateDiet.setSelectedItem(v.getDietaryRestriction());
            
            JComboBox<String> updateBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"});
            updateBlood.setSelectedItem(v.getBloodType());

            form.add(new JLabel("First Name:")); form.add(updateFName);
            form.add(new JLabel("Last Name:")); form.add(updateLName);
            form.add(new JLabel("DOB (yyyy-mm-dd):")); form.add(updateDob);
            form.add(new JLabel("Phone:")); form.add(updatePhone);
            form.add(new JLabel("Disaster ID:")); form.add(updateDId);
            form.add(new JLabel("Gender:")); form.add(updateGender);
            form.add(new JLabel("Injury Status:")); form.add(updateInjury);
            form.add(new JLabel("Dietary Need:")); form.add(updateDiet);
            form.add(new JLabel("Blood Type:")); form.add(updateBlood);

            dialog.add(form, BorderLayout.CENTER);

            JPanel btnGrid = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            btnGrid.add(saveBtn);
            btnGrid.add(cancelBtn);
            dialog.add(btnGrid, BorderLayout.SOUTH);

            cancelBtn.addActionListener(ev -> dialog.dispose());

            saveBtn.addActionListener(ev -> {
                String fName = updateFName.getText().trim();
                String lName = updateLName.getText().trim();
                String dobStr = updateDob.getText().trim();
                String phone = updatePhone.getText().trim();
                String dIdStr = updateDId.getText().trim();

                if (!ValidationUtils.isValidName(fName) || !ValidationUtils.isValidName(lName)) {
                    JOptionPane.showMessageDialog(dialog, "Invalid Name (use letters only).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate dobDate;
                try {
                    dobDate = LocalDate.parse(dobStr);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid DOB format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!ValidationUtils.isValidPhone(phone)) {
                    JOptionPane.showMessageDialog(dialog, "Invalid Phone (must be exactly 10 digits).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!ValidationUtils.isValidNumber(dIdStr)) {
                    JOptionPane.showMessageDialog(dialog, "Invalid Disaster ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    v.setFirstName(fName);
                    v.setLastName(lName);
                    v.setDob(dobDate);
                    int age = java.time.Period.between(dobDate, LocalDate.now()).getYears();
                    v.setAge(age);
                    v.setPhoneNumber(phone);
                    v.setDisasterId(Integer.parseInt(dIdStr));
                    v.setInjuryStatus((String) updateInjury.getSelectedItem());
                    v.setDietaryRestriction((String) updateDiet.getSelectedItem());
                    v.setGender((String) updateGender.getSelectedItem());
                    v.setBloodType((String) updateBlood.getSelectedItem());

                    victimDAO.updateVictim(v);
                    JOptionPane.showMessageDialog(dialog, "Victim Updated Successfully (Age: " + age + ")");
                    dialog.dispose();
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error updating victim: " + ex.getMessage());
                }
            });

            dialog.setVisible(true);
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

        // Row Selection Listener
        victimTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = victimTable.getSelectedRow();
                if (row >= 0 && cachedVictims != null && row < cachedVictims.size()) {
                    Victim v = cachedVictims.get(row);
                    txtFirstName.setText(v.getFirstName());
                    txtLastName.setText(v.getLastName());
                    txtDob.setText(v.getDob() != null ? v.getDob().toString() : "");
                    txtPhone.setText(v.getPhoneNumber());
                    comboGender.setSelectedItem(v.getGender());
                    comboInjury.setSelectedItem(v.getInjuryStatus());
                    comboDiet.setSelectedItem(v.getDietaryRestriction());
                    comboBlood.setSelectedItem(v.getBloodType());
                    txtDisasterId.setText(String.valueOf(v.getDisasterId()));
                }
            }
        });

        // Initial Load
        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0); // clear
            cachedVictims = victimDAO.getAllVictims();
            for (Victim v : cachedVictims) {
                tableModel.addRow(new Object[]{
                        v.getPersonId(),
                        v.getFirstName(),
                        v.getLastName(),
                        v.getDob(),
                        v.getGender(),
                        v.getBloodType(),
                        v.getPhoneNumber(),
                        v.getInjuryStatus(),
                        v.getDietaryRestriction(),
                        v.getDisasterId()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load victim data.");
        }
    }
}