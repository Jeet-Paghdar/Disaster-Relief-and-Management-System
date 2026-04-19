package com.disasterrelief.gui;

import com.disasterrelief.dao.VictimDAO;
import com.disasterrelief.models.Victim;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class VictimSearchPanel extends JPanel {
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> comboCriteria;
    private VictimDAO victimDAO;

    public VictimSearchPanel() {
        victimDAO = new VictimDAO();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        JLabel header = new JLabel("Quick Victim Discovery", SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(30, 48, 80));

        // Search Bar Container
        JPanel searchBar = new JPanel(new GridBagLayout());
        searchBar.setBackground(new Color(245, 247, 250));
        searchBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 225)),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.fill = GridBagConstraints.VERTICAL;

        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        comboCriteria = new JComboBox<>(new String[]{"NAME", "PHONE", "ID"});
        comboCriteria.setPreferredSize(new Dimension(100, 35));

        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(0, 120, 215));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setPreferredSize(new Dimension(120, 35));
        btnSearch.setFocusPainted(false);

        JButton btnClear = new JButton("Clear Search");
        btnClear.setBackground(new Color(240, 242, 245));
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClear.setPreferredSize(new Dimension(140, 35));
        btnClear.setFocusPainted(false);

        searchBar.add(new JLabel("Find Victims by:"), gbc);
        searchBar.add(comboCriteria, gbc);
        searchBar.add(txtSearch, gbc);
        searchBar.add(btnSearch, gbc);
        searchBar.add(btnClear, gbc);

        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setBackground(Color.WHITE);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(searchBar, BorderLayout.CENTER);

        // Results Table
        String[] columns = {"ID", "First Name", "Last Name", "DOB", "Gender", "Blood", "Location", "Phone", "Injury", "Disaster"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(35);
        resultTable.setFillsViewportHeight(true);
        resultTable.getTableHeader().setBackground(new Color(30, 48, 80));
        resultTable.getTableHeader().setForeground(Color.WHITE);
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        add(topSection, BorderLayout.NORTH);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // Actions
        btnSearch.addActionListener(e -> performSearch());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            loadAll();
        });
        
        // Enter key performance
        txtSearch.addActionListener(e -> performSearch());

        loadAll();
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        String crit = (String) comboCriteria.getSelectedItem();
        
        if (query.isEmpty()) {
            loadAll();
            return;
        }

        try {
            List<Victim> results = victimDAO.searchVictims(query, crit);
            updateTable(results);
        } catch (com.disasterrelief.exceptions.VictimNotFoundException vnfe) {
            JOptionPane.showMessageDialog(this, vnfe.getMessage(), "Search Warning", JOptionPane.WARNING_MESSAGE);
            tableModel.setRowCount(0); // Clear table on error
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric ID.", "Search Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during search: " + ex.getMessage());
        }
    }

    private void loadAll() {
        try {
            updateTable(victimDAO.getAllVictims());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateTable(List<Victim> victims) {
        tableModel.setRowCount(0);
        for (Victim v : victims) {
            tableModel.addRow(new Object[]{
                v.getPersonId(), v.getFirstName(), v.getLastName(),
                v.getDob(), v.getGender(), v.getBloodType(),
                v.getAddressAfter(), v.getPhoneNumber(), v.getInjuryStatus(),
                v.getDisasterId()
            });
        }
    }
}
