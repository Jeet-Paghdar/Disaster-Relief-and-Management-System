package com.disasterrelief.models;

import java.time.LocalDate;

public class Victim extends Person {

    private int locationId;
    private String locationName; // Transient, for display
    private String injuryStatus;
    private LocalDate entryDate;
    private int disasterId;
    private String dietaryRestriction;
    private String bloodType;

    public Victim() {
    }

    public Victim(int personId, String firstName, String lastName,
            LocalDate dob, int age, String gender, String email,
            String phoneNumber, int locationId,
            String injuryStatus, LocalDate entryDate, int disasterId) {
        super(personId, firstName, lastName, dob, age, gender, email, phoneNumber);
        this.locationId = locationId;
        this.injuryStatus = injuryStatus;
        setEntryDate(entryDate);
        this.disasterId = disasterId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getInjuryStatus() {
        return injuryStatus;
    }

    public void setInjuryStatus(String injuryStatus) {
        this.injuryStatus = injuryStatus;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        if (entryDate == null)
            throw new IllegalArgumentException("Entry date cannot be null");
        this.entryDate = entryDate;
    }

    public int getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(int disasterId) {
        this.disasterId = disasterId;
    }

    public String getDietaryRestriction() {
        return dietaryRestriction;
    }

    public void setDietaryRestriction(String dietaryRestriction) {
        this.dietaryRestriction = dietaryRestriction;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    @Override
    public String toString() {
        return "Victim [victimId=" + getPersonId() + ", name=" + getFirstName() +
                " " + getLastName() + ", injuryStatus=" + injuryStatus + "]";
    }
}