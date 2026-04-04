package com.disasterrelief.gui;

import com.disasterrelief.dao.InquirerDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MatchRegistryPanel extends JPanel {
    private JTable matchTable;
    private DefaultTableModel tableModel;
    private InquirerDAO inquirerDAO;

    public MatchRegistryPanel() {
        inquirerDAO = new InquirerDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Successful Family Matches & Registry", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        add(header, BorderLayout.NORTH);

        String[] cols = {"Inquirer Name", "Victim Name", "Relationship / Info"};
        tableModel = new DefaultTableModel(cols, 0);
        matchTable = new JTable(tableModel);
        add(new JScrollPane(matchTable), BorderLayout.CENTER);

        JButton btnDelete = new JButton("Delete Selected Match");
        JButton btnRefresh = new JButton("Refresh Registry");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnDelete);
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        btnDelete.addActionListener(e -> {
            int row = matchTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a match to delete.");
                return;
            }
            
            String inqFull = (String) tableModel.getValueAt(row, 0);
            String vicFull = (String) tableModel.getValueAt(row, 1);
            
            // Simple space split (Safe for first/last name assumption)
            String[] inq = inqFull.split(" ", 2);
            String[] vic = vicFull.split(" ", 2);
            
            int confirm = JOptionPane.showConfirmDialog(this, "Remove this match record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    inquirerDAO.deleteMatchByName(inq[0], inq[1], vic[0], vic[1]);
                    JOptionPane.showMessageDialog(this, "Match record removed.");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0);
            List<String[]> matches = inquirerDAO.getMatchedRegistry();
            for (String[] m : matches) {
                tableModel.addRow(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}