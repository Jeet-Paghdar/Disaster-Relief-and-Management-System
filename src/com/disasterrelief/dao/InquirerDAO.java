package com.disasterrelief.dao;


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

        // 2. Insert Inquirer and Link them
        String addPersonSql = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, GENDER) VALUES (?, ?, 'Unknown')";
        String addInquirerSql = "INSERT INTO INQUIRER (INQUIRER_ID) VALUES (?)";
        String addServiceSql = "INSERT INTO RELIEF_SERVICE (INQUIRER_ID, VICTIM_ID, INFO_PROVIDED) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int personId = 0;
                try (PreparedStatement stmt = conn.prepareStatement(addPersonSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, inquirerFirst);
                    stmt.setString(2, inquirerLast);
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) personId = rs.getInt(1);
                    }
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(addInquirerSql)) {
                    stmt.setInt(1, personId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(addServiceSql)) {
                    stmt.setInt(1, personId);
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

    public List<String[]> getMatchedRegistry() throws SQLException {
        List<String[]> matches = new ArrayList<>();
        String sql = "SELECT p1.FIRST_NAME as inq_f, p1.LAST_NAME as inq_l, " +
                     "p2.FIRST_NAME as vic_f, p2.LAST_NAME as vic_l, rs.INFO_PROVIDED " +
                     "FROM RELIEF_SERVICE rs " +
                     "JOIN INQUIRER i ON rs.INQUIRER_ID = i.INQUIRER_ID " +
                     "JOIN PERSON p1 ON i.INQUIRER_ID = p1.PERSON_ID " +
                     "JOIN VICTIM v ON rs.VICTIM_ID = v.VICTIM_ID " +
                     "JOIN PERSON p2 ON v.VICTIM_ID = p2.PERSON_ID";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String inquirer = rs.getString("inq_f") + " " + rs.getString("inq_l");
                String victim = rs.getString("vic_f") + " " + rs.getString("vic_l");
                String relation = rs.getString("INFO_PROVIDED");
                matches.add(new String[]{inquirer, victim, relation});
            }
        }
        return matches;
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
}
