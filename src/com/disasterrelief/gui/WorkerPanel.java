package com.disasterrelief.gui;

import com.disasterrelief.dao.WorkerDAO;
import com.disasterrelief.models.SocialWorker;
import com.disasterrelief.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class WorkerPanel extends JPanel {
    private JTable workerTable;
    private DefaultTableModel tableModel;
    private WorkerDAO workerDAO;

    public WorkerPanel() {
        workerDAO = new WorkerDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Manage Workers"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;

        // Adaptive Pinning: Right Glue (Column 4)
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);

        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtFirstName = new JTextField();
        txtFirstName.setPreferredSize(fieldSize);
        txtFirstName.setMinimumSize(fieldSize);
        txtFirstName.setMargin(new Insets(5, 8, 5, 8));
        
        JTextField txtLastName = new JTextField();
        txtLastName.setPreferredSize(fieldSize);
        txtLastName.setMinimumSize(fieldSize);
        txtLastName.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtDob = new JTextField();
        txtDob.setPreferredSize(fieldSize);
        txtDob.setMinimumSize(fieldSize);
        txtDob.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtEmail = new JTextField();
        txtEmail.setPreferredSize(fieldSize);
        txtEmail.setMinimumSize(fieldSize);
        txtEmail.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtSpecialty = new JTextField();
        txtSpecialty.setPreferredSize(fieldSize);
        txtSpecialty.setMinimumSize(fieldSize);
        txtSpecialty.setMargin(new Insets(5, 8, 5, 8));

        // Row 0: Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtFirstName, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtLastName, gbc);

        // Row 1: DOB & Email
        gbc.gridy = 1;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("DOB (yyyy-mm-dd):"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtDob, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtEmail, gbc);

        // Row 2: Specialty & Shift
        gbc.gridy = 2;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Specialty:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtSpecialty, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Shift:"), gbc);
        JComboBox<String> comboShift = new JComboBox<>(new String[] { "Day", "Afternoon", "Night" });
        comboShift.setPreferredSize(fieldSize);
        comboShift.setMinimumSize(fieldSize);
        gbc.gridx = 3; 
        formContainer.add(comboShift, gbc);

        // Row 3: Gender
        gbc.gridy = 3;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Gender:"), gbc);
        JComboBox<String> comboGender = new JComboBox<>(new String[] { "Male", "Female", "Other" });
        comboGender.setPreferredSize(fieldSize);
        comboGender.setMinimumSize(fieldSize);
        gbc.gridx = 1; 
        formContainer.add(comboGender, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Worker");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Worker");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = { "ID", "First Name", "Last Name", "DOB", "Email", "Specialty", "Shift" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        workerTable = new JTable(tableModel);
        workerTable.setRowHeight(30);
        workerTable.setFillsViewportHeight(true);
        
        // Brighter Header
        workerTable.getTableHeader().setBackground(new Color(30, 48, 80));
        workerTable.getTableHeader().setForeground(Color.WHITE);
        workerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(workerTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String fName = txtFirstName.getText().trim();
            String lName = txtLastName.getText().trim();
            String dobStr = txtDob.getText().trim();
            String email = txtEmail.getText().trim();
            String specialty = txtSpecialty.getText().trim();

            if (!ValidationUtils.isValidName(fName)) {
                JOptionPane.showMessageDialog(this, "Invalid First Name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidName(lName)) {
                JOptionPane.showMessageDialog(this, "Invalid Last Name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LocalDate dobDate;
            try {
                dobDate = LocalDate.parse(dobStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid DOB format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid Email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isNotEmpty(specialty)) {
                JOptionPane.showMessageDialog(this, "Specialty cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                SocialWorker sw = new SocialWorker();
                sw.setFirstName(fName);
                sw.setLastName(lName);
                sw.setDob(dobDate);
                sw.setEmail(email);
                sw.setSpecialisation(specialty);
                sw.setWorkShift((String) comboShift.getSelectedItem());
                sw.setGender((String) comboGender.getSelectedItem());

                // Set Gender from UI
                sw.setGender((String) comboGender.getSelectedItem());
                sw.setPhoneNumber("");

                workerDAO.addWorker(sw);
                JOptionPane.showMessageDialog(this, "Worker Added");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = workerTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a worker to update.");
                return;
            }
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Worker", true);
            dialog.setSize(400, 400);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            try {
                List<SocialWorker> workers = workerDAO.getAllWorkers();
                SocialWorker sw = workers.get(row);

                JTextField updateFirstName = new JTextField(sw.getFirstName());
                JTextField updateLastName = new JTextField(sw.getLastName());
                JTextField updateDob = new JTextField(sw.getDob() != null ? sw.getDob().toString() : "");
                JTextField updateEmail = new JTextField(sw.getEmail());
                JTextField updateSpecialty = new JTextField(sw.getSpecialisation());
                
                JComboBox<String> updateShift = new JComboBox<>(new String[] { "Day", "Afternoon", "Night" });
                updateShift.setSelectedItem(sw.getWorkShift());
                
                JComboBox<String> updateGender = new JComboBox<>(new String[] { "Male", "Female", "Other" });
                updateGender.setSelectedItem(sw.getGender());

                form.add(new JLabel("First Name:")); form.add(updateFirstName);
                form.add(new JLabel("Last Name:")); form.add(updateLastName);
                form.add(new JLabel("DOB (yyyy-mm-dd):")); form.add(updateDob);
                form.add(new JLabel("Email:")); form.add(updateEmail);
                form.add(new JLabel("Specialty:")); form.add(updateSpecialty);
                form.add(new JLabel("Shift:")); form.add(updateShift);
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
                    String fName = updateFirstName.getText().trim();
                    String lName = updateLastName.getText().trim();
                    String dobStr = updateDob.getText().trim();
                    String email = updateEmail.getText().trim();
                    String specialty = updateSpecialty.getText().trim();

                    if (!ValidationUtils.isValidName(fName) || !ValidationUtils.isValidName(lName) ||
                        !ValidationUtils.isValidEmail(email) || !ValidationUtils.isNotEmpty(specialty)) {
                        JOptionPane.showMessageDialog(dialog, "Please fix validation errors before updating.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    LocalDate dobDate;
                    try {
                        dobDate = LocalDate.parse(dobStr);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Invalid DOB format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        sw.setFirstName(fName);
                        sw.setLastName(lName);
                        sw.setDob(dobDate);
                        int age = java.time.Period.between(dobDate, LocalDate.now()).getYears();
                        sw.setAge(age);
                        sw.setEmail(email);
                        sw.setSpecialisation(specialty);
                        sw.setWorkShift((String) updateShift.getSelectedItem());
                        sw.setGender((String) updateGender.getSelectedItem());

                        workerDAO.updateWorker(sw);
                        JOptionPane.showMessageDialog(dialog, "Worker Updated Successfully (Age: " + age + ")");
                        dialog.dispose();
                        loadTableData();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                    }
                });

                dialog.setVisible(true);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error fetching worker data.");
            }
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = workerTable.getSelectedRow();
            if (selectedRow >= 0) {
                int empId = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    workerDAO.deleteWorker(empId);
                    JOptionPane.showMessageDialog(this, "Worker Deleted");
                    loadTableData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a worker to delete.");
            }
        });

        // Row Selection Listener
        workerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = workerTable.getSelectedRow();
                if (row >= 0) {
                    txtFirstName.setText(tableModel.getValueAt(row, 1).toString());
                    txtLastName.setText(tableModel.getValueAt(row, 2).toString());
                    txtDob.setText(tableModel.getValueAt(row, 3).toString());
                    txtEmail.setText(tableModel.getValueAt(row, 4).toString());
                    txtSpecialty.setText(tableModel.getValueAt(row, 5).toString());
                    comboShift.setSelectedItem(tableModel.getValueAt(row, 6).toString());
                }
            }
        });

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<SocialWorker> workers = workerDAO.getAllWorkers();
            for (SocialWorker w : workers) {
                tableModel.addRow(new Object[] {
                        w.getEmployeeId(), w.getFirstName(), w.getLastName(),
                        w.getDob(), w.getEmail(),
                        w.getSpecialisation(), w.getWorkShift()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}