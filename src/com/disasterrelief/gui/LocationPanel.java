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

        JComboBox<String> comboType = new JComboBox<>(new String[]{"Shelter", "Hospital", "Supply Center"});
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

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Location");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 2; 
        formContainer.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtCapacity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"Loc ID", "Name", "Address", "Type", "Pincode", "Cap", "Occ"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
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

            if (!ValidationUtils.isNotEmpty(name) || !ValidationUtils.isNotEmpty(address) || 
                !ValidationUtils.isValidPincode(pincode) || !ValidationUtils.isValidNumber(capacityStr)) {
                JOptionPane.showMessageDialog(this, "Please fix validation errors.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Location Added Successfully!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = locationTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a location to update.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Location", true);
            dialog.setSize(400, 300);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JTextField updateName = new JTextField(tableModel.getValueAt(row, 1).toString());
            JTextField updateAddr = new JTextField(tableModel.getValueAt(row, 2).toString());
            JComboBox<String> updateType = new JComboBox<>(new String[]{"Shelter", "Hospital", "Supply Center"});
            updateType.setSelectedItem(tableModel.getValueAt(row, 3).toString());
            JTextField updatePincode = new JTextField(tableModel.getValueAt(row, 4).toString());
            JTextField updateCapacity = new JTextField(tableModel.getValueAt(row, 5).toString());
            

            form.add(new JLabel("Name:")); form.add(updateName);
            form.add(new JLabel("Address:")); form.add(updateAddr);
            form.add(new JLabel("Type:")); form.add(updateType);
            form.add(new JLabel("Pincode:")); form.add(updatePincode);
            form.add(new JLabel("Capacity:")); form.add(updateCapacity);

            dialog.add(form, BorderLayout.CENTER);

            JPanel btnGrid = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            btnGrid.add(saveBtn);
            btnGrid.add(cancelBtn);
            dialog.add(btnGrid, BorderLayout.SOUTH);

            cancelBtn.addActionListener(ev -> dialog.dispose());

            saveBtn.addActionListener(ev -> {
                String name = updateName.getText().trim();
                String address = updateAddr.getText().trim();
                String pincode = updatePincode.getText().trim();
                String capacityStr = updateCapacity.getText().trim();

                if (!ValidationUtils.isNotEmpty(name) || !ValidationUtils.isNotEmpty(address) || 
                    !ValidationUtils.isValidPincode(pincode) || !ValidationUtils.isValidNumber(capacityStr)) {
                    JOptionPane.showMessageDialog(dialog, "Please fix validation errors before updating.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int id = (int) tableModel.getValueAt(row, 0);
                    Location loc = new Location();
                    loc.setLocationId(id);
                    loc.setName(name);
                    loc.setAddress(address);
                    loc.setType((String) updateType.getSelectedItem());
                    loc.setPincode(pincode);
                    loc.setCapacity(Integer.parseInt(capacityStr));
                    
                    

                    locationDAO.updateLocation(loc);
                    JOptionPane.showMessageDialog(dialog, "Location Updated Successfully!");
                    dialog.dispose();
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            dialog.setVisible(true);
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
                "- MATCH_REGISTRY (Matches tied to this location)\n" +
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

        btnRefresh.addActionListener(e -> {
            loadTableData();
        });

        // Row Selection Listener
        locationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = locationTable.getSelectedRow();
                if (row >= 0) {
                    txtName.setText(tableModel.getValueAt(row, 1).toString());
                    txtAddr.setText(tableModel.getValueAt(row, 2).toString());
                    comboType.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                    txtPincode.setText(tableModel.getValueAt(row, 4).toString());
                    txtCapacity.setText(tableModel.getValueAt(row, 5).toString());
                    
                    
                }
            }
        });

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<Location> locations = locationDAO.getAllLocations();
            for (Location loc : locations) {
                tableModel.addRow(new Object[]{
                        loc.getLocationId(), loc.getName(), loc.getAddress(), loc.getType(), loc.getPincode(), loc.getCapacity(), loc.getCurrentOccupancy()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}