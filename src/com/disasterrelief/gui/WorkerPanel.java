package com.disasterrelief.gui;

import com.disasterrelief.dao.WorkerDAO;
import com.disasterrelief.models.SocialWorker;
import com.disasterrelief.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
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

        JTextField txtAge = new JTextField();
        txtAge.setPreferredSize(fieldSize);
        txtAge.setMinimumSize(fieldSize);
        txtAge.setMargin(new Insets(5, 8, 5, 8));

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

        // Row 1: Age & Email
        gbc.gridy = 1;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtAge, gbc);
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
        JButton btnDelete = new JButton("Delete Worker");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = { "ID", "First Name", "Last Name", "Age", "Email", "Specialty", "Shift" };
        tableModel = new DefaultTableModel(cols, 0);
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
            String ageStr = txtAge.getText().trim();
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
            if (!ValidationUtils.isValidNumber(ageStr)) {
                JOptionPane.showMessageDialog(this, "Invalid Age.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
                sw.setAge(Integer.parseInt(ageStr));
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

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<SocialWorker> workers = workerDAO.getAllWorkers();
            for (SocialWorker w : workers) {
                tableModel.addRow(new Object[] {
                        w.getEmployeeId(), w.getFirstName(), w.getLastName(),
                        w.getAge(), w.getEmail(),
                        w.getSpecialisation(), w.getWorkShift()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}