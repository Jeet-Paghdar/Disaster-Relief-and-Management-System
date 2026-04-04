package com.disasterrelief.models;

import java.time.LocalDate;

public class Victim extends Person {

    private String addressBefore;
    private String addressAfter;
    private String injuryStatus;
    private LocalDate entryDate;
    private int disasterId;
    private String dietaryRestriction;

    public Victim() {
    }

    public Victim(int personId, String firstName, String lastName,
            LocalDate dob, int age, String gender, String email,
            String phoneNumber, String addressBefore, String addressAfter,
            String injuryStatus, LocalDate entryDate, int disasterId) {
        super(personId, firstName, lastName, dob, age, gender, email, phoneNumber);
        this.addressBefore = addressBefore;
        this.addressAfter = addressAfter;
        this.injuryStatus = injuryStatus;
        setEntryDate(entryDate);
        this.disasterId = disasterId;
    }

    public String getAddressBefore() {
        return addressBefore;
    }

    public void setAddressBefore(String addressBefore) {
        this.addressBefore = addressBefore;
    }

    public String getAddressAfter() {
        return addressAfter;
    }

    public void setAddressAfter(String addressAfter) {
        this.addressAfter = addressAfter;
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

    @Override
    public String toString() {
        return "Victim [victimId=" + getPersonId() + ", name=" + getFirstName() +
                " " + getLastName() + ", injuryStatus=" + injuryStatus + "]";
    }
}