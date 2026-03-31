package com.disasterrelief.dao;

import com.disasterrelief.models.Supply;
import com.disasterrelief.utils.DBConnection;
import com.disasterrelief.exceptions.InvalidSupplyException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SupplyDAO {

    public void addSupply(Supply supply) throws SQLException, InvalidSupplyException {
        if (supply.getQuantity() < 0) {
            throw new InvalidSupplyException("Quantity cannot be negative!");
        }

        String sql = "INSERT INTO SUPPLY (ITEM_NAME, QUANTITY, TYPE, EXPIRY_DATE) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supply.getItemName());
            stmt.setInt(2, supply.getQuantity());
            stmt.setString(3, supply.getType());

            // Fix: Check if expiry date is null before inserting to prevent a crash
            stmt.setDate(4, supply.getExpiryDate() != null ? Date.valueOf(supply.getExpiryDate()) : null);

            stmt.executeUpdate();
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
        String sql = "DELETE FROM SUPPLY WHERE SUPPLY_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplyId);
            stmt.executeUpdate();
        }
    }
}
