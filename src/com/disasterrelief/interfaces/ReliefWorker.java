package com.disasterrelief.interfaces;

import com.disasterrelief.models.Victim;
import com.disasterrelief.models.MedicalRecord;
import com.disasterrelief.exceptions.VictimNotFoundException;

import java.sql.SQLException;
import java.util.List;

/**
 * Base interface for all relief workers in the disaster management system.
 * Defines core operations that any worker (central or field-based) must support.
 */
public interface ReliefWorker {

    /**
     * Register a new victim into the system.
     */
    void registerVictim(Victim victim) throws SQLException;

    /**
     * Search for victims by first name.
     */
    List<Victim> searchVictims(String firstName) throws SQLException;

    /**
     * Retrieve a victim's medical history.
     */
    List<MedicalRecord> getMedicalHistory(int victimId) throws SQLException, VictimNotFoundException;

    /**
     * Log a new medical record for a victim.
     */
    void logMedicalRecord(MedicalRecord record) throws SQLException, VictimNotFoundException;
}
