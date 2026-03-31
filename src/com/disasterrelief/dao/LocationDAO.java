package com.disasterrelief.dao;

import com.disasterrelief.models.Location;
import com.disasterrelief.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO {

    public void addLocation(Location location) throws SQLException {
        String sql = "INSERT INTO LOCATION (NAME, ADDRESS, TYPE, PINCODE, CAPACITY) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location.getName());
            stmt.setString(2, location.getAddress());
            stmt.setString(3, location.getType());
            stmt.setString(4, location.getPincode());
            stmt.setInt(5, location.getCapacity());
            stmt.executeUpdate();
        }
    }

    public List<Location> getAllLocations() throws SQLException {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM LOCATION";

        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Location l = new Location(
                        rs.getInt("LOCATION_ID"),
                        rs.getString("NAME"),
                        rs.getString("ADDRESS"),
                        rs.getString("TYPE"),
                        rs.getString("PINCODE"),
                        rs.getInt("CAPACITY")
                );
                locations.add(l);
            }
        }
        return locations;
    }

    public void updateCapacity(int locationId, int capacity) throws SQLException {
        String sql = "UPDATE LOCATION SET CAPACITY = ? WHERE LOCATION_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, capacity);
            stmt.setInt(2, locationId);
            stmt.executeUpdate();
        }
    }

    public void deleteLocation(int locationId) throws SQLException {
        String sql = "DELETE FROM LOCATION WHERE LOCATION_ID = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, locationId);
            stmt.executeUpdate();
        }
    }
}
