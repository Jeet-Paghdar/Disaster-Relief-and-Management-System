USE DisasterReliefDB;

DELIMITER $$


-- 1. FUNCTIONS


-- Function to dynamically calculate a person's age
CREATE FUNCTION fn_calculate_age(p_dob DATE) 
RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_age INT;
    IF p_dob IS NULL THEN
        RETURN NULL;
    END IF;
    SET v_age = TIMESTAMPDIFF(YEAR, p_dob, CURDATE());
    RETURN v_age;
END$$

-- Function to count total victims in a specific camp (by address_after)
CREATE FUNCTION fn_count_victims_in_camp(p_location_id INT) 
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count FROM victim WHERE location_id = p_location_id;
    RETURN v_count;
END$$

-- 2. STORED PROCEDURES


-- Procedure to safely update camp capacity with occupancy check
CREATE PROCEDURE sp_update_camp_capacity(
    IN p_location_id INT,
    IN p_new_capacity INT
)
BEGIN
    DECLARE v_current_occupancy INT;
    
    -- Calculate current occupancy
    SET v_current_occupancy = fn_count_victims_in_camp(p_location_id);
    
    -- Validate: New capacity cannot be less than current occupancy
    IF p_new_capacity < v_current_occupancy THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: New capacity cannot be less than current occupancy!';
    ELSE
        UPDATE location SET capacity = p_new_capacity WHERE location_id = p_location_id;
    END IF;
END$$

-- Procedure to perform a deep cascading delete of a victim
CREATE PROCEDURE sp_cascade_delete_victim(
    IN p_victim_id INT
)
BEGIN
    DECLARE v_person_id INT;
    SET v_person_id = p_victim_id;

    START TRANSACTION;

    -- 1. Delete Medical Records
    DELETE FROM medical_record WHERE victim_id = v_person_id;
    
    -- 2. Delete Dietary Restrictions
    DELETE FROM victim_dietary_restrictions WHERE victim_id = v_person_id;
    
    -- 3. Delete Supply Allocations
    DELETE FROM victim_supply WHERE victim_id = v_person_id;
    
    -- 4. Delete Matches
    DELETE FROM match_registry WHERE victim_id = v_person_id;
    
    -- 5. Delete from Victim profile
    DELETE FROM victim WHERE victim_id = v_person_id;
    
    -- 6. Finally delete from Person base table
    DELETE FROM person WHERE person_id = v_person_id;

    COMMIT;
END$$

-- Procedure to update medical records and victim profile (Blood Type) in sync
CREATE PROCEDURE sp_unified_medical_update(
    IN p_record_number INT,
    IN p_prescriptions TEXT,
    IN p_details TEXT,
    IN p_treatment_date DATE,
    IN p_victim_id INT,
    IN p_blood_type VARCHAR(5)
)
BEGIN
    START TRANSACTION;

    -- Update Medical Record
    UPDATE medical_record 
    SET prescriptions = p_prescriptions, 
        treatment_details = p_details, 
        treatment_date = p_treatment_date 
    WHERE record_number = p_record_number;

    -- Update Victim's Blood Type
    UPDATE victim SET blood_type = p_blood_type WHERE victim_id = p_victim_id;

    COMMIT;
END$$

-- 3. TRIGGERS


-- Trigger: Ensure medical records aren't dated in the future
CREATE TRIGGER trg_validate_medical_date
BEFORE INSERT ON medical_record
FOR EACH ROW
BEGIN
    IF NEW.treatment_date > CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Treatment date cannot be in the future!';
    END IF;
END$$

-- Trigger: Prevent adding victims if the camp is at full capacity
CREATE TRIGGER trg_prevent_overcrowding
BEFORE INSERT ON victim
FOR EACH ROW
BEGIN
    DECLARE v_capacity INT;
    DECLARE v_current INT;
    
    -- Get total capacity for the specific camp
    SELECT CAPACITY INTO v_capacity FROM location WHERE location_id = NEW.location_id;
    
    -- Use our function to get current occupancy
    SET v_current = fn_count_victims_in_camp(NEW.location_id);
    
    IF v_current >= v_capacity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Selected camp is already at 100% capacity!';
    END IF;
END$$

-- Trigger: Prevent registering people with future Dates of Birth
CREATE TRIGGER trg_validate_dob
BEFORE INSERT ON person
FOR EACH ROW
BEGIN
    IF NEW.DOB > CURDATE() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Date of Birth cannot be in the future!';
    END IF;
END$$

DELIMITER ;
