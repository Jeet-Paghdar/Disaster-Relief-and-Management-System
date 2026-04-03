package com.disasterrelief.interfaces;

import com.disasterrelief.models.SocialWorker;
import com.disasterrelief.models.Victim;
import com.disasterrelief.models.Supply;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for central/headquarters-level workers who manage
 * the overall disaster relief system — worker assignments, 
 * global supply oversight, and full victim listings.
 */
public interface CentralWorker extends ReliefWorker {

    /**
     * Register a new social worker into the system.
     */
    void registerWorker(SocialWorker worker) throws SQLException;

    /**
     * Retrieve all registered social workers.
     */
    List<SocialWorker> getAllWorkers() throws SQLException;

    /**
     * Remove a social worker from the system by employee ID.
     */
    void removeWorker(int employeeId) throws SQLException;

    /**
     * Retrieve all victims across all disaster sites.
     */
    List<Victim> getAllVictims() throws SQLException;

    /**
     * Retrieve the full global supply inventory.
     */
    List<Supply> getAllSupplies() throws SQLException;

    /**
     * Update a social worker's shift assignment.
     */
    void updateWorkerShift(int employeeId, String workShift) throws SQLException;
}
