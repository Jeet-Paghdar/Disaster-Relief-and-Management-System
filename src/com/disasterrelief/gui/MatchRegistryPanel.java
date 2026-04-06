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
        setBackground(Color.WHITE);

        JLabel header = new JLabel("Successful Family Matches & Registry", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));

        // Auto-refresh when tab is switched to
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) { loadData(); }
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {}
        });

        // Top Toolbar for Actions
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolbar.setBackground(new Color(245, 247, 250));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 225)));

        JButton btnUpdateMatch = new JButton("Update Match");
        btnUpdateMatch.setFocusPainted(false);

        JButton btnDelete = new JButton("Delete Match");
        btnDelete.setFocusPainted(false);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFocusPainted(false);

        toolbar.add(btnUpdateMatch);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(header, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.CENTER);
        
        add(topContainer, BorderLayout.NORTH);

        String[] cols = {"ID", "Inquirer Name", "Victim Name", "Relationship / Info"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        matchTable = new JTable(tableModel);
        matchTable.setRowHeight(30);
        matchTable.setFillsViewportHeight(true);
        matchTable.getTableHeader().setBackground(new Color(30, 48, 80));
        matchTable.getTableHeader().setForeground(Color.WHITE);
        matchTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        matchTable.getColumnModel().getColumn(0).setMinWidth(0);
        matchTable.getColumnModel().getColumn(0).setMaxWidth(0);
        matchTable.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(matchTable), BorderLayout.CENTER);

        btnUpdateMatch.addActionListener(e -> {
            int row = matchTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a match to update.");
                return;
            }
            
            int serviceId = (int) tableModel.getValueAt(row, 0);
            String currentInfo = (String) tableModel.getValueAt(row, 3);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Match Information", true);
            dialog.setSize(400, 200);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(this);

            JPanel form = new JPanel(new GridLayout(2, 1, 5, 5));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            JTextField infoField = new JTextField(currentInfo);
            form.add(new JLabel("Relationship / Notes:"));
            form.add(infoField);
            dialog.add(form, BorderLayout.CENTER);

            JPanel bp = new JPanel();
            JButton save = new JButton("Save Changes");
            JButton cancel = new JButton("Cancel");
            bp.add(save); bp.add(cancel);
            dialog.add(bp, BorderLayout.SOUTH);

            cancel.addActionListener(ev -> dialog.dispose());
            save.addActionListener(ev -> {
                try {
                    inquirerDAO.updateMatchById(serviceId, infoField.getText().trim());
                    JOptionPane.showMessageDialog(dialog, "Match Updated!");
                    dialog.dispose();
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });
            dialog.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int row = matchTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a match to delete.");
                return;
            }
            
            int serviceId = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Remove this match record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    inquirerDAO.deleteMatchById(serviceId);
                    JOptionPane.showMessageDialog(this, "Match record removed.");
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnRefresh.addActionListener(e -> loadData());

        // Row Selection Listener removed as we use JDialog now
        /*
        matchTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = matchTable.getSelectedRow();
                if (row >= 0) {
                    // txtInfo handled by dialog now
                }
            }
        });
        */

        loadData();
    }

    private void loadData() {
        try {
            tableModel.setRowCount(0);
            List<Object[]> matches = inquirerDAO.getMatchedRegistry();
            for (Object[] m : matches) {
                tableModel.addRow(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}