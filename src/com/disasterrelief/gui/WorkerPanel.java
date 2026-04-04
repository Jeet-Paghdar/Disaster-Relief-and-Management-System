package com.disasterrelief.gui;

import com.disasterrelief.dao.WorkerDAO;
import com.disasterrelief.models.SocialWorker;

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

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBorder(BorderFactory.createTitledBorder("Manage Workers"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        formContainer.add(new JLabel("Email:"), gbc);
        JTextField txtEmail = new JTextField(12);
        gbc.gridx = 3;
        formContainer.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formContainer.add(new JLabel("Specialty:"), gbc);
        JTextField txtSpecialty = new JTextField(12);
        gbc.gridx = 1;
        formContainer.add(txtSpecialty, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Shift:"), gbc);
        JComboBox<String> comboShift = new JComboBox<>(new String[]{"Day", "Afternoon", "Night"});
        gbc.gridx = 3;
        formContainer.add(comboShift, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formContainer.add(new JLabel("Gender:"), gbc);
        JComboBox<String> comboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        gbc.gridx = 1;
        formContainer.add(comboGender, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnAdd = new JButton("Add Worker");
        JButton btnDelete = new JButton("Delete Worker");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        formContainer.add(btnPanel, gbc);

        // Pushing everything left
        gbc.gridx = 4; gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formContainer.add(new JPanel(), gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "First Name", "Last Name", "Age", "Email", "Specialty", "Shift"};
        tableModel = new DefaultTableModel(cols, 0);
        workerTable = new JTable(tableModel);
        add(new JScrollPane(workerTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty() || 
                txtSpecialty.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all text fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                SocialWorker sw = new SocialWorker();
                sw.setFirstName(txtFirstName.getText().trim());
                sw.setLastName(txtLastName.getText().trim());
                
                if (!txtAge.getText().trim().isEmpty()) {
                    sw.setAge(Integer.parseInt(txtAge.getText().trim()));
                }
                sw.setEmail(txtEmail.getText().trim());
                
                sw.setSpecialisation(txtSpecialty.getText().trim());
                sw.setWorkShift((String) comboShift.getSelectedItem());
                
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
                tableModel.addRow(new Object[]{
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