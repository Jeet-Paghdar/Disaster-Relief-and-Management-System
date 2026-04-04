package com.disasterrelief.gui;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {

    public MainDashboard() {
        setTitle("Disaster Relief & Management System - Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Setup Tabbed Pane navigation as a Side Menu
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        // Create the individual panels
        DisasterPanel disasterPanel = new DisasterPanel();
        LocationPanel locationPanel = new LocationPanel();
        WorkerPanel workerPanel = new WorkerPanel();
        VictimPanel victimPanel = new VictimPanel();
        InquirerPanel inquirerPanel = new InquirerPanel();
        MatchRegistryPanel matchRegistryPanel = new MatchRegistryPanel();
        MedicalPanel medicalPanel = new MedicalPanel();
        SupplyPanel supplyPanel = new SupplyPanel();

        // Add panels to tabs (Custom Requested Sequence)
        tabbedPane.addTab("Disaster Setup", disasterPanel);
        tabbedPane.addTab("Victim Management", victimPanel);
        tabbedPane.addTab("Worker Directory", workerPanel);
        tabbedPane.addTab("Medical Records", medicalPanel);
        tabbedPane.addTab("Locations", locationPanel);
        tabbedPane.addTab("Supply Inventory", supplyPanel);
        tabbedPane.addTab("Inquirer Matcher", inquirerPanel);
        tabbedPane.addTab("Match Registry", matchRegistryPanel);

        // Main Layout
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel headerLabel = new JLabel("Central Control Dashboard", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        mainContainer.add(headerLabel, BorderLayout.NORTH);
        mainContainer.add(tabbedPane, BorderLayout.CENTER);

        add(mainContainer);
    }
}