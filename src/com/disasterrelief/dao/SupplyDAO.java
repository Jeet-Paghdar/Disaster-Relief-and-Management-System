package com.disasterrelief.dao;

import com.disasterrelief.exceptions.InvalidSupplyException;
import com.disasterrelief.models.Supply;
import com.disasterrelief.utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupplyDAO {

    public int addSupplyWithLocation(Supply supply, int locationId) throws SQLException, InvalidSupplyException {
        if (supply.getQuantity() < 0) {
            throw new InvalidSupplyException("Quantity cannot be negative!");
        }

        String supplySql = "INSERT INTO SUPPLY (ITEM_NAME, QUANTITY, TYPE, EXPIRY_DATE) VALUES (?, ?, ?, ?)";
        String linkSql = "INSERT INTO LOCATION_SUPPLY (LOCATION_ID, SUPPLY_ID, QUANTITY_STORED) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement supplyStmt = conn.prepareStatement(supplySql, Statement.RETURN_GENERATED_KEYS)) {
                supplyStmt.setString(1, supply.getItemName());
                supplyStmt.setInt(2, supply.getQuantity());
                supplyStmt.setString(3, supply.getType());
                supplyStmt.setDate(4, supply.getExpiryDate() != null ? Date.valueOf(supply.getExpiryDate()) : null);
                supplyStmt.executeUpdate();

                int supplyId = 0;
                try (ResultSet rs = supplyStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        supplyId = rs.getInt(1);
                    }
                }

                try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                    linkStmt.setInt(1, locationId);
                    linkStmt.setInt(2, supplyId);
                    linkStmt.setInt(3, supply.getQuantity());
                    linkStmt.executeUpdate();
                }

                conn.commit();
                return supplyId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Supply> getAllSupplies() throws SQLException {
        List<Supply> supplies = new ArrayList<>();
        String sql = "SELECT * FROM SUPPLY";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Fix: Safely extract date from database preventing NPEs
                Date expirySQL = rs.getDate("EXPIRY_DATE");
                LocalDate expiryDate = (expirySQL != null) ? expirySQL.toLocalDate() : null;

                Supply s = new Supply(
                        rs.getInt("SUPPLY_ID"),
                        rs.getString("ITEM_NAME"),
                        rs.getInt("QUANTITY"),
                        rs.getString("TYPE"),
                        expiryDate
                );
                supplies.add(s);
            }
        }
        return supplies;
    }

    public void updateQuantity(int supplyId, int quantity) throws SQLException, InvalidSupplyException {
        if (quantity < 0) {
            throw new InvalidSupplyException("Quantity cannot be negative!");
        }

        String sql = "UPDATE SUPPLY SET QUANTITY = ? WHERE SUPPLY_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, supplyId);
            stmt.executeUpdate();
        }
    }

    public void deleteSupply(int supplyId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete from LOCATION_SUPPLY
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM LOCATION_SUPPLY WHERE SUPPLY_ID = ?")) {
                    stmt.setInt(1, supplyId);
                    stmt.executeUpdate();
                }

                // 2. Delete from VENDOR_SUPPLY
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM VENDOR_SUPPLY WHERE SUPPLY_ID = ?")) {
                    stmt.setInt(1, supplyId);
                    stmt.executeUpdate();
                }

                // 3. Delete from VICTIM_SUPPLY
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM VICTIM_SUPPLY WHERE SUPPLY_ID = ?")) {
                    stmt.setInt(1, supplyId);
                    stmt.executeUpdate();
                }

                // 4. Finally delete from SUPPLY
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM SUPPLY WHERE SUPPLY_ID = ?")) {
                    stmt.setInt(1, supplyId);
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
