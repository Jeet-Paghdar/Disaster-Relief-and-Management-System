USE DisasterReliefDB;

-- =====================================================================================
-- FACTORY RESET (DISABLED BY DEFAULT TO PROTECT GUI DATA)
-- If you ever need to completely wipe the database and start over, you can uncomment 
-- the lines below. WARNING: This will permanently delete ALL data you added manually!
-- =====================================================================================
 	
-- =====================================================================================


-- 1. AGENCIES
INSERT INTO GOVT_AGENCY (AGENCY_NAME, BUDGET_CODE) VALUES 
('NDMA', 'BUDGET-1001'), 
('NDRF', 'BUDGET-1002'),
('NIDM', 'BUDGET-1003');

-- 2. DISASTERS & REGIONS
INSERT INTO DISASTER (DISASTER_ID, TYPE, SEVERITY, AGENCY_ID) VALUES 
(1, 'Hurricane', 'High', 1),
(2, 'Earthquake', 'Severe', 2);

INSERT INTO DISASTER_REGION (DISASTER_ID, REGION_NAME) VALUES 
(1, 'Florida'), (1, 'Georgia'),
(2, 'California');

-- 3. LOCATIONS
INSERT INTO LOCATION (LOCATION_ID, NAME, ADDRESS, TYPE, PINCODE, CAPACITY) VALUES
(1, 'Shelter Alpha', '123 Safe St', 'Shelter', '110001', 500),
(2, 'City Hospital', '456 Med Blvd', 'Hospital', '110002', 1000),
(3, 'Base Warehouse', '789 Supply Rd', 'Supply Center', '110003', 5000);



-- 5. SUPPLIES
INSERT INTO SUPPLY (SUPPLY_ID, ITEM_NAME, TYPE, EXPIRY_DATE) VALUES
(1, 'First Aid Kit', 'Medical', '2027-12-31'),
(2, 'Water Bottles 1L', 'Food/Water', '2026-10-01'),
(3, 'Blankets', 'Utility', NULL);

-- Add Stock to Location (Location 3 gets stock)
INSERT INTO LOCATION_SUPPLY (LOCATION_ID, SUPPLY_ID, QUANTITY_STORED) VALUES
(3, 1, 1000), (3, 2, 5000), (3, 3, 2000);

-- 6. INSERT 20 PEOPLE

-- 10 VICTIMS (via Procedure sp_register_victim)
CALL sp_register_victim('John', 'Doe', '1990-05-15', 'Male', 'john.doe@email.com', '5550000001', 'Old House 1', 'Shelter Alpha', 'None', CURDATE(), 1, 'O+');
CALL sp_register_victim('Jane', 'Smith', '1985-08-20', 'Female', 'jane.s@email.com', '5550000002', 'Old House 2', 'Shelter Alpha', 'Minor', CURDATE(), 1, 'A-');
CALL sp_register_victim('Robert', 'Johnson', '1975-11-10', 'Male', 'rob.j@email.com', '5550000003', 'Coastal St 3', 'City Hospital', 'Critical', CURDATE(), 1, 'AB+');
CALL sp_register_victim('Emily', 'Davis', '2000-02-28', 'Female', 'emily.d@email.com', '5550000004', 'Apt 4B', 'Shelter Alpha', 'None', CURDATE(), 2, 'B+');
CALL sp_register_victim('Michael', 'Wilson', '1950-12-05', 'Male', 'm.wilson@email.com', '5550000005', 'Retirement Rd', 'City Hospital', 'Moderate', CURDATE(), 2, 'O-');
CALL sp_register_victim('Sarah', 'Miller', '1995-07-14', 'Female', 'sarah.m@email.com', '5550000006', 'Main St 10', 'Shelter Alpha', 'None', CURDATE(), 1, 'A+');
CALL sp_register_victim('David', 'Anderson', '1988-04-30', 'Male', 'david.a@email.com', '5550000007', 'Pine Ave 55', 'Shelter Alpha', 'Minor', CURDATE(), 2, 'B-');
CALL sp_register_victim('Laura', 'Thomas', '1992-09-18', 'Female', 'laura.t@email.com', '5550000008', 'Elm St 12', 'City Hospital', 'Severe', CURDATE(), 1, 'AB-');
CALL sp_register_victim('James', 'Jackson', '1980-03-25', 'Male', 'james.j@email.com', '5550000009', 'Cedar Ln 8', 'Shelter Alpha', 'None', CURDATE(), 2, 'O+');
CALL sp_register_victim('Linda', 'White', '1965-06-12', 'Female', 'linda.w@email.com', '5550000010', 'Oak Dr 44', 'City Hospital', 'Moderate', CURDATE(), 1, 'A+');

-- 5 SOCIAL WORKERS (via Procedure sp_register_worker)
CALL sp_register_worker('Alice', 'Brown', '1982-01-10', 'Female', 'alice.b@agency.org', '5551000001', 'Medical First Responder', 'Day');
CALL sp_register_worker('Brian', 'Clark', '1979-05-22', 'Male', 'brian.c@agency.org', '5551000002', 'Logistics Coordinator', 'Afternoon');
CALL sp_register_worker('Chloe', 'Lewis', '1990-11-30', 'Female', 'chloe.l@agency.org', '5551000003', 'Psychological Support', 'Night');
CALL sp_register_worker('Daniel', 'Walker', '1985-08-15', 'Male', 'daniel.w@agency.org', '5551000004', 'Rescue Field Agent', 'Day');
CALL sp_register_worker('Eva', 'Hall', '1993-02-18', 'Female', 'eva.h@agency.org', '5551000005', 'Camp Administration', 'Night');

-- 5 INQUIRERS
INSERT INTO PERSON (FIRST_NAME, LAST_NAME, DOB, GENDER, EMAIL, PHONE_NUMBER) VALUES 
('Frank', 'Taylor', '1970-04-12', 'Male', 'frank.t@email.com', '5552000001'),
('Grace', 'Moore', '1988-09-05', 'Female', 'grace.m@email.com', '5552000002'),
('Harry', 'King', '1960-12-20', 'Male', 'harry.k@email.com', '5552000003'),
('Ivy', 'Wright', '1995-07-08', 'Female', 'ivy.w@email.com', '5552000004'),
('Jack', 'Scott', '1982-03-14', 'Male', 'jack.s@email.com', '5552000005');

INSERT INTO INQUIRER (INQUIRER_ID) VALUES
((SELECT PERSON_ID FROM PERSON WHERE PHONE_NUMBER='5552000001')),
((SELECT PERSON_ID FROM PERSON WHERE PHONE_NUMBER='5552000002')),
((SELECT PERSON_ID FROM PERSON WHERE PHONE_NUMBER='5552000003')),
((SELECT PERSON_ID FROM PERSON WHERE PHONE_NUMBER='5552000004')),
((SELECT PERSON_ID FROM PERSON WHERE PHONE_NUMBER='5552000005'));
