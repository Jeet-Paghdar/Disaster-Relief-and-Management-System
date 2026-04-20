package com.disasterrelief.gui;

import com.disasterrelief.dao.SupplyDAO;
import com.disasterrelief.models.Supply;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import com.disasterrelief.dao.LocationDAO;
import com.disasterrelief.models.Location;
import com.disasterrelief.utils.ValidationUtils;

public class SupplyPanel extends JPanel {
    private JTable supplyTable;
    private DefaultTableModel tableModel;
    private SupplyDAO supplyDAO;

    public SupplyPanel() {
        supplyDAO = new SupplyDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        // Form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Manage Supplies"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;
 
        // Adaptive Pinning: Right Glue (Column 4)
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);
 
        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtItemName = new JTextField();
        txtItemName.setPreferredSize(fieldSize);
        txtItemName.setMinimumSize(fieldSize);
        txtItemName.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtQuantity = new JTextField();
        txtQuantity.setPreferredSize(fieldSize);
        txtQuantity.setMinimumSize(fieldSize);
        txtQuantity.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtType = new JTextField();
        txtType.setPreferredSize(fieldSize);
        txtType.setMinimumSize(fieldSize);
        txtType.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtExpiry = new JTextField();
        txtExpiry.setPreferredSize(fieldSize);
        txtExpiry.setMinimumSize(fieldSize);
        txtExpiry.setMargin(new Insets(5, 8, 5, 8));
        
        JComboBox<Location> comboLocation = new JComboBox<>();
        comboLocation.setPreferredSize(fieldSize);
        comboLocation.setMinimumSize(fieldSize);
        loadLocations(comboLocation);
 
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formContainer.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtItemName, gbc);
        
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtQuantity, gbc);
 
        // Row 1
        gbc.gridy = 1;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Type (e.g. Food):"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtType, gbc);
        
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Stored At Location:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(comboLocation, gbc);

        // Row 2
        gbc.gridy = 2;
        gbc.gridx = 0;
        formContainer.add(new JLabel("Expiry Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        formContainer.add(txtExpiry, gbc);
 
        // Row 2: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Supply");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnUpdate.setEnabled(true);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
 
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);
 
        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Item Name", "Quantity", "Type", "Expiry Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplyTable = new JTable(tableModel);
        supplyTable.setRowHeight(30);
        supplyTable.setFillsViewportHeight(true);
        
        // Brighter Header
        supplyTable.getTableHeader().setBackground(new Color(30, 48, 80));
        supplyTable.getTableHeader().setForeground(Color.WHITE);
        supplyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        add(new JScrollPane(supplyTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            String itemName = txtItemName.getText().trim();
            String qtyStr = txtQuantity.getText().trim();
            String type = txtType.getText().trim();
            String expiryStr = txtExpiry.getText().trim();

            if (!ValidationUtils.isNotEmpty(itemName) || !ValidationUtils.isValidNumber(qtyStr) || !ValidationUtils.isNotEmpty(type)) {
                JOptionPane.showMessageDialog(this, "Please check Name, Quantity, and Type.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Supply s = new Supply();
                s.setItemName(itemName);
                s.setQuantity(Integer.parseInt(qtyStr));
                s.setType(type);
                if (ValidationUtils.isNotEmpty(expiryStr)) {
                    s.setExpiryDate(LocalDate.parse(expiryStr));
                } else {
                    s.setExpiryDate(null);
                }

                Location selected = (Location) comboLocation.getSelectedItem();
                int locId = (selected != null) ? selected.getLocationId() : -1;

                supplyDAO.addSupplyWithLocation(s, locId);
                JOptionPane.showMessageDialog(this, "Supply Added Successfully!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = supplyTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a supply item to update.");
                return;
            }

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Supply", true);
            dialog.setSize(400, 250);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JTextField updateItemName = new JTextField(tableModel.getValueAt(row, 1).toString());
            JTextField updateQuantity = new JTextField(tableModel.getValueAt(row, 2).toString());
            updateQuantity.setEditable(false);
            JTextField updateType = new JTextField(tableModel.getValueAt(row, 3).toString());
            JTextField updateExpiry = new JTextField(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");

            form.add(new JLabel("Item Name:")); form.add(updateItemName);
            form.add(new JLabel("Type (e.g. Food):")); form.add(updateType);
            form.add(new JLabel("Expiry Date (YYYY-MM-DD):")); form.add(updateExpiry);

            dialog.add(form, BorderLayout.CENTER);

            JPanel btnGrid = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            btnGrid.add(saveBtn);
            btnGrid.add(cancelBtn);
            dialog.add(btnGrid, BorderLayout.SOUTH);

            cancelBtn.addActionListener(ev -> dialog.dispose());

            saveBtn.addActionListener(ev -> {
                String itemName = updateItemName.getText().trim();
                String type = updateType.getText().trim();
                String expiryStr = updateExpiry.getText().trim();

                if (!ValidationUtils.isNotEmpty(itemName) || !ValidationUtils.isNotEmpty(type)) {
                    JOptionPane.showMessageDialog(dialog, "Please fix validation errors before updating.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    int supplyId = (int) tableModel.getValueAt(row, 0);
                    Supply s = new Supply();
                    s.setSupplyId(supplyId);
                    s.setItemName(itemName);
                    s.setQuantity(Integer.parseInt(updateQuantity.getText().trim()));
                    s.setType(type);
                    if (!expiryStr.isEmpty()) {
                        s.setExpiryDate(LocalDate.parse(expiryStr));
                    } else {
                        s.setExpiryDate(null);
                    }

                    supplyDAO.updateSupply(s);
                    JOptionPane.showMessageDialog(dialog, "Supply Updated Successfully!");
                    dialog.dispose();
                    loadTableData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            dialog.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int row = supplyTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a supply item to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "CAUTION: Deleting this Supply will remove it from all:\n" +
                "- LOCATION_SUPPLY (Stock inventories)\n" +
                "- VICTIM_SUPPLY (Distribution records)\n" +
                "Are you sure you want to proceed?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    supplyDAO.deleteSupply(id);
                    JOptionPane.showMessageDialog(this, "Supply and all inventory links deleted.");
                    loadTableData();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnRefresh.addActionListener(e -> loadTableData());

        // Row Selection Listener
        supplyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = supplyTable.getSelectedRow();
                if (row >= 0) {
                    txtItemName.setText(tableModel.getValueAt(row, 1).toString());
                    txtQuantity.setText(tableModel.getValueAt(row, 2).toString());
                    txtType.setText(tableModel.getValueAt(row, 3).toString());
                    txtExpiry.setText(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
                }
            }
        });

        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0);
            List<Supply> supplies = supplyDAO.getAllSupplies();
            for (Supply s : supplies) {
                tableModel.addRow(new Object[]{s.getSupplyId(), s.getItemName(), s.getQuantity(), s.getType(), s.getExpiryDate()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLocations(JComboBox<Location> combo) {
        try {
            LocationDAO locDAO = new LocationDAO();
            List<Location> locations = locDAO.getAllLocations();
            for (Location l : locations) {
                combo.addItem(l);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}