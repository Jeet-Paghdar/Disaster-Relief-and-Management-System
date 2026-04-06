package com.disasterrelief.dao;

import com.disasterrelief.models.Inquirer;
import com.disasterrelief.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InquirerDAO {

    public boolean findAndMatchVictim(String inquirerFirst, String inquirerLast, String victimFirst, String victimLast, String victimPhone, String relationStr) throws SQLException {
        // 1. Check if the Victim exists First (Case-Insensitive Search + Unique Phone)
        String findVictimSql = "SELECT p.PERSON_ID as p_id, v.VICTIM_ID as v_id FROM PERSON p JOIN VICTIM v ON p.PERSON_ID = v.VICTIM_ID " +
                               "WHERE LOWER(p.FIRST_NAME) = LOWER(?) AND LOWER(p.LAST_NAME) = LOWER(?) AND p.PHONE_NUMBER = ?";
        int victimId = -1;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement findVictimStmt = conn.prepareStatement(findVictimSql)) {
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
                // 2. Check if this Inquirer already exists
                String findInqSql = "SELECT p.PERSON_ID FROM PERSON p JOIN INQUIRER i ON p.PERSON_ID = i.INQUIRER_ID " +
                                    "WHERE LOWER(p.FIRST_NAME) = LOWER(?) AND LOWER(p.LAST_NAME) = LOWER(?) LIMIT 1";
                int inquirerId = -1;
                try (PreparedStatement stmt = conn.prepareStatement(findInqSql)) {
                    stmt.setString(1, inquirerFirst.trim());
                    stmt.setString(2, inquirerLast.trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            inquirerId = rs.getInt("PERSON_ID");
                        }
                    }
                }

                // If Inquirer doesn't exist, create them newly
                if (inquirerId == -1) {
                    String addPersonSql = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, GENDER) VALUES (?, ?, 'Unknown')";
                    try (PreparedStatement stmt = conn.prepareStatement(addPersonSql, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, inquirerFirst);
                        stmt.setString(2, inquirerLast);
                        stmt.executeUpdate();
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) inquirerId = rs.getInt(1);
                        }
                    }
                    
                    String addInquirerSql = "INSERT INTO INQUIRER (INQUIRER_ID) VALUES (?)";
                    try (PreparedStatement stmt = conn.prepareStatement(addInquirerSql)) {
                        stmt.setInt(1, inquirerId);
                        stmt.executeUpdate();
                    }
                }

                // 3. Link them in the RELIEF_SERVICE (Match Registry)
                String addServiceSql = "INSERT INTO RELIEF_SERVICE (INQUIRER_ID, VICTIM_ID, INFO_PROVIDED) VALUES (?, ?, ?)";
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
        String sql = "SELECT rs.SERVICE_ID, p1.FIRST_NAME as inq_f, p1.LAST_NAME as inq_l, " +
                     "p2.FIRST_NAME as vic_f, p2.LAST_NAME as vic_l, rs.INFO_PROVIDED " +
                     "FROM RELIEF_SERVICE rs " +
                     "JOIN INQUIRER i ON rs.INQUIRER_ID = i.INQUIRER_ID " +
                     "JOIN PERSON p1 ON i.INQUIRER_ID = p1.PERSON_ID " +
                     "JOIN VICTIM v ON rs.VICTIM_ID = v.VICTIM_ID " +
                     "JOIN PERSON p2 ON v.VICTIM_ID = p2.PERSON_ID";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("SERVICE_ID");
                String inquirer = rs.getString("inq_f") + " " + rs.getString("inq_l");
                String victim = rs.getString("vic_f") + " " + rs.getString("vic_l");
                String relation = rs.getString("INFO_PROVIDED");
                matches.add(new Object[]{id, inquirer, victim, relation});
            }
        }
        return matches;
    }

    public void updateMatchById(int serviceId, String newInfo) throws SQLException {
        String sql = "UPDATE RELIEF_SERVICE SET INFO_PROVIDED = ? WHERE SERVICE_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newInfo);
            stmt.setInt(2, serviceId);
            stmt.executeUpdate();
        }
    }

    public void deleteMatchById(int serviceId) throws SQLException {
        String sql = "DELETE FROM RELIEF_SERVICE WHERE SERVICE_ID = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceId);
            stmt.executeUpdate();
        }
    }

    public void updateInquirerById(int inquirerId, String newFirst, String newLast, String newPhone, String newGender) throws SQLException {
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
                String deleteRS = "DELETE FROM RELIEF_SERVICE WHERE INQUIRER_ID = ?";
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

    public void updateMatch(String inqFirst, String inqLast, String vicFirst, String vicLast, String newInfo) throws SQLException {
        String sql = "UPDATE RELIEF_SERVICE SET INFO_PROVIDED = ? " +
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

    public void deleteMatchByName(String inqFirst, String inqLast, String vicFirst, String vicLast) throws SQLException {
        String sql = "DELETE FROM RELIEF_SERVICE WHERE INQUIRER_ID IN (SELECT PERSON_ID FROM PERSON WHERE FIRST_NAME=? AND LAST_NAME=?) " +
                     "AND VICTIM_ID IN (SELECT PERSON_ID FROM PERSON WHERE FIRST_NAME=? AND LAST_NAME=?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, inqFirst);
            stmt.setString(2, inqLast);
            stmt.setString(3, vicFirst);
            stmt.setString(4, vicLast);
            stmt.executeUpdate();
        }
    }

    public void updateInquirerDetails(String oldFirst, String oldLast, String newFirst, String newLast, String newPhone, String newGender) throws SQLException {
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
            while(rs.next()) {
                Inquirer inq = new Inquirer();
                inq.setPersonId(rs.getInt("PERSON_ID"));
                inq.setInquirerId(rs.getInt("INQUIRER_ID"));
                inq.setFirstName(rs.getString("FIRST_NAME"));
                inq.setLastName(rs.getString("LAST_NAME"));
                if (rs.getDate("DOB") != null) inq.setDob(rs.getDate("DOB").toLocalDate());
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
        String deleteMatchesSql = "DELETE FROM RELIEF_SERVICE WHERE INQUIRER_ID = ?";
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
