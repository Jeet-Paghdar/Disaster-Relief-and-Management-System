package com.disasterrelief;

import com.disasterrelief.gui.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                seedDatabase();
                
                // Launch the login frame
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void seedDatabase() {
        String countSql = "SELECT COUNT(*) AS total FROM GOVT_AGENCY";
        try (java.sql.Connection conn = com.disasterrelief.utils.DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(countSql)) {
            
            if (rs.next() && rs.getInt("total") == 0) {
                System.out.println("Seeding national government agencies into database...");
                stmt.addBatch("INSERT INTO GOVT_AGENCY (AGENCY_NAME, BUDGET_CODE) VALUES ('NDMA (National Disaster Management Authority)', 'BGT-IN-01')");
                stmt.addBatch("INSERT INTO GOVT_AGENCY (AGENCY_NAME, BUDGET_CODE) VALUES ('NDRF (National Disaster Response Force)', 'BGT-IN-02')");
                stmt.addBatch("INSERT INTO GOVT_AGENCY (AGENCY_NAME, BUDGET_CODE) VALUES ('NIDM (National Institute of Disaster Management)', 'BGT-IN-03')");
                stmt.executeBatch();
            }
        } catch (Exception e) {
            System.err.println("Seed skipped: " + e.getMessage());
        }
    }
}
