package com.disasterrelief.models;

import java.time.LocalDate;

public class Inquirer extends Person {

    private int inquirerId;
    private LocalDate inquiryDate;

    public Inquirer() {
    }

    public Inquirer(int personId, String firstName, String lastName,
            LocalDate dob, int age, String gender, String email,
            String phoneNumber, int inquirerId, LocalDate inquiryDate) {
        super(personId, firstName, lastName, dob, age, gender, email, phoneNumber);
        this.inquirerId = inquirerId;
        setInquiryDate(inquiryDate);
    }

    public int getInquirerId() {
        return inquirerId;
    }

    public void setInquirerId(int inquirerId) {
        this.inquirerId = inquirerId;
    }

    public LocalDate getInquiryDate() {
        return inquiryDate;
    }

    public void setInquiryDate(LocalDate inquiryDate) {
        if (inquiryDate == null)
            throw new IllegalArgumentException("Inquiry date cannot be null");
        this.inquiryDate = inquiryDate;
    }

    @Override
    public String toString() {
        return "Inquirer [inquirerId=" + inquirerId + ", name=" + getFirstName() +
                " " + getLastName() + "]";
    }
}
