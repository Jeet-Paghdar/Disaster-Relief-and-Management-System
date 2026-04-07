package com.disasterrelief.dao;

import com.disasterrelief.models.Victim;
import com.disasterrelief.utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VictimDAO {

    public void addVictim(Victim victim) throws SQLException {
        String personSQL = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, DOB, GENDER, EMAIL, PHONE_NUMBER) VALUES (?, ?, ?, ?, ?, ?)";
        String victimSQL = "INSERT INTO VICTIM (VICTIM_ID, ADDRESS_BEFORE, ADDRESS_AFTER, INJURY_STATUS, ENTRY_DATE, DISASTER_ID, BLOOD_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement personStmt = conn.prepareStatement(personSQL, Statement.RETURN_GENERATED_KEYS)) {

                personStmt.setString(1, victim.getFirstName());
                personStmt.setString(2, victim.getLastName());
                personStmt.setDate(3, victim.getDob() != null ? Date.valueOf(victim.getDob()) : null);
                personStmt.setString(4, victim.getGender());
                personStmt.setString(5, victim.getEmail());
                personStmt.setString(6, victim.getPhoneNumber());
                personStmt.executeUpdate();

                int personId;
                try (ResultSet rs = personStmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to get generated person ID");
                    }
                    personId = rs.getInt(1);
                }

                try (PreparedStatement victimStmt = conn.prepareStatement(victimSQL)) {
                    victimStmt.setInt(1, personId);
                    victimStmt.setString(2, victim.getAddressBefore());
                    victimStmt.setString(3, victim.getAddressAfter());
                    victimStmt.setString(4, victim.getInjuryStatus());
                    victimStmt.setDate(5, victim.getEntryDate() != null ? Date.valueOf(victim.getEntryDate()) : null);
                    victimStmt.setInt(6, victim.getDisasterId());
                    victimStmt.setString(7, victim.getBloodType());
                    victimStmt.executeUpdate();
                }

                // Dietary Restriction Insertion
                if (victim.getDietaryRestriction() != null && !victim.getDietaryRestriction().equals("None")) {
                    String dietSql = "INSERT INTO VICTIM_DIETARY_RESTRICTIONS (VICTIM_ID, RESTRICTION_TYPE) VALUES (?, ?)";
                    try (PreparedStatement dietStmt = conn.prepareStatement(dietSql)) {
                        dietStmt.setInt(1, personId);
                        String dietValue = victim.getDietaryRestriction();
                        if (dietValue.length() > 50) {
                            dietValue = dietValue.substring(0, 50);
                        }
                        dietStmt.setString(2, dietValue);
                        dietStmt.executeUpdate();
                    }
                }

                conn.commit();
                victim.setPersonId(personId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Victim> getAllVictims() throws SQLException {
        List<Victim> victims = new ArrayList<>();
        String sql = "SELECT p.*, fn_calculate_age(p.DOB) AS AGE, v.ADDRESS_BEFORE, v.ADDRESS_AFTER, v.INJURY_STATUS, v.ENTRY_DATE, v.DISASTER_ID, v.BLOOD_TYPE, dr.RESTRICTION_TYPE "
                + "FROM PERSON p JOIN VICTIM v ON p.PERSON_ID = v.VICTIM_ID "
                + "LEFT JOIN VICTIM_DIETARY_RESTRICTIONS dr ON v.VICTIM_ID = dr.VICTIM_ID";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                victims.add(mapResultSetToVictim(rs));
            }
        }
        return victims;
    }

    public List<Victim> searchVictimsByName(String firstName) throws SQLException {
        List<Victim> victims = new ArrayList<>();
        String sql = "SELECT p.*, fn_calculate_age(p.DOB) AS AGE, v.ADDRESS_BEFORE, v.ADDRESS_AFTER, v.INJURY_STATUS, v.ENTRY_DATE, v.DISASTER_ID, v.BLOOD_TYPE, dr.RESTRICTION_TYPE "
                + "FROM PERSON p JOIN VICTIM v ON p.PERSON_ID = v.VICTIM_ID "
                + "LEFT JOIN VICTIM_DIETARY_RESTRICTIONS dr ON v.VICTIM_ID = dr.VICTIM_ID "
                + "WHERE p.FIRST_NAME LIKE ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + firstName + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    victims.add(mapResultSetToVictim(rs));
                }
            }
        }

        return victims;
    }

    public void updateVictim(Victim victim) throws SQLException {
        String personSQL = "UPDATE PERSON SET FIRST_NAME = ?, LAST_NAME = ?, DOB = ?, AGE = ?, GENDER = ?, EMAIL = ?, PHONE_NUMBER = ? WHERE PERSON_ID = ?";
        String victimSQL = "UPDATE VICTIM SET ADDRESS_BEFORE = ?, ADDRESS_AFTER = ?, INJURY_STATUS = ?, ENTRY_DATE = ?, DISASTER_ID = ? WHERE VICTIM_ID = ?";
        String deleteDietSQL = "DELETE FROM VICTIM_DIETARY_RESTRICTIONS WHERE VICTIM_ID = ?";
        String insertDietSQL = "INSERT INTO VICTIM_DIETARY_RESTRICTIONS (VICTIM_ID, RESTRICTION_TYPE) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // Update PERSON table
                try (PreparedStatement personStmt = conn.prepareStatement(personSQL)) {
                    personStmt.setString(1, victim.getFirstName());
                    personStmt.setString(2, victim.getLastName());
                    personStmt.setDate(3, victim.getDob() != null ? Date.valueOf(victim.getDob()) : null);
                    personStmt.setInt(4, victim.getAge());
                    personStmt.setString(5, victim.getGender());
                    personStmt.setString(6, victim.getEmail());
                    personStmt.setString(7, victim.getPhoneNumber());
                    personStmt.setInt(8, victim.getPersonId());
                    personStmt.executeUpdate();
                }

                // Update VICTIM table
                try (PreparedStatement victimStmt = conn.prepareStatement(victimSQL)) {
                    victimStmt.setString(1, victim.getAddressBefore());
                    victimStmt.setString(2, victim.getAddressAfter());
                    victimStmt.setString(3, victim.getInjuryStatus());
                    victimStmt.setDate(4, victim.getEntryDate() != null ? Date.valueOf(victim.getEntryDate()) : null);
                    victimStmt.setInt(5, victim.getDisasterId());
                    victimStmt.setInt(6, victim.getPersonId());
                    victimStmt.executeUpdate();
                }

                // Update Dietary Restrictions (Delete and Re-insert is safest)
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteDietSQL)) {
                    deleteStmt.setInt(1, victim.getPersonId());
                    deleteStmt.executeUpdate();
                }

                if (victim.getDietaryRestriction() != null && !victim.getDietaryRestriction().equals("None")) {
                    try (PreparedStatement dietStmt = conn.prepareStatement(insertDietSQL)) {
                        dietStmt.setInt(1, victim.getPersonId());
                        dietStmt.setString(2, victim.getDietaryRestriction());
                        dietStmt.executeUpdate();
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

    public void updateInjuryStatus(int victimId, String injuryStatus) throws SQLException {
        String sql = "UPDATE VICTIM SET INJURY_STATUS = ? WHERE VICTIM_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, injuryStatus);
            stmt.setInt(2, victimId);
            stmt.executeUpdate();
        }
    }

    public void deleteVictim(int victimId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete Medical Records
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MEDICAL_RECORD WHERE VICTIM_ID = ?")) {
                    stmt.setInt(1, victimId);
                    stmt.executeUpdate();
                }

                // 2. Delete Dietary Restrictions
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM VICTIM_DIETARY_RESTRICTIONS WHERE VICTIM_ID = ?")) {
                    stmt.setInt(1, victimId);
                    stmt.executeUpdate();
                }

                // 3. Delete Family Relations
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM FAMILY_RELATION WHERE VICTIM1_ID = ? OR VICTIM2_ID = ?")) {
                    stmt.setInt(1, victimId);
                    stmt.setInt(2, victimId);
                    stmt.executeUpdate();
                }

                // 4. Delete Supply Links
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM VICTIM_SUPPLY WHERE VICTIM_ID = ?")) {
                    stmt.setInt(1, victimId);
                    stmt.executeUpdate();
                }

                // 5. Delete Relief Services
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM RELIEF_SERVICE WHERE VICTIM_ID = ?")) {
                    stmt.setInt(1, victimId);
                    stmt.executeUpdate();
                }

                // 6. Delete Victim Record
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM VICTIM WHERE VICTIM_ID = ?")) {
                    stmt.setInt(1, victimId);
                    stmt.executeUpdate();
                }

                // 7. Finally delete from PERSON
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM PERSON WHERE PERSON_ID = ?")) {
                    stmt.setInt(1, victimId);
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

    private Victim mapResultSetToVictim(ResultSet rs) throws SQLException {
        Date dobDate = rs.getDate("DOB");
        LocalDate dob = (dobDate != null) ? dobDate.toLocalDate() : null;

        Date entryDateSQL = rs.getDate("ENTRY_DATE");
        LocalDate entryDate = (entryDateSQL != null) ? entryDateSQL.toLocalDate() : null;

        Victim victim = new Victim(
                rs.getInt("PERSON_ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                dob,
                rs.getInt("AGE"),
                rs.getString("GENDER"),
                rs.getString("EMAIL"),
                rs.getString("PHONE_NUMBER"),
                rs.getString("ADDRESS_BEFORE"),
                rs.getString("ADDRESS_AFTER"),
                rs.getString("INJURY_STATUS"),
                entryDate,
                rs.getInt("DISASTER_ID")
        );
        victim.setBloodType(rs.getString("BLOOD_TYPE"));

        String dietary = rs.getString("RESTRICTION_TYPE");
        if (dietary != null) {
            victim.setDietaryRestriction(dietary);
        } else {
            victim.setDietaryRestriction("None");
        }

        return victim;
    }

    public String getBloodType(int victimId) throws SQLException {
        String sql = "SELECT BLOOD_TYPE FROM VICTIM WHERE VICTIM_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, victimId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("BLOOD_TYPE");
                }
            }
        }
        return null;
    }
}
