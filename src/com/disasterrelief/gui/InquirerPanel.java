package com.disasterrelief.gui;

import com.disasterrelief.dao.InquirerDAO;
import com.disasterrelief.utils.ValidationUtils;
import javax.swing.*;
import java.awt.*;

public class InquirerPanel extends JPanel {

    private InquirerDAO inquirerDAO;

    public InquirerPanel() {
        inquirerDAO = new InquirerDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // --- TOP: Input Search Form ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createTitledBorder("Search for Loved Ones & Secure Match"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE; // Prevents stretching
        gbc.anchor = GridBagConstraints.WEST;

        // Note: Help label at the top
        JLabel helpLabel = new JLabel("<html><i>Note: Matching requires exact names and phone numbers for unique identification.</i></html>");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        gbc.insets = new Insets(5, 10, 15, 10);
        formContainer.add(helpLabel, gbc);

        // Adaptive Pinning: Right Glue (Column 4)
        gbc.gridx = 4; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 1.0;
        formContainer.add(Box.createHorizontalGlue(), gbc);

        Dimension fieldSize = new Dimension(150, 30);

        JTextField txtInquirerFirst = new JTextField();
        txtInquirerFirst.setPreferredSize(fieldSize);
        txtInquirerFirst.setMinimumSize(fieldSize);
        txtInquirerFirst.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtInquirerLast = new JTextField();
        txtInquirerLast.setPreferredSize(fieldSize);
        txtInquirerLast.setMinimumSize(fieldSize);
        txtInquirerLast.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtVictimFirst = new JTextField();
        txtVictimFirst.setPreferredSize(fieldSize);
        txtVictimFirst.setMinimumSize(fieldSize);
        txtVictimFirst.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtVictimLast = new JTextField();
        txtVictimLast.setPreferredSize(fieldSize);
        txtVictimLast.setMinimumSize(fieldSize);
        txtVictimLast.setMargin(new Insets(5, 8, 5, 8));

        JTextField txtVictimPhone = new JTextField();
        txtVictimPhone.setPreferredSize(fieldSize);
        txtVictimPhone.setMinimumSize(fieldSize);
        txtVictimPhone.setMargin(new Insets(5, 8, 5, 8));

        JComboBox<String> comboRelation = new JComboBox<>(new String[]{"Parent", "Child", "Sibling", "Spouse", "Extended Family"});
        comboRelation.setPreferredSize(fieldSize);
        comboRelation.setMinimumSize(fieldSize);

        // Row 1: Inquirer Name
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0;
        formContainer.add(new JLabel("First Name (Inquirer):"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtInquirerFirst, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Last Name (Inquirer):"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtInquirerLast, gbc);

        // Row 2: Victim Name
        gbc.gridy = 2;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Victim First Name:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtVictimFirst, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Victim Last Name:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(txtVictimLast, gbc);

        // Row 3: Other Details
        gbc.gridy = 3;
        gbc.gridx = 0; 
        formContainer.add(new JLabel("Victim Phone:"), gbc);
        gbc.gridx = 1; 
        formContainer.add(txtVictimPhone, gbc);
        gbc.gridx = 2; 
        formContainer.add(new JLabel("Your Relation:"), gbc);
        gbc.gridx = 3; 
        formContainer.add(comboRelation, gbc);

        // Row 4: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);
        JButton btnSearch = new JButton("Search & Secure Match");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(30, 48, 80));
        btnSearch.setForeground(Color.WHITE);
        btnPanel.add(btnSearch);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 10, 5, 10);
        formContainer.add(btnPanel, gbc);

        add(formContainer, BorderLayout.NORTH);

        btnSearch.addActionListener(e -> {
            String iFirst = txtInquirerFirst.getText().trim();
            String iLast = txtInquirerLast.getText().trim();
            String vFirst = txtVictimFirst.getText().trim();
            String vLast = txtVictimLast.getText().trim();
            String vPhone = txtVictimPhone.getText().trim();

            if (!ValidationUtils.isValidName(iFirst)) {
                JOptionPane.showMessageDialog(this, "Invalid Inquirer First Name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidName(iLast)) {
                JOptionPane.showMessageDialog(this, "Invalid Inquirer Last Name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidName(vFirst)) {
                JOptionPane.showMessageDialog(this, "Invalid Victim First Name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidName(vLast)) {
                JOptionPane.showMessageDialog(this, "Invalid Victim Last Name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidPhone(vPhone)) {
                JOptionPane.showMessageDialog(this, "Invalid Victim Phone (must be 10 digits).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean matched = inquirerDAO.findAndMatchVictim(
                        iFirst, iLast, vFirst, vLast, vPhone,
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