package com.disasterrelief.models;

public class Location {

    private static final String[] VALID_TYPES = {"Hospital", "Shelter", "Supply Center", "Medical Camp", "Relief Center"};
    
    private int locationId;
    private String name;
    private String address;
    private String type;
    private String pincode;
    private int capacity;
    private int currentOccupancy;


    public Location() {}

    public Location(int locationId, String name, String address,
                    String type, String pincode, int capacity) {
        setLocationId(locationId);
        setName(name);
        setAddress(address);
        setType(type);
        setPincode(pincode);
        setCapacity(capacity);
    }

    public Location(int locationId, String name, String address,
                    String type, String pincode, int capacity, int currentOccupancy) {
        this(locationId, name, address, type, pincode, capacity);
        this.currentOccupancy = currentOccupancy;
    }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) {
        if (locationId <= 0)
            throw new IllegalArgumentException("Location ID must be a positive integer");
        this.locationId = locationId;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Location name cannot be empty");
        this.name = name.trim();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) {
        if (address == null || address.isBlank())
            throw new IllegalArgumentException("Address cannot be empty");
        this.address = address.trim();
    }

    public String getType() { return type; }
    public void setType(String type) {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException("Type cannot be empty");
        String trimmedType = type.trim();
        boolean isValid = false;
        for (String valid : VALID_TYPES) {
            if (valid.equals(trimmedType)) {
                isValid = true;
                break;
            }
        }
        if (!isValid)
            throw new IllegalArgumentException("Type must be one of: Hospital, Shelter, Supply Center, Medical Camp, Relief Center");
        this.type = trimmedType;
    }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) {
        if (pincode == null || pincode.isBlank())
            throw new IllegalArgumentException("Pincode cannot be empty");
        String trimmedPincode = pincode.trim();
        if (!trimmedPincode.matches("\\d{5,6}"))
            throw new IllegalArgumentException("Pincode must be 5-6 digits");
        this.pincode = trimmedPincode;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be a positive integer");
        this.capacity = capacity;
    }

    public int getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(int currentOccupancy) { this.currentOccupancy = currentOccupancy; }



    @Override
    public String toString() {
        return name + " (" + address + ")";
    }
}
