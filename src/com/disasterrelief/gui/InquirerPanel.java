package com.disasterrelief.gui;

import com.disasterrelief.dao.InquirerDAO;
import javax.swing.*;
import java.awt.*;

public class InquirerPanel extends JPanel {

    private InquirerDAO inquirerDAO;

    public InquirerPanel() {
        inquirerDAO = new InquirerDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(BorderFactory.createTitledBorder("Disaster Relief: Victim & Family Matcher"));

        JLabel helpLabel = new JLabel("<html><i>Note: Matching requires exact names and phone numbers for unique identification.</i></html>");
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formContainer.add(helpLabel);
        formContainer.add(Box.createVerticalStrut(15));

        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 20, 15));
        
        JTextField txtInquirerFirst = new JTextField();
        JTextField txtInquirerLast = new JTextField();
        JTextField txtVictimFirst = new JTextField();
        JTextField txtVictimLast = new JTextField();
        JTextField txtVictimPhone = new JTextField();
        JComboBox<String> comboRelation = new JComboBox<>(new String[]{"Parent", "Child", "Sibling", "Spouse", "Extended Family"});

        gridPanel.add(new JLabel("First Name (Inquirer):"));
        gridPanel.add(txtInquirerFirst);
        gridPanel.add(new JLabel("Last Name (Inquirer):"));
        gridPanel.add(txtInquirerLast);

        gridPanel.add(new JLabel("Victim's First Name:"));
        gridPanel.add(txtVictimFirst);
        gridPanel.add(new JLabel("Victim's Last Name:"));
        gridPanel.add(txtVictimLast);

        formContainer.add(gridPanel);
        formContainer.add(Box.createVerticalStrut(15));

        JPanel subGrid = new JPanel(new GridLayout(2, 2, 20, 15));
        subGrid.add(new JLabel("Victim Phone Number (Mandatory):"));
        subGrid.add(txtVictimPhone);
        subGrid.add(new JLabel("Your Relation:"));
        subGrid.add(comboRelation);
        
        formContainer.add(subGrid);
        formContainer.add(Box.createVerticalStrut(20));

        JButton btnSearch = new JButton("Search & Secure Match");
        btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearch.setPreferredSize(new Dimension(180, 40));
        formContainer.add(btnSearch);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(formContainer);
        add(wrapper, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            if (txtInquirerFirst.getText().trim().isEmpty() || txtVictimFirst.getText().trim().isEmpty() || txtVictimLast.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in names correctly.");
                return;
            }

            try {
                boolean matched = inquirerDAO.findAndMatchVictim(
                        txtInquirerFirst.getText().trim(),
                        txtInquirerLast.getText().trim(),
                        txtVictimFirst.getText().trim(),
                        txtVictimLast.getText().trim(),
                        txtVictimPhone.getText().trim(),
                        (String) comboRelation.getSelectedItem()
                );

                if (matched) {
                    JOptionPane.showMessageDialog(this, "MATCHED! The victim was found in the database.\nYour relationship has been securely recorded.", "Match Found", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No records found matching that victim's exact First and Last name.", "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error during search: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}