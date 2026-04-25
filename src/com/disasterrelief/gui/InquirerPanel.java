package com.disasterrelief.gui;

import com.disasterrelief.dao.InquirerDAO;
import com.disasterrelief.utils.ValidationUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class InquirerPanel extends JPanel {

    private InquirerDAO inquirerDAO;
    private JTable inquirerTable;
    private javax.swing.table.DefaultTableModel tableModel;

    public InquirerPanel() {
        inquirerDAO = new InquirerDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // --- TOP: Input Search & Update Form ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Inquirer & Match Management"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        // Help label
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        gbc.insets = new Insets(5, 10, 5, 10);
        // Removed outdated manual update note

        Dimension fieldSize = new Dimension(150, 30);

        // Inquirer Fields
        JTextField txtInquirerFirst = new JTextField();
        txtInquirerFirst.setPreferredSize(fieldSize);
        JTextField txtInquirerLast = new JTextField();
        txtInquirerLast.setPreferredSize(fieldSize);
        JTextField txtInquirerPhone = new JTextField();
        txtInquirerPhone.setPreferredSize(fieldSize);
        JComboBox<String> comboInquirerGender = new JComboBox<>(new String[]{"Male", "Female", "Other", "Unknown"});
        comboInquirerGender.setPreferredSize(fieldSize);
        JTextField txtInquirerDob = new JTextField();
        txtInquirerDob.setPreferredSize(fieldSize);
        txtInquirerDob.setToolTipText("YYYY-MM-DD");

        // Victim Fields
        JTextField txtVictimFirst = new JTextField();
        txtVictimFirst.setPreferredSize(fieldSize);
        JTextField txtVictimLast = new JTextField();
        txtVictimLast.setPreferredSize(fieldSize);
        JTextField txtVictimPhone = new JTextField();
        txtVictimPhone.setPreferredSize(fieldSize);
        JComboBox<String> comboRelation = new JComboBox<>(new String[]{"Parent", "Child", "Sibling", "Spouse", "Extended Family"});
        comboRelation.setPreferredSize(fieldSize);

        // Row 1: Inquirer Name
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0; gbc.gridy = 1;
        formContainer.add(new JLabel("First Name (Inquirer):"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtInquirerFirst, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Last Name (Inquirer):"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtInquirerLast, gbc);

        // Row 2: Inquirer Meta
        gbc.gridy = 2;
        gbc.gridx = 0;
        formContainer.add(new JLabel("Phone (Inquirer):"), gbc);
        gbc.gridx = 1;
        formContainer.add(txtInquirerPhone, gbc);
        gbc.gridx = 2;
        formContainer.add(new JLabel("Gender (Inquirer):"), gbc);
        gbc.gridx = 3;
        formContainer.add(comboInquirerGender, gbc);

        // Row 3: Inquirer DOB
        gbc.gridy = 3;
        gbc.gridx = 0;
        formContainer.add(new JLabel("DOB (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        formContainer.add(txtInquirerDob, gbc);

        // Separator logic
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        formContainer.add(sep, gbc);

        // Row 5: Victim Name
        gbc.fill = GridBagConstraints.NONE; gbc.gridwidth = 1;
        gbc.gridy = 5;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Victim First Name:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtVictimFirst, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Victim Last Name:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtVictimLast, gbc);

        // Row 6: Victim Details
        gbc.gridy = 6;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Victim Phone:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtVictimPhone, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Inquirer's Relation:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(comboRelation, gbc);

        // Row 7: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnSearch = new JButton("Search & Match");
        btnSearch.setBackground(new Color(30, 48, 80));
        btnSearch.setForeground(Color.WHITE);
        
        JButton btnUpdateInquirer = new JButton("Update Inquirer Profile");
        JButton btnUpdateMatch = new JButton("Update Match Info");
        JButton btnDeleteInquirer = new JButton("Delete Inquirer");
        
        btnPanel.add(btnSearch);
        btnPanel.add(btnUpdateInquirer);
        btnPanel.add(btnUpdateMatch);
        btnPanel.add(btnDeleteInquirer);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 4;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // --- CENTER: Data Table ---
        String[] columns = {"ID", "First Name", "Last Name", "Phone", "Gender", "Inquiry Date"};
        tableModel = new javax.swing.table.DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inquirerTable = new JTable(tableModel);
        inquirerTable.setRowHeight(30);
        inquirerTable.setFillsViewportHeight(true);
        inquirerTable.getTableHeader().setBackground(new Color(30, 48, 80));
        inquirerTable.getTableHeader().setForeground(Color.WHITE);
        inquirerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(inquirerTable), BorderLayout.CENTER);

        // Row Selection Listener
        inquirerTable.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int row = inquirerTable.getSelectedRow();
                if (row >= 0) {
                    txtInquirerFirst.setText(tableModel.getValueAt(row, 1).toString());
                    txtInquirerLast.setText(tableModel.getValueAt(row, 2).toString());
                    Object phoneObj = tableModel.getValueAt(row, 3);
                    txtInquirerPhone.setText(phoneObj != null ? phoneObj.toString() : "");
                    Object genderObj = tableModel.getValueAt(row, 4);
                    if (genderObj != null) comboInquirerGender.setSelectedItem(genderObj.toString());
                }
            }
        });

        // --- ACTIONS ---

        btnSearch.addActionListener(e -> {
            String iFirst = txtInquirerFirst.getText().trim();
            String iLast = txtInquirerLast.getText().trim();
            String iPhone = txtInquirerPhone.getText().trim();
            String iGender = (String) comboInquirerGender.getSelectedItem();
            String iDobStr = txtInquirerDob.getText().trim();
            String vFirst = txtVictimFirst.getText().trim();
            String vLast = txtVictimLast.getText().trim();
            String vPhone = txtVictimPhone.getText().trim();

            if (!ValidationUtils.isValidName(iFirst) || !ValidationUtils.isValidName(iLast) || 
                !ValidationUtils.isValidPhone(iPhone) || // Validating Inquirer Phone
                !ValidationUtils.isValidName(vFirst) || !ValidationUtils.isValidName(vLast) || 
                !ValidationUtils.isValidPhone(vPhone)) {
                JOptionPane.showMessageDialog(this, "Please fix validation errors (ensure phone numbers are 10 digits).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.sql.Date iDob = null;
            if (!iDobStr.isEmpty()) {
                try {
                    iDob = java.sql.Date.valueOf(iDobStr);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid DOB format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try {
                boolean matched = inquirerDAO.findAndMatchVictim(
                        iFirst, iLast, iPhone, iGender, iDob,
                        vFirst, vLast, vPhone,
                        (String) comboRelation.getSelectedItem()
                );
                if (matched) {
                    JOptionPane.showMessageDialog(this, "MATCHED! Link recorded successfully.");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "Victim not found with those details.", "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdateInquirer.addActionListener(e -> {
            int row = inquirerTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an Inquirer from the table to update.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Inquirer Profile", true);
            dialog.setSize(400, 300);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            String oldFirst = tableModel.getValueAt(row, 1).toString();
            String oldLast = tableModel.getValueAt(row, 2).toString();
            
            JTextField updateFirst = new JTextField(oldFirst);
            JTextField updateLast = new JTextField(oldLast);
            
            Object phoneObj = tableModel.getValueAt(row, 3);
            JTextField updatePhone = new JTextField(phoneObj != null ? phoneObj.toString() : "");
            
            JComboBox<String> updateGender = new JComboBox<>(new String[]{"Male", "Female", "Other", "Unknown"});
            Object genderObj = tableModel.getValueAt(row, 4);
            if (genderObj != null) updateGender.setSelectedItem(genderObj.toString());

            form.add(new JLabel("First Name:")); form.add(updateFirst);
            form.add(new JLabel("Last Name:")); form.add(updateLast);
            form.add(new JLabel("Phone:")); form.add(updatePhone);
            form.add(new JLabel("Gender:")); form.add(updateGender);

            dialog.add(form, BorderLayout.CENTER);

            JPanel btnGrid = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            btnGrid.add(saveBtn);
            btnGrid.add(cancelBtn);
            dialog.add(btnGrid, BorderLayout.SOUTH);

            cancelBtn.addActionListener(ev -> dialog.dispose());

            saveBtn.addActionListener(ev -> {
                String newFirst = updateFirst.getText().trim();
                String newLast = updateLast.getText().trim();
                String phone = updatePhone.getText().trim();
                String gender = (String) updateGender.getSelectedItem();

                if (newFirst.isEmpty() || newLast.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "First Name and Last Name are required.");
                    return;
                }

                try {
                    int inquirerId = (int) tableModel.getValueAt(row, 0);
                    inquirerDAO.updateInquirerById(inquirerId, newFirst, newLast, phone, gender);
                    JOptionPane.showMessageDialog(dialog, "Inquirer Profile Updated Successfully!");
                    dialog.dispose();
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            dialog.setVisible(true);
        });

        btnUpdateMatch.addActionListener(e -> {
            String iFirst = txtInquirerFirst.getText().trim();
            String iLast = txtInquirerLast.getText().trim();
            String vFirst = txtVictimFirst.getText().trim();
            String vLast = txtVictimLast.getText().trim();
            String newInfo = "Matched relation: " + comboRelation.getSelectedItem();

            if (iFirst.isEmpty() || iLast.isEmpty() || vFirst.isEmpty() || vLast.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inquirer and Victim names required for match update.");
                return;
            }

            try {
                inquirerDAO.updateMatch(iFirst, iLast, vFirst, vLast, newInfo);
                JOptionPane.showMessageDialog(this, "Match Information Updated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnDeleteInquirer.addActionListener(e -> {
            int row = inquirerTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an Inquirer from the table to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this Inquirer?\nThis will also remove all their matching registry records.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int inquirerId = (int) tableModel.getValueAt(row, 0);
                    inquirerDAO.deleteInquirer(inquirerId);
                    JOptionPane.showMessageDialog(this, "Inquirer deleted successfully.");
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            java.util.List<com.disasterrelief.models.Inquirer> inquirers = inquirerDAO.getAllInquirers();
            for (com.disasterrelief.models.Inquirer inq : inquirers) {
                tableModel.addRow(new Object[]{
                        inq.getInquirerId(),
                        inq.getFirstName(),
                        inq.getLastName(),
                        inq.getPhoneNumber(),
                        inq.getGender(),
                        inq.getInquiryDate()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load inquirer data.");
        }
    }
}