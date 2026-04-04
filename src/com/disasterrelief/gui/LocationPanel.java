package com.disasterrelief.gui;

import com.disasterrelief.dao.LocationDAO;
import com.disasterrelief.models.Location;

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

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBorder(BorderFactory.createTitledBorder("Manage Shelters & Locations"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formContainer.add(new JLabel("Name:"), gbc);
        JTextField txtName = new JTextField(15);
        gbc.gridx = 1;
        formContainer.add(txtName, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Address:"), gbc);
        JTextField txtAddr = new JTextField(15);
        gbc.gridx = 3;
        formContainer.add(txtAddr, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formContainer.add(new JLabel("Type:"), gbc);
        JComboBox<String> comboType = new JComboBox<>(new String[]{"Shelter", "Hospital", "Warehouse"});
        gbc.gridx = 1;
        formContainer.add(comboType, gbc);

        gbc.gridx = 2;
        formContainer.add(new JLabel("Pincode:"), gbc);
        JTextField txtPincode = new JTextField(15);
        gbc.gridx = 3;
        formContainer.add(txtPincode, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formContainer.add(new JLabel("Capacity:"), gbc);
        JTextField txtCapacity = new JTextField(15);
        gbc.gridx = 1;
        formContainer.add(txtCapacity, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnAdd = new JButton("Add Location");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 4;
        formContainer.add(btnPanel, gbc);

        // Pushing everything left
        gbc.gridx = 4; gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formContainer.add(new JPanel(), gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Loc ID", "Name", "Address", "Type", "Pincode", "Capacity"};
        tableModel = new DefaultTableModel(cols, 0);
        locationTable = new JTable(tableModel);
        add(new JScrollPane(locationTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() || txtAddr.getText().trim().isEmpty() || txtCapacity.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in Name, Address, and Capacity.");
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(txtCapacity.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Capacity must be a number.");
                return;
            }

            try {
                Location loc = new Location();
                loc.setName(txtName.getText().trim());
                loc.setAddress(txtAddr.getText().trim());
                loc.setType((String) comboType.getSelectedItem());
                loc.setPincode(txtPincode.getText().trim());
                loc.setCapacity(capacity);

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