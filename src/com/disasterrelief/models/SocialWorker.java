package com.disasterrelief.models;

import java.time.LocalDate;

public class SocialWorker extends Person {

    private int employeeId;
    private String specialisation;
    private String workShift;

    public SocialWorker() {
    }

    public SocialWorker(int personId, String firstName, String lastName,
                        LocalDate dob, int age, String gender, String email,
                        String phoneNumber, int employeeId,
                        String specialisation, String workShift) {

        super(personId, firstName, lastName, dob, age, gender, email, phoneNumber);

        this.employeeId = employeeId;
        this.specialisation = specialisation;
        this.workShift = workShift;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getSpecialisation() {
        return specialisation;
    }

    public void setSpecialisation(String specialisation) {
        this.specialisation = specialisation;
    }

    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    @Override
    public String toString() {
        return "SocialWorker [employeeId=" + employeeId + ", name=" + getFirstName() +
                " " + getLastName() + ", specialisation=" + specialisation + "]";
    }
}
