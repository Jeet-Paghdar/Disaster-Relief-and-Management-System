package com.disasterrelief.interfaces;

import com.disasterrelief.models.Victim;
import com.disasterrelief.exceptions.VictimNotFoundException;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface defining the complete lifecycle management of disaster victims —
 * registration, retrieval, search, status updates, and removal.
 * Intended to be implemented by the VictimDAO or a service layer.
 */
public interface DisasterVictimManagement {

    /**
     * Register a new victim (inserts into both PERSON and VICTIM tables transactionally).
     */
    void addVictim(Victim victim) throws SQLException;

    /**
     * Retrieve all registered victims across all disasters.
     */
    List<Victim> getAllVictims() throws SQLException;

    /**
     * Search victims by first name.
     */
    List<Victim> searchVictimsByName(String firstName) throws SQLException;

    /**
     * Update a victim's injury status.
     */
    void updateInjuryStatus(int victimId, String injuryStatus) throws SQLException, VictimNotFoundException;

    /**
     * Remove a victim from the system (deletes from both VICTIM and PERSON tables transactionally).
     */
    void deleteVictim(int victimId) throws SQLException, VictimNotFoundException;
}
