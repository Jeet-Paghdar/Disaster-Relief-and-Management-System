package com.disasterrelief.dao;

import com.disasterrelief.models.MedicalRecord;
import com.disasterrelief.utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalDAO {

    public void addMedicalRecord(MedicalRecord record) throws SQLException {
        String sql = "INSERT INTO MEDICAL_RECORD (VICTIM_ID, PRESCRIPTIONS, TREATMENT_DETAILS, TREATMENT_DATE, WORKER_ID) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getVictimId());
            stmt.setString(2, record.getPrescriptions());
            stmt.setString(3, record.getTreatmentDetails());

            // Handle null treatment dates for new records
            stmt.setDate(4, record.getTreatmentDate() != null ? Date.valueOf(record.getTreatmentDate()) : null);

            stmt.setInt(5, record.getWorkerId());
            stmt.executeUpdate();
        }
    }

    public List<MedicalRecord> getMedicalRecords(int victimId) throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT m.*, v.BLOOD_TYPE FROM MEDICAL_RECORD m LEFT JOIN VICTIM v ON m.VICTIM_ID = v.VICTIM_ID WHERE m.VICTIM_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, victimId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    // Extract possible null dates from the database
                    Date treatmentSQLDate = rs.getDate("TREATMENT_DATE");
                    LocalDate treatmentDate = (treatmentSQLDate != null) ? treatmentSQLDate.toLocalDate() : null;

                    MedicalRecord r = new MedicalRecord(
                            rs.getInt("RECORD_NUMBER"),
                            rs.getInt("VICTIM_ID"),
                            rs.getString("BLOOD_TYPE"),
                            rs.getString("PRESCRIPTIONS"),
                            rs.getString("TREATMENT_DETAILS"),
                            treatmentDate,
                            rs.getInt("WORKER_ID")
                    );
                    records.add(r);
                }
            }
        }
        return records;
    }

    public List<MedicalRecord> getAllMedicalRecords() throws SQLException {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT m.*, v.BLOOD_TYPE FROM MEDICAL_RECORD m LEFT JOIN VICTIM v ON m.VICTIM_ID = v.VICTIM_ID";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Date treatmentSQLDate = rs.getDate("TREATMENT_DATE");
                LocalDate treatmentDate = (treatmentSQLDate != null) ? treatmentSQLDate.toLocalDate() : null;

                MedicalRecord r = new MedicalRecord(
                        rs.getInt("RECORD_NUMBER"),
                        rs.getInt("VICTIM_ID"),
                        rs.getString("BLOOD_TYPE"),
                        rs.getString("PRESCRIPTIONS"),
                        rs.getString("TREATMENT_DETAILS"),
                        treatmentDate,
                        rs.getInt("WORKER_ID")
                );
                records.add(r);
            }
        }
        return records;
    }

    public void updateMedicalRecord(MedicalRecord record) throws SQLException {
        String medSql = "UPDATE MEDICAL_RECORD SET PRESCRIPTIONS = ?, TREATMENT_DETAILS = ? WHERE RECORD_NUMBER = ?";
        String vicSql = "UPDATE VICTIM SET BLOOD_TYPE = ? WHERE VICTIM_ID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(medSql)) {
                    stmt.setString(1, record.getPrescriptions());
                    stmt.setString(2, record.getTreatmentDetails());
                    stmt.setInt(3, record.getRecordNumber());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt2 = conn.prepareStatement(vicSql)) {
                    stmt2.setString(1, record.getBloodType());
                    stmt2.setInt(2, record.getVictimId());
                    stmt2.executeUpdate();
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

    public void updateTreatment(int recordNumber, String treatmentDetails) throws SQLException {
        String sql = "UPDATE MEDICAL_RECORD SET TREATMENT_DETAILS = ? WHERE RECORD_NUMBER = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, treatmentDetails);
            stmt.setInt(2, recordNumber);
            stmt.executeUpdate();
        }
    }

    public void deleteMedicalRecord(int recordNumber) throws SQLException {
        String sql = "DELETE FROM MEDICAL_RECORD WHERE RECORD_NUMBER = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, recordNumber);
            stmt.executeUpdate();
        }
    }
}
