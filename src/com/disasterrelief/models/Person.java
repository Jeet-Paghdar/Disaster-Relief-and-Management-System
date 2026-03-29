package com.disasterrelief.models;

import java.time.LocalDate;

public class Person {

    private int personId;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private int age;
    private String gender;
    private String email;
    private String phoneNumber;

    public Person() {}

    public Person(int personId, String firstName, String lastName,
                  LocalDate dob, String gender,
                  String email, String phoneNumber) 
    {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        // Age is generally calculated automatically in database routines,
        // so we omit it from the constructor.
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public int getPersonId() { return personId; }
    public void setPersonId(int personId)
    { this.personId = personId; }

    public String getFirstName() 
    { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return "Person [personId=" + personId + ", name=" + firstName +
               " " + lastName + ", age=" + age + "]";
    }
}