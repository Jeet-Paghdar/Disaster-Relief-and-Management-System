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
        
        JComboBox<String> comboLocation = new JComboBox<>();
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
 
        // Row 2: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Add Supply");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnPanel.add(btnAdd);
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
 
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);
 
        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Item Name", "Quantity", "Type", "Expiry Date"};
        tableModel = new DefaultTableModel(cols, 0);
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

            if (!ValidationUtils.isNotEmpty(itemName)) {
                JOptionPane.showMessageDialog(this, "Invalid Item Name (cannot be empty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidNumber(qtyStr)) {
                JOptionPane.showMessageDialog(this, "Invalid Quantity (must be a positive number).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isNotEmpty(type)) {
                JOptionPane.showMessageDialog(this, "Invalid Type (cannot be empty).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Supply s = new Supply();
                s.setItemName(itemName);
                s.setQuantity(Integer.parseInt(qtyStr));
                s.setType(type);
                s.setExpiryDate(LocalDate.now().plusMonths(6));

                // Parse Location ID from selection e.g. "1 - Shelter A"
                String selected = (String) comboLocation.getSelectedItem();
                int locId = (selected != null) ? Integer.parseInt(selected.split(" - ")[0]) : 1;

                supplyDAO.addSupplyWithLocation(s, locId);
                JOptionPane.showMessageDialog(this, "Supply Added and linked to Location!");
                loadTableData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
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

    private void loadLocations(JComboBox<String> combo) {
        try {
            LocationDAO locDAO = new LocationDAO();
            List<Location> locations = locDAO.getAllLocations();
            for (Location l : locations) {
                combo.addItem(l.getLocationId() + " - " + l.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}