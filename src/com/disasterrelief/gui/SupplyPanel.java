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

public class SupplyPanel extends JPanel {
    private JTable supplyTable;
    private DefaultTableModel tableModel;
    private SupplyDAO supplyDAO;

    public SupplyPanel() {
        supplyDAO = new SupplyDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form
        JPanel formContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formContainer.setBorder(BorderFactory.createTitledBorder("Manage Supplies"));

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 15, 10));

        JTextField txtItemName = new JTextField(12);
        JTextField txtQuantity = new JTextField(12);
        JTextField txtType = new JTextField(12);
        JComboBox<String> comboLocation = new JComboBox<>();
        loadLocations(comboLocation);

        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(txtItemName);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(txtQuantity);
        formPanel.add(new JLabel("Type (e.g. Food):"));
        formPanel.add(txtType);
        formPanel.add(new JLabel("Stored At Location:"));
        formPanel.add(comboLocation);

        JButton btnAdd = new JButton("Add Supply");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnRefresh = new JButton("Refresh");
        formPanel.add(btnAdd);
        formPanel.add(btnDelete);
        formPanel.add(btnRefresh);

        formContainer.add(formPanel);
        add(formContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Item Name", "Quantity", "Type", "Expiry Date"};
        tableModel = new DefaultTableModel(cols, 0);
        supplyTable = new JTable(tableModel);
        add(new JScrollPane(supplyTable), BorderLayout.CENTER);

        // Actions
        btnAdd.addActionListener(e -> {
            if (txtItemName.getText().trim().isEmpty() || txtType.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all text fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int qty;
            try {
                qty = Integer.parseInt(txtQuantity.getText().trim());
                if (qty < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity must be a valid positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Supply s = new Supply();
                s.setItemName(txtItemName.getText().trim());
                s.setQuantity(qty);
                s.setType(txtType.getText().trim());
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