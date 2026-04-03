package com.disasterrelief.interfaces;

import com.disasterrelief.models.Supply;
import com.disasterrelief.models.Location;
import com.disasterrelief.exceptions.InvalidSupplyException;
import com.disasterrelief.exceptions.VictimNotFoundException;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface for field workers who operate at a specific relief location
 * (shelter, medical camp, supply center, etc.). Handles on-the-ground
 * operations like updating victim status and allocating supplies.
 */
public interface LocationBasedWorker extends ReliefWorker {

    /**
     * Update the injury status of a victim at this location.
     */
    void updateVictimInjuryStatus(int victimId, String injuryStatus) throws SQLException, VictimNotFoundException;

    /**
     * Allocate a specific quantity of supply to a victim.
     */
    void allocateSupplyToVictim(int victimId, int supplyId, int quantity) throws SQLException, InvalidSupplyException, VictimNotFoundException;

    /**
     * Add a new supply item to this worker's location inventory.
     */
    void addSupply(Supply supply) throws SQLException, InvalidSupplyException;

    /**
     * Update the quantity of an existing supply item.
     */
    void updateSupplyQuantity(int supplyId, int quantity) throws SQLException, InvalidSupplyException;

    /**
     * Retrieve all available locations (shelters, camps, hospitals).
     */
    List<Location> getAllLocations() throws SQLException;
}
