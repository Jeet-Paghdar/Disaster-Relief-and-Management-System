package com.disasterrelief.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainDashboard extends JFrame {
    private String username;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private List<JButton> navButtons;

    public MainDashboard(String username) {
        this.username = username;
        this.navButtons = new ArrayList<>();
        
        setTitle("Disaster Relief & Management System - Welcome " + this.username);
        setSize(1400, 850); // Generous size for professional look
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Layout Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);

        // 1. Header (Upper Bar)
        JPanel headerPanel = createHeader();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // 2. Sidebar Navigation (West)
        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);

        // 3. Content Area (Center)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        // Add all panels to the CardLayout
        contentPanel.add(new DisasterPanel(), "Disaster Setup");
        contentPanel.add(new VictimPanel(), "Victim Management");
        contentPanel.add(new WorkerPanel(), "Worker Directory");
        contentPanel.add(new MedicalPanel(), "Medical Records");
        contentPanel.add(new LocationPanel(), "Locations");
        contentPanel.add(new SupplyPanel(), "Supply Inventory");
        contentPanel.add(new InquirerPanel(), "Inquirer Matcher");
        contentPanel.add(new MatchRegistryPanel(), "Match Registry");

        mainContainer.add(contentPanel, BorderLayout.CENTER);
        add(mainContainer);
        
        // Default to first tab
        if (!navButtons.isEmpty()) {
            switchTab("Disaster Setup", navButtons.get(0));
        }
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 48, 80)); // Professional Dark Blue
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("Central Control Dashboard", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        controlsPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Logged in as: " + this.username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        welcomeLabel.setForeground(new Color(220, 220, 220));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(100, 32));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());

        controlsPanel.add(welcomeLabel);
        controlsPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controlsPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 242, 245));
        sidebar.setPreferredSize(new Dimension(220, 0)); // Fixed width for sidebar
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        String[] tabs = {
            "Disaster Setup", "Victim Management", "Worker Directory", 
            "Medical Records", "Locations", "Supply Inventory", 
            "Inquirer Matcher", "Match Registry"
        };

        sidebar.add(Box.createVerticalStrut(5));
        for (int i = 0; i < tabs.length; i++) {
            JButton btn = createNavButton(tabs[i]);
            navButtons.add(btn);
            
            // Add a proper bottom partition line (Matte Border)
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 225)), // The partition line
                BorderFactory.createEmptyBorder(0, 22, 0, 0) // Padding for text
            ));
            
            sidebar.add(btn);
        }
        
        return sidebar;
    }

    private JButton createNavButton(String name) {
        JButton btn = new JButton(name);
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setPreferredSize(new Dimension(220, 48));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(240, 242, 245));
        btn.setForeground(new Color(60, 60, 60));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Initial default border with the darkened bottom partition line
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 185, 200)), // Sharper partition line
            BorderFactory.createEmptyBorder(0, 20, 0, 0)
        ));
        
        btn.addActionListener(e -> switchTab(name, btn));
        return btn;
    }

    private void switchTab(String name, JButton activeBtn) {
        cardLayout.show(contentPanel, name);
        
        // Reset all buttons
        for (JButton btn : navButtons) {
            btn.setBackground(new Color(240, 242, 245));
            btn.setForeground(new Color(60, 60, 60));
            // Reset to default partition border
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 185, 200)),
                BorderFactory.createEmptyBorder(0, 20, 0, 0)
            ));
        }
        
        // Highlight active with Left Indicator bar
        activeBtn.setBackground(new Color(30, 48, 80));
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 1, 0, new Color(0, 120, 215)), // 4px Blue Primary Indicator
            BorderFactory.createEmptyBorder(0, 16, 0, 0) // Adjusted padding for the bar
        ));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to log out?", "Logout", 
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}