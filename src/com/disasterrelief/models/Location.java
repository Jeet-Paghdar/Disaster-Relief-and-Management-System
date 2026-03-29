package com.disasterrelief.models;

public class Location {

    private int locationId;
    private String name;
    private String address;
    private String type;
    private String pincode;
    private int capacity;

    public Location() {}

    public Location(int locationId, String name, String address,
                    String type, String pincode, int capacity) {
        this.locationId = locationId;
        setName(name);
        setAddress(address);
        setType(type);
        setPincode(pincode);
        setCapacity(capacity);
    }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

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
        this.type = type.trim();
    }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) {
        if (pincode == null || pincode.isBlank())
            throw new IllegalArgumentException("Pincode cannot be empty");
        this.pincode = pincode.trim();
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("Capacity cannot be negative");
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Location [locationId=" + locationId + ", name=" + name +
               ", capacity=" + capacity + "]";
    }
}
