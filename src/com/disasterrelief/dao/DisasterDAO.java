package com.disasterrelief.dao;

import com.disasterrelief.models.Disaster;
import com.disasterrelief.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisasterDAO {
    
    /**
     * Auto-Sync: Ensures the database agencies match the GUI labels.
     * This handles the IDs 1, 2, and 3 specifically for the demonstration.
     */
    public void ensureAgenciesExist() throws SQLException {
        String[] names = {"NDMA", "NDRF", "NIDM"};
        try (Connection conn = DBConnection.getConnection()) {
            for (int i = 0; i < names.length; i++) {
                int id = i + 1;
                String name = names[i];
                
                // 1. Try to update existing row
                String updateSQL = "UPDATE GOVT_AGENCY SET AGENCY_NAME = ? WHERE AGENCY_ID = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(updateSQL)) {
                    checkStmt.setString(1, name);
                    checkStmt.setInt(2, id);
                    int rows = checkStmt.executeUpdate();
                    
                    // 2. If row doesn't exist, insert it
                    if (rows == 0) {
                        String insertSQL = "INSERT INTO GOVT_AGENCY (AGENCY_ID, AGENCY_NAME, BUDGET_CODE) VALUES (?, ?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                            insertStmt.setInt(1, id);
                            insertStmt.setString(2, name);
                            insertStmt.setString(3, "BUDGET-" + (1000 + id));
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public void addDisaster(Disaster disaster) throws SQLException {
        String sql = "INSERT INTO DISASTER (TYPE, SEVERITY, AGENCY_ID) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, disaster.getType());
            stmt.setString(2, disaster.getSeverity());

            if (disaster.getAgencyId() > 0) {
                stmt.setInt(3, disaster.getAgencyId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER); // Allow null so it doesn't crash if Agency doesn't exist
            }

            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int disasterId = rs.getInt(1);
                    if (disaster.getAffectedRegions() != null && !disaster.getAffectedRegions().trim().isEmpty()) {
                        String[] regions = disaster.getAffectedRegions().split(",");
                        String regionSql = "INSERT INTO DISASTER_REGION (DISASTER_ID, REGION_NAME) VALUES (?, ?)";
                        try (PreparedStatement regionStmt = conn.prepareStatement(regionSql)) {
                            for (String r : regions) {
                                if (r.trim().isEmpty()) continue;
                                regionStmt.setInt(1, disasterId);
                                regionStmt.setString(2, r.trim());
                                regionStmt.addBatch();
                            }
                            regionStmt.executeBatch();
                        }
                    }
                }
            }
        }
    }

    public List<Disaster> getAllDisasters() throws SQLException {
        List<Disaster> disasters = new ArrayList<>();
        String sql = "SELECT d.*, g.AGENCY_NAME, (SELECT GROUP_CONCAT(REGION_NAME SEPARATOR ', ') FROM DISASTER_REGION WHERE DISASTER_ID = d.DISASTER_ID) AS AFFECTED_REGIONS FROM DISASTER d LEFT JOIN GOVT_AGENCY g ON d.AGENCY_ID = g.AGENCY_ID";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Disaster d = new Disaster(
                        rs.getInt("DISASTER_ID"),
                        rs.getString("TYPE"),
                        rs.getString("SEVERITY"),
                        rs.getString("AFFECTED_REGIONS"),
                        rs.getInt("AGENCY_ID"),
                        rs.getString("AGENCY_NAME")
                );
                disasters.add(d);
            }
        }
        return disasters;
    }

    public void updateDisaster(Disaster disaster) throws SQLException {
        // 1. Update the base table (without AFFECTED_REGIONS)
        String sql = "UPDATE DISASTER SET TYPE = ?, SEVERITY = ?, AGENCY_ID = ? WHERE DISASTER_ID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // start transaction
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, disaster.getType());
                    stmt.setString(2, disaster.getSeverity());
                    if (disaster.getAgencyId() > 0) {
                        stmt.setInt(3, disaster.getAgencyId());
                    } else {
                        stmt.setNull(3, java.sql.Types.INTEGER);
                    }
                    stmt.setInt(4, disaster.getDisasterId());
                    stmt.executeUpdate();
                }

                // 2. Clear old regions
                try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM DISASTER_REGION WHERE DISASTER_ID = ?")) {
                    delStmt.setInt(1, disaster.getDisasterId());
                    delStmt.executeUpdate();
                }

                // 3. Insert new regions
                if (disaster.getAffectedRegions() != null && !disaster.getAffectedRegions().trim().isEmpty()) {
                    String[] regions = disaster.getAffectedRegions().split(",");
                    String regionSql = "INSERT INTO DISASTER_REGION (DISASTER_ID, REGION_NAME) VALUES (?, ?)";
                    try (PreparedStatement regionStmt = conn.prepareStatement(regionSql)) {
                        for (String r : regions) {
                            if (r.trim().isEmpty()) continue;
                            regionStmt.setInt(1, disaster.getDisasterId());
                            regionStmt.setString(2, r.trim());
                            regionStmt.addBatch();
                        }
                        regionStmt.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void deleteDisaster(int disasterId) throws SQLException {
        // Cascade manually: Delete Victims -> then everything tied to those victims -> then Disaster
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete everything linked to victims of this disaster first
                String[] victimIdTables = {"MEDICAL_RECORD", "VICTIM_DIETARY_RESTRICTIONS", "VICTIM_SUPPLY", "RELIEF_SERVICE"};
                for (String table : victimIdTables) {
                    String sql = "DELETE FROM " + table + " WHERE VICTIM_ID IN (SELECT VICTIM_ID FROM VICTIM WHERE DISASTER_ID = ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, disasterId);
                        stmt.executeUpdate();
                    }
                }

                // Handle FAMILY_RELATION separately (it has VICTIM1_ID and VICTIM2_ID)
                String familySql = "DELETE FROM FAMILY_RELATION WHERE VICTIM1_ID IN (SELECT VICTIM_ID FROM VICTIM WHERE DISASTER_ID = ?) " +
                                  "OR VICTIM2_ID IN (SELECT VICTIM_ID FROM VICTIM WHERE DISASTER_ID = ?)";
                try (PreparedStatement stmt = conn.prepareStatement(familySql)) {
                    stmt.setInt(1, disasterId);
                    stmt.setInt(2, disasterId);
                    stmt.executeUpdate();
                }

                // Delete Victims
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM VICTIM WHERE DISASTER_ID = ?")) {
                    stmt.setInt(1, disasterId);
                    stmt.executeUpdate();
                }

                // Finally delete from DISASTER_REGION and then DISASTER
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DISASTER_REGION WHERE DISASTER_ID = ?")) {
                    stmt.setInt(1, disasterId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DISASTER WHERE DISASTER_ID = ?")) {
                    stmt.setInt(1, disasterId);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
