package com.disasterrelief.dao;

import com.disasterrelief.models.SocialWorker;
import com.disasterrelief.utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkerDAO {

    public void addWorker(SocialWorker worker) throws SQLException {
        String personSQL = "INSERT INTO PERSON (FIRST_NAME, LAST_NAME, DOB, GENDER, EMAIL, PHONE_NUMBER) VALUES (?, ?, ?, ?, ?, ?)";
        String workerSQL = "INSERT INTO SOCIAL_WORKER (PERSON_ID, SPECIALISATION, WORK_SHIFT) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement personStmt = conn.prepareStatement(personSQL, Statement.RETURN_GENERATED_KEYS)) {

                personStmt.setString(1, worker.getFirstName());
                personStmt.setString(2, worker.getLastName());
                personStmt.setDate(3, worker.getDob() != null ? Date.valueOf(worker.getDob()) : null);
                personStmt.setString(4, worker.getGender());
                personStmt.setString(5, worker.getEmail());
                personStmt.setString(6, worker.getPhoneNumber());
                personStmt.executeUpdate();

                int personId;
                try (ResultSet rs = personStmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to get generated person ID");
                    }
                    personId = rs.getInt(1);
                }

                try (PreparedStatement workerStmt = conn.prepareStatement(workerSQL)) {
                    workerStmt.setInt(1, personId);
                    workerStmt.setString(2, worker.getSpecialisation());
                    workerStmt.setString(3, worker.getWorkShift());
                    workerStmt.executeUpdate();
                }

                conn.commit();
                worker.setPersonId(personId);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<SocialWorker> getAllWorkers() throws SQLException {
        List<SocialWorker> workers = new ArrayList<>();
        String sql = "SELECT p.*, fn_calculate_age(p.DOB) AS AGE, sw.EMPLOYEE_ID, sw.SPECIALISATION, sw.WORK_SHIFT "
                + "FROM PERSON p JOIN SOCIAL_WORKER sw ON p.PERSON_ID = sw.PERSON_ID";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Date dobDate = rs.getDate("DOB");
                LocalDate dob = (dobDate != null) ? dobDate.toLocalDate() : null;

                SocialWorker w = new SocialWorker(
                        rs.getInt("PERSON_ID"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME"),
                        dob,
                        rs.getInt("AGE"),
                        rs.getString("GENDER"),
                        rs.getString("EMAIL"),
                        rs.getString("PHONE_NUMBER"),
                        rs.getInt("EMPLOYEE_ID"),
                        rs.getString("SPECIALISATION"),
                        rs.getString("WORK_SHIFT")
                );
                workers.add(w);
            }
        }
        return workers;
    }

    public void updateWorker(SocialWorker worker) throws SQLException {
        String personSQL = "UPDATE PERSON SET FIRST_NAME = ?, LAST_NAME = ?, DOB = ?, GENDER = ?, EMAIL = ?, PHONE_NUMBER = ? WHERE PERSON_ID = ?";
        String workerSQL = "UPDATE SOCIAL_WORKER SET SPECIALISATION = ?, WORK_SHIFT = ? WHERE EMPLOYEE_ID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // Update PERSON table
                try (PreparedStatement personStmt = conn.prepareStatement(personSQL)) {
                    personStmt.setString(1, worker.getFirstName());
                    personStmt.setString(2, worker.getLastName());
                    personStmt.setDate(3, worker.getDob() != null ? Date.valueOf(worker.getDob()) : null);
                    personStmt.setString(4, worker.getGender());
                    personStmt.setString(5, worker.getEmail());
                    personStmt.setString(6, worker.getPhoneNumber());
                    personStmt.setInt(7, worker.getPersonId());
                    personStmt.executeUpdate();
                }

                // Update SOCIAL_WORKER table
                try (PreparedStatement workerStmt = conn.prepareStatement(workerSQL)) {
                    workerStmt.setString(1, worker.getSpecialisation());
                    workerStmt.setString(2, worker.getWorkShift());
                    workerStmt.setInt(3, worker.getEmployeeId());
                    workerStmt.executeUpdate();
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

    public void updateWorkShift(int employeeId, String workShift) throws SQLException {
        String sql = "UPDATE SOCIAL_WORKER SET WORK_SHIFT = ? WHERE EMPLOYEE_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, workShift);
            stmt.setInt(2, employeeId);
            stmt.executeUpdate();
        }
    }

    public void deleteWorker(int employeeId) throws SQLException {
        String getPersonIdSQL = "SELECT PERSON_ID FROM SOCIAL_WORKER WHERE EMPLOYEE_ID = ?";
        String deleteWorkerSQL = "DELETE FROM SOCIAL_WORKER WHERE EMPLOYEE_ID = ?";
        String deletePersonSQL = "DELETE FROM PERSON WHERE PERSON_ID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement getPersonStmt = conn.prepareStatement(getPersonIdSQL); PreparedStatement workerStmt = conn.prepareStatement(deleteWorkerSQL); PreparedStatement personStmt = conn.prepareStatement(deletePersonSQL)) {

                // Retrieve mapping ID first
                getPersonStmt.setInt(1, employeeId);
                int personId = -1;
                try (ResultSet rs = getPersonStmt.executeQuery()) {
                    if (rs.next()) {
                        personId = rs.getInt("PERSON_ID");
                    }
                }

                // If found, execute the deletions safely
                if (personId != -1) {
                    workerStmt.setInt(1, employeeId);
                    workerStmt.executeUpdate();

                    personStmt.setInt(1, personId);
                    personStmt.executeUpdate();

                    conn.commit();
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
