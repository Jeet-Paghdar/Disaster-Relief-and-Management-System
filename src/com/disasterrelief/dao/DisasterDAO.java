package com.disasterrelief.dao;

import com.disasterrelief.models.Disaster;
import com.disasterrelief.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisasterDAO {

    public void addDisaster(Disaster disaster) throws SQLException {
        String sql = "INSERT INTO DISASTER (TYPE, SEVERITY, AFFECTED_REGIONS, AGENCY_ID) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, disaster.getType());
            stmt.setString(2, disaster.getSeverity());
            stmt.setString(3, disaster.getAffectedRegions());

            if (disaster.getAgencyId() > 0) {
                stmt.setInt(4, disaster.getAgencyId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER); // Allow null so it doesn't crash if Agency doesn't exist
            }

            stmt.executeUpdate();
        }
    }

    public List<Disaster> getAllDisasters() throws SQLException {
        List<Disaster> disasters = new ArrayList<>();
        String sql = "SELECT d.*, g.AGENCY_NAME FROM DISASTER d LEFT JOIN GOVT_AGENCY g ON d.AGENCY_ID = g.AGENCY_ID";

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

                // Finally delete the Disaster
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
