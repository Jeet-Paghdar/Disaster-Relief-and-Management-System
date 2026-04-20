package com.disasterrelief.dao;

import com.disasterrelief.models.Inquirer;
import com.disasterrelief.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InquirerDAO {

    public boolean findAndMatchVictim(String inquirerFirst, String inquirerLast, String inqPhone, String inqGender,
            String victimFirst, String victimLast, String victimPhone, String relationStr) throws SQLException {
        // 1. Check if the Victim exists First (Case-Insensitive Search + Unique Phone)
        String findVictimSql = "SELECT p.PERSON_ID as p_id, v.VICTIM_ID as v_id FROM PERSON p JOIN VICTIM v ON p.PERSON_ID = v.VICTIM_ID "
                +
                "WHERE LOWER(p.FIRST_NAME) = LOWER(?) AND LOWER(p.LAST_NAME) = LOWER(?) AND p.PHONE_NUMBER = ?";
        int victimId = -1;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement findVictimStmt = conn.prepareStatement(findVictimSql)) {
            findVictimStmt.setString(1, victimFirst.trim());
            findVictimStmt.setString(2, victimLast.trim());
            findVictimStmt.setString(3, victimPhone.trim());

            try (ResultSet rs = findVictimStmt.executeQuery()) {
                if (rs.next()) {
                    victimId = rs.getInt("v_id");
                }
            }
        }

        if (victimId == -1) {
            return false; // Victim not found
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 2. Check if this person already exists anywhere in PERSON table (by name + phone)
                String findPersonSql = "SELECT PERSON_ID FROM PERSON " +
                        "WHERE LOWER(FIRST_NAME) = LOWER(?) AND LOWER(LAST_NAME) = LOWER(?) AND PHONE_NUMBER = ? LIMIT 1";
                int inquirerId = -1;
                try (PreparedStatement stmt = conn.prepareStatement(findPersonSql)) {
                    stmt.setString(1, inquirerFirst.trim());
                    stmt.setString(2, inquirerLast.trim());
                    stmt.setString(3, inqPhone.trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            inquirerId = rs.getInt("PERSON_ID");
                        }
                    }
                }

                if (inquirerId == -1) {
                    // Person does not exist at all — create a new PERSON record
                    String addPersonSql = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, PHONE_NUMBER, GENDER) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(addPersonSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, inquirerFirst);
                        stmt.setString(2, inquirerLast);
                        stmt.setString(3, inqPhone);
                        stmt.setString(4, inqGender);
                        stmt.executeUpdate();
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next())
                                inquirerId = rs.getInt(1);
                        }
                    }
                }

                // Check if this person is already registered as an Inquirer
                boolean alreadyInquirer = false;
                String checkInqSql = "SELECT 1 FROM INQUIRER WHERE INQUIRER_ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(checkInqSql)) {
                    stmt.setInt(1, inquirerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        alreadyInquirer = rs.next();
                    }
                }

                // If not yet an Inquirer, register them (reuse existing PERSON_ID)
                if (!alreadyInquirer) {
                    String addInquirerSql = "INSERT INTO INQUIRER (INQUIRER_ID) VALUES (?)";
                    try (PreparedStatement stmt = conn.prepareStatement(addInquirerSql)) {
                        stmt.setInt(1, inquirerId);
                        stmt.executeUpdate();
                    }
                }

                // 3. Link them in the MATCH_REGISTRY
                String addServiceSql = "INSERT INTO MATCH_REGISTRY (INQUIRER_ID, VICTIM_ID, INFO_PROVIDED) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(addServiceSql)) {
                    stmt.setInt(1, inquirerId);
                    stmt.setInt(2, victimId);
                    stmt.setString(3, "Matched relation: " + relationStr);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Object[]> getMatchedRegistry() throws SQLException {
        List<Object[]> matches = new ArrayList<>();
        String sql = "SELECT rs.MATCH_ID, p1.FIRST_NAME as inq_f, p1.LAST_NAME as inq_l, " +
                "p2.FIRST_NAME as vic_f, p2.LAST_NAME as vic_l, CONCAT(l.NAME, ' (', l.ADDRESS, ')') as vic_loc, rs.INFO_PROVIDED "
                +
                "FROM MATCH_REGISTRY rs " +
                "JOIN INQUIRER i ON rs.INQUIRER_ID = i.INQUIRER_ID " +
                "JOIN PERSON p1 ON i.INQUIRER_ID = p1.PERSON_ID " +
                "JOIN VICTIM v ON rs.VICTIM_ID = v.VICTIM_ID " +
                "JOIN PERSON p2 ON v.VICTIM_ID = p2.PERSON_ID " +
                "LEFT JOIN LOCATION l ON v.LOCATION_ID = l.LOCATION_ID";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("MATCH_ID");
                String inquirer = rs.getString("inq_f") + " " + rs.getString("inq_l");
                String victim = rs.getString("vic_f") + " " + rs.getString("vic_l");
                String location = rs.getString("vic_loc");
                String relation = rs.getString("INFO_PROVIDED");
                matches.add(new Object[] { id, inquirer, victim, location, relation });
            }
        }
        return matches;
    }

    public void updateMatchById(int matchId, String newInfo) throws SQLException {
        String sql = "UPDATE MATCH_REGISTRY SET INFO_PROVIDED = ? WHERE MATCH_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newInfo);
            stmt.setInt(2, matchId);
            stmt.executeUpdate();
        }
    }

    public void deleteMatchById(int matchId) throws SQLException {
        String sql = "DELETE FROM MATCH_REGISTRY WHERE MATCH_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, matchId);
            stmt.executeUpdate();
        }
    }

    public void updateInquirerById(int inquirerId, String newFirst, String newLast, String newPhone, String newGender)
            throws SQLException {
        String sql = "UPDATE PERSON SET FIRST_NAME=?, LAST_NAME=?, PHONE_NUMBER=?, GENDER=? WHERE PERSON_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newFirst);
            stmt.setString(2, newLast);
            stmt.setString(3, newPhone);
            stmt.setString(4, newGender);
            stmt.setInt(5, inquirerId);
            stmt.executeUpdate();
        }
    }

    public void deleteInquirerById(int inquirerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete associated relief service records
                String deleteRS = "DELETE FROM MATCH_REGISTRY WHERE INQUIRER_ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteRS)) {
                    stmt.setInt(1, inquirerId);
                    stmt.executeUpdate();
                }

                // 2. Delete Inquirer record
                String deleteInq = "DELETE FROM INQUIRER WHERE INQUIRER_ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteInq)) {
                    stmt.setInt(1, inquirerId);
                    stmt.executeUpdate();
                }

                // 3. Delete Person record
                String deletePerson = "DELETE FROM PERSON WHERE PERSON_ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deletePerson)) {
                    stmt.setInt(1, inquirerId);
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

    public void updateMatch(String inqFirst, String inqLast, String vicFirst, String vicLast, String newInfo)
            throws SQLException {
        String sql = "UPDATE MATCH_REGISTRY SET INFO_PROVIDED = ? " +
                "WHERE INQUIRER_ID IN (SELECT PERSON_ID FROM PERSON WHERE FIRST_NAME=? AND LAST_NAME=?) " +
                "AND VICTIM_ID IN (SELECT PERSON_ID FROM PERSON WHERE FIRST_NAME=? AND LAST_NAME=?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newInfo);
            stmt.setString(2, inqFirst);
            stmt.setString(3, inqLast);
            stmt.setString(4, vicFirst);
            stmt.setString(5, vicLast);
            stmt.executeUpdate();
        }
    }

    public void deleteMatchByName(String inqFirst, String inqLast, String vicFirst, String vicLast)
            throws SQLException {
        String sql = "DELETE FROM MATCH_REGISTRY WHERE INQUIRER_ID IN (SELECT PERSON_ID FROM PERSON WHERE FIRST_NAME=? AND LAST_NAME=?) "
                +
                "AND VICTIM_ID IN (SELECT PERSON_ID FROM PERSON WHERE FIRST_NAME=? AND LAST_NAME=?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, inqFirst);
            stmt.setString(2, inqLast);
            stmt.setString(3, vicFirst);
            stmt.setString(4, vicLast);
            stmt.executeUpdate();
        }
    }

    public void updateInquirerDetails(String oldFirst, String oldLast, String newFirst, String newLast, String newPhone,
            String newGender) throws SQLException {
        String sql = "UPDATE PERSON SET FIRST_NAME=?, LAST_NAME=?, PHONE_NUMBER=?, GENDER=? " +
                "WHERE PERSON_ID IN (SELECT INQUIRER_ID FROM INQUIRER) " +
                "AND FIRST_NAME=? AND LAST_NAME=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newFirst);
            stmt.setString(2, newLast);
            stmt.setString(3, newPhone);
            stmt.setString(4, newGender);
            stmt.setString(5, oldFirst);
            stmt.setString(6, oldLast);
            stmt.executeUpdate();
        }
    }

    public List<Inquirer> getAllInquirers() throws SQLException {
        List<Inquirer> list = new ArrayList<>();
        String sql = "SELECT p.PERSON_ID, p.FIRST_NAME, p.LAST_NAME, p.DOB, p.GENDER, p.EMAIL, p.PHONE_NUMBER, " +
                "i.INQUIRER_ID, i.INQUIRY_TIMESTAMP " +
                "FROM INQUIRER i JOIN PERSON p ON i.INQUIRER_ID = p.PERSON_ID";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Inquirer inq = new Inquirer();
                inq.setPersonId(rs.getInt("PERSON_ID"));
                inq.setInquirerId(rs.getInt("INQUIRER_ID"));
                inq.setFirstName(rs.getString("FIRST_NAME"));
                inq.setLastName(rs.getString("LAST_NAME"));
                if (rs.getDate("DOB") != null)
                    inq.setDob(rs.getDate("DOB").toLocalDate());
                inq.setGender(rs.getString("GENDER"));
                inq.setEmail(rs.getString("EMAIL"));
                inq.setPhoneNumber(rs.getString("PHONE_NUMBER"));
                if (rs.getTimestamp("INQUIRY_TIMESTAMP") != null) {
                    inq.setInquiryDate(rs.getTimestamp("INQUIRY_TIMESTAMP").toLocalDateTime().toLocalDate());
                } else {
                    inq.setInquiryDate(java.time.LocalDate.now());
                }
                list.add(inq);
            }
        }
        return list;
    }

    public void deleteInquirer(int inquirerId) throws SQLException {
        String deleteMatchesSql = "DELETE FROM MATCH_REGISTRY WHERE INQUIRER_ID = ?";
        String deleteInquirerSql = "DELETE FROM INQUIRER WHERE INQUIRER_ID = ?";
        String deletePersonSql = "DELETE FROM PERSON WHERE PERSON_ID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delMatches = conn.prepareStatement(deleteMatchesSql);
                    PreparedStatement delInq = conn.prepareStatement(deleteInquirerSql);
                    PreparedStatement delPerson = conn.prepareStatement(deletePersonSql)) {

                delMatches.setInt(1, inquirerId);
                delMatches.executeUpdate();

                delInq.setInt(1, inquirerId);
                delInq.executeUpdate();

                delPerson.setInt(1, inquirerId);
                delPerson.executeUpdate();

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
