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
CREATE FUNCTION fn_count_victims_in_camp(p_camp_name VARCHAR(200)) 
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count FROM victim WHERE address_after = p_camp_name;
    RETURN v_count;
END$$

-- 2. STORED PROCEDURES


-- Procedure to insert a generic PERSON and get the ID
CREATE PROCEDURE sp_insert_person(
    IN p_first_name VARCHAR(50),
    IN p_last_name VARCHAR(50),
    IN p_dob DATE,
    IN p_age INT,
    IN p_gender VARCHAR(10),
    IN p_email VARCHAR(100),
    IN p_phone_number VARCHAR(15),
    OUT p_out_person_id INT
)
BEGIN
    -- If age is not provided but DOB is, calculate it dynamically
    IF p_age IS NULL AND p_dob IS NOT NULL THEN
        SET p_age = fn_calculate_age(p_dob);
    END IF;

    INSERT INTO person (first_name, last_name, dob, age, gender, email, phone_number)
    VALUES (p_first_name, p_last_name, p_dob, p_age, p_gender, p_email, p_phone_number);
    
    SET p_out_person_id = LAST_INSERT_ID();
END$$

-- Procedure to register a VICTIM (inserts PERSON then VICTIM safely in 1 transaction)
CREATE PROCEDURE sp_register_victim(
    IN p_first_name VARCHAR(50),
    IN p_last_name VARCHAR(50),
    IN p_dob DATE,
    IN p_age INT,
    IN p_gender VARCHAR(10),
    IN p_email VARCHAR(100),
    IN p_phone_number VARCHAR(15),
    IN p_address_before VARCHAR(200),
    IN p_address_after VARCHAR(200),
    IN p_injury_status VARCHAR(100),
    IN p_entry_date DATE,
    IN p_disaster_id INT
)
BEGIN
    DECLARE v_person_id INT;

    -- Start Transaction to ensure both tables succeed or both fail
    START TRANSACTION;

    -- Insert into person base table and grab the returned ID
    CALL sp_insert_person(
        p_first_name, p_last_name, p_dob, p_age, p_gender, p_email, p_phone_number, v_person_id
    );

    -- Insert into victim table using the exact new person_id
    INSERT INTO victim (victim_id, address_before, address_after, injury_status, entry_date, disaster_id)
    VALUES (v_person_id, p_address_before, p_address_after, p_injury_status, p_entry_date, p_disaster_id);

    COMMIT;
END$$

-- Procedure to register a SOCIAL_WORKER (inserts PERSON then SOCIAL_WORKER safely)
CREATE PROCEDURE sp_register_worker(
    IN p_first_name VARCHAR(50),
    IN p_last_name VARCHAR(50),
    IN p_dob DATE,
    IN p_age INT,
    IN p_gender VARCHAR(10),
    IN p_email VARCHAR(100),
    IN p_phone_number VARCHAR(15),
    IN p_specialisation VARCHAR(100),
    IN p_work_shift VARCHAR(20)
)
BEGIN
    DECLARE v_person_id INT;

    START TRANSACTION;

    -- Call generic person insert
    CALL sp_insert_person(
        p_first_name, p_last_name, p_dob, p_age, p_gender, p_email, p_phone_number, v_person_id
    );

    -- Insert into social_worker
    INSERT INTO social_worker (employee_id, person_id, specialisation, work_shift)
    VALUES (NULL, v_person_id, p_specialisation, p_work_shift);

    COMMIT;
END$$

-- Procedure to allocate supply to victim safely
CREATE PROCEDURE sp_allocate_victim_supply(
    IN p_victim_id INT,
    IN p_supply_id INT,
    IN p_quantity_allocated INT,
    IN p_allocation_date DATE
)
BEGIN
    -- Ensure we have enough global stock first
    DECLARE v_current_stock INT;
    SELECT quantity INTO v_current_stock FROM supply WHERE supply_id = p_supply_id;

    IF v_current_stock >= p_quantity_allocated THEN
        START TRANSACTION;
        
        -- Insert the allocation record
        INSERT INTO victim_supply (victim_id, supply_id, quantity_allocated, allocation_date)
        VALUES (p_victim_id, p_supply_id, p_quantity_allocated, p_allocation_date);
        
        -- Deduct from global inventory automatically
        UPDATE supply 
        SET quantity = quantity - p_quantity_allocated 
        WHERE supply_id = p_supply_id;
        
        COMMIT;
    ELSE
        -- Signal an error if not enough stock is available (This gets caught by Java!)
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Insufficient supply stock to allocate to victim!';
    END IF;
END$$

-- 3. TRIGGERS


-- Trigger: When location receives new supply stock, increase global supply total automatically!
CREATE TRIGGER trg_update_global_supply_stock
AFTER INSERT ON location_supply
FOR EACH ROW
BEGIN
    UPDATE supply 
    SET quantity = quantity + NEW.quantity_stored
    WHERE supply_id = NEW.supply_id;
END$$

-- Trigger: Ensure medical records aren't dated in the future
CREATE TRIGGER trg_validate_medical_date
BEFORE INSERT ON medical_record
FOR EACH ROW
BEGIN
    IF NEW.treatment_date > CURDATE() THEN
        -- Standard SQL way to throw a custom Exception
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Treatment date cannot be in the future!';
    END IF;
END$$

DELIMITER ;
