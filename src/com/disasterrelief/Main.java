package com.disasterrelief;

import java.time.LocalDate;
import com.disasterrelief.models.Victim;
import com.disasterrelief.utils.DBConnection;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Starting Disaster Relief System...");
            
            // 1. Initialise the victim with mock data
            Victim newVictim = new Victim();
            newVictim.setFirstName("John");
            newVictim.setLastName("Doe");
            newVictim.setDob(LocalDate.parse("1985-06-15"));
            newVictim.setGender("Male");
            newVictim.setEmail("johndoe@example.com");
            newVictim.setPhoneNumber("1234567890");
            newVictim.setAddressBefore("123 Old Street");
            newVictim.setAddressAfter("Camp Hope #4");
            newVictim.setInjuryStatus("Minor fractures");
            newVictim.setEntryDate(LocalDate.now());
            // Make sure disaster ID matches one in DISASTER table or handles foreign key constraint
            // In a real scenario, we'll fetch this from DB. For now, we leave as 1 assuming it exists.
            newVictim.setDisasterId(1); 
            
            System.out.println("Victim initialized but DAO operations are removed.");
            

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            DBConnection.closeConnection();
        }
    }
}
