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
        String victimSQL = "INSERT INTO VICTIM (VICTIM_ID, ADDRESS_BEFORE, ADDRESS_AFTER, INJURY_STATUS, ENTRY_DATE, DISASTER_ID) VALUES (?, ?, ?, ?, ?, ?)";

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
                    victimStmt.executeUpdate();
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
        String sql = "SELECT p.*, v.ADDRESS_BEFORE, v.ADDRESS_AFTER, v.INJURY_STATUS, v.ENTRY_DATE, v.DISASTER_ID "
                + "FROM PERSON p JOIN VICTIM v ON p.PERSON_ID = v.VICTIM_ID";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                victims.add(mapResultSetToVictim(rs));
            }
        }
        return victims;
    }

    public List<Victim> searchVictimsByName(String firstName) throws SQLException {
        List<Victim> victims = new ArrayList<>();
        String sql = "SELECT p.*, v.ADDRESS_BEFORE, v.ADDRESS_AFTER, v.INJURY_STATUS, v.ENTRY_DATE, v.DISASTER_ID "
                + "FROM PERSON p JOIN VICTIM v ON p.PERSON_ID = v.VICTIM_ID "
                + "WHERE p.FIRST_NAME = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    victims.add(mapResultSetToVictim(rs));
                }
            }
        }

        return victims;
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
        String deleteVictimSQL = "DELETE FROM VICTIM WHERE VICTIM_ID = ?";
        String deletePersonSQL = "DELETE FROM PERSON WHERE PERSON_ID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement victimStmt = conn.prepareStatement(deleteVictimSQL); PreparedStatement personStmt = conn.prepareStatement(deletePersonSQL)) {

                victimStmt.setInt(1, victimId);
                victimStmt.executeUpdate();

                personStmt.setInt(1, victimId);
                personStmt.executeUpdate();

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

        return new Victim(
                rs.getInt("PERSON_ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                dob,
                rs.getString("GENDER"),
                rs.getString("EMAIL"),
                rs.getString("PHONE_NUMBER"),
                rs.getString("ADDRESS_BEFORE"),
                rs.getString("ADDRESS_AFTER"),
                rs.getString("INJURY_STATUS"),
                entryDate,
                rs.getInt("DISASTER_ID")
        );
    }
}
