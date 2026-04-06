package com.disasterrelief.gui;

import com.disasterrelief.dao.LocationDAO;
import com.disasterrelief.models.Location;
import com.disasterrelief.utils.ValidationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LocationPanel extends JPanel {
    private JTable locationTable;
    private DefaultTableModel tableModel;
    private LocationDAO locationDAO;

    public LocationPanel() {
        locationDAO = new LocationDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Manage Shelters & Locations"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;
 
        // Adaptive Pinning: Right Glue (Column 4)
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);
 
        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtName = new JTextField();
        txtName.setPreferredSize(fieldSize);
        txtName.setMinimumSize(fieldSize);
        txtName.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtAddr = new JTextField();
        txtAddr.setPreferredSize(fieldSize);
        txtAddr.setMinimumSize(fieldSize);
        txtAddr.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtPincode = new JTextField();
        txtPincode.setPreferredSize(fieldSize);
        txtPincode.setMinimumSize(fieldSize);
        txtPincode.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtCapacity = new JTextField();
        txtCapacity.setPreferredSize(fieldSize);
        txtCapacity.setMinimumSize(fieldSize);
        txtCapacity.setMargin(new Insets(5, 8, 5, 8));

        JComboBox<String> comboType = new JComboBox<>(new String[]{"Shelter", "Hospital", "Warehouse"});
        comboType.setPreferredSize(fieldSize);
        comboType.setMinimumSize(fieldSize);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtName, gbc);
  
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Address:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtAddr, gbc);
 
        gbc.gridx = 0; gbc.gridy = 1; 
        formContainer.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(comboType, gbc);
  
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Pincode:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtPincode, gbc);
 
        gbc.gridx = 0; gbc.gridy = 2; 
        formContainer.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtCapacity, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Location");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Loc ID", "Name", "Address", "Type", "Pincode", "Capacity"};
        tableModel = new DefaultTableModel(cols, 0);
        locationTable = new JTable(tableModel);
        locationTable.setRowHeight(30);
        locationTable.setFillsViewportHeight(true);
        
        // Brighter Header
        locationTable.getTableHeader().setBackground(new Color(30, 48, 80));
        locationTable.getTableHeader().setForeground(Color.WHITE);
        locationTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(locationTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String name = txtName.getText().trim();
            String address = txtAddr.getText().trim();
            String pincode = txtPincode.getText().trim();
            String capacityStr = txtCapacity.getText().trim();

            if (!ValidationUtils.isNotEmpty(name)) {
                JOptionPane.showMessageDialog(this, "Invalid Name (cannot be empty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isNotEmpty(address)) {
                JOptionPane.showMessageDialog(this, "Invalid Address (cannot be empty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidPincode(pincode)) {
                JOptionPane.showMessageDialog(this, "Invalid Pincode (must be exactly 6 digits).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidNumber(capacityStr)) {
                JOptionPane.showMessageDialog(this, "Invalid Capacity (must be a positive number).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Location loc = new Location();
                loc.setName(name);
                loc.setAddress(address);
                loc.setType((String) comboType.getSelectedItem());
                loc.setPincode(pincode);
                loc.setCapacity(Integer.parseInt(capacityStr));

                locationDAO.addLocation(loc);
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            int row = locationTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a location to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "CAUTION: Deleting this Location will also delete all linked records in:\n" +
                "- LOCATION_SUPPLY\n" +
                "- RELIEF_SERVICE (Matches tied to this location)\n" +
                "Are you sure you want to proceed?", "Confirm Cascaded Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    locationDAO.deleteLocation(id);
                    JOptionPane.showMessageDialog(this, "Location and linked inventory links deleted.");
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
            List<Location> locations = locationDAO.getAllLocations();
            for (Location loc : locations) {
                tableModel.addRow(new Object[]{
                        loc.getLocationId(), loc.getName(), loc.getAddress(), loc.getType(), loc.getPincode(), loc.getCapacity()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}