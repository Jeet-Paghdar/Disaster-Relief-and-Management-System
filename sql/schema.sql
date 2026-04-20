CREATE DATABASE DisasterReliefDB;
USE DisasterReliefDB;


-- 1. GOVT_AGENCY
create table GOVT_AGENCY (
    AGENCY_ID int auto_increment primary key,
    AGENCY_NAME varchar(100) not null,
    BUDGET_CODE varchar(50),
    CREATED_AT timestamp default current_timestamp
);

-- 2. DISASTER
create table DISASTER (
    DISASTER_ID int auto_increment primary key,
    TYPE varchar(50) not null,
    SEVERITY varchar(20),
    AGENCY_ID int,
    foreign key (AGENCY_ID) references GOVT_AGENCY(AGENCY_ID)
);

-- 2.1 DISASTER_REGION (1NF Resolution)
create table DISASTER_REGION (
    DISASTER_ID int,
    REGION_NAME varchar(100) not null,
    primary key (DISASTER_ID, REGION_NAME),
    foreign key (DISASTER_ID) references DISASTER(DISASTER_ID)
);

-- 3. PERSON
create table PERSON (
    PERSON_ID int auto_increment primary key,
    FIRST_NAME varchar(50) not null,
    LAST_NAME varchar(50) not null,
    DOB date,
    GENDER varchar(10),
    EMAIL varchar(100),
    PHONE_NUMBER varchar(15)
);

-- 4. VICTIM
create table VICTIM (
    VICTIM_ID int primary key,
    LOCATION_ID int,
    INJURY_STATUS varchar(100),
    ENTRY_DATE date,
    DISASTER_ID int,
    BLOOD_TYPE varchar(5),
    foreign key (VICTIM_ID) references PERSON(PERSON_ID),
    foreign key (DISASTER_ID) references DISASTER(DISASTER_ID),
    foreign key (LOCATION_ID) references LOCATION(LOCATION_ID)
);

-- 5. INQUIRER
create table INQUIRER (
    INQUIRER_ID int primary key,
    INQUIRY_TIMESTAMP timestamp default current_timestamp,
    foreign key (INQUIRER_ID) references PERSON(PERSON_ID)
);

-- 6. SOCIAL_WORKER
create table SOCIAL_WORKER (
    EMPLOYEE_ID int auto_increment primary key,
    PERSON_ID int,
    SPECIALISATION varchar(100),
    WORK_SHIFT varchar(20),
    CONSTRAINT uq_worker_person UNIQUE (PERSON_ID),
    foreign key (PERSON_ID) references PERSON(PERSON_ID)
);



-- 8. MEDICAL_RECORD
create table MEDICAL_RECORD (
    RECORD_NUMBER int auto_increment primary key,
    VICTIM_ID int,
    PRESCRIPTIONS text,
    TREATMENT_DETAILS text,
    TREATMENT_DATE date,
    WORKER_ID int,
    foreign key (VICTIM_ID) references VICTIM(VICTIM_ID),
    foreign key (WORKER_ID) references SOCIAL_WORKER(EMPLOYEE_ID)
);

-- 9. VICTIM_DIETARY_RESTRICTIONS
create table VICTIM_DIETARY_RESTRICTIONS (
    VICTIM_ID int,
    RESTRICTION_TYPE varchar(50),
    primary key (VICTIM_ID, RESTRICTION_TYPE),
    foreign key (VICTIM_ID) references VICTIM(VICTIM_ID)
);

-- 10. LOCATION
create table LOCATION (
    LOCATION_ID int auto_increment primary key,
    NAME varchar(100) not null,
    ADDRESS varchar(200),
    TYPE varchar(50),
    PINCODE varchar(10),
    CAPACITY int,
    CONSTRAINT uq_location_detail UNIQUE (NAME, ADDRESS, PINCODE)
);

-- 11. MATCH_REGISTRY
create table MATCH_REGISTRY (
    MATCH_ID int auto_increment primary key,
    INQUIRER_ID int,
    VICTIM_ID int,
    LOCATION_ID int,
    INFO_PROVIDED text,
    foreign key (INQUIRER_ID) references INQUIRER(INQUIRER_ID),
    foreign key (VICTIM_ID) references VICTIM(VICTIM_ID),
    foreign key (LOCATION_ID) references LOCATION(LOCATION_ID)
);



-- 13. SUPPLY
create table SUPPLY (
    SUPPLY_ID int auto_increment primary key,
    ITEM_NAME varchar(100) not null,
    TYPE varchar(50),
    EXPIRY_DATE date
);

-- 14. LOCATION_SUPPLY
create table LOCATION_SUPPLY (
    LOCATION_ID int,
    SUPPLY_ID int,
    QUANTITY_STORED int default 0,
    primary key (LOCATION_ID, SUPPLY_ID),
    foreign key (LOCATION_ID) references LOCATION(LOCATION_ID),
    foreign key (SUPPLY_ID) references SUPPLY(SUPPLY_ID)
);



-- 16. VICTIM_SUPPLY
create table VICTIM_SUPPLY (
    VICTIM_ID int,
    SUPPLY_ID int,
    QUANTITY_ALLOCATED int default 0,
    ALLOCATION_DATE date,
    primary key (VICTIM_ID, SUPPLY_ID),
    foreign key (VICTIM_ID) references VICTIM(VICTIM_ID),
    foreign key (SUPPLY_ID) references SUPPLY(SUPPLY_ID)
);

-- 17. USERS
CREATE TABLE USERS (
    USER_ID INT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(50) NOT NULL UNIQUE,
    PASSWORD VARCHAR(100) NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin
INSERT INTO USERS (USERNAME, PASSWORD) VALUES ('admin', 'admin123');

-- 18. SUPPLY_WITH_STOCK VIEW
CREATE VIEW SUPPLY_WITH_STOCK AS
SELECT 
    s.SUPPLY_ID,
    s.ITEM_NAME,
    s.TYPE,
    s.EXPIRY_DATE,
    (COALESCE(ls.total_stored, 0) - COALESCE(vs.total_allocated, 0)) AS QUANTITY
FROM SUPPLY s
LEFT JOIN (
    SELECT SUPPLY_ID, SUM(QUANTITY_STORED) AS total_stored 
    FROM LOCATION_SUPPLY GROUP BY SUPPLY_ID
) ls ON s.SUPPLY_ID = ls.SUPPLY_ID
LEFT JOIN (
    SELECT SUPPLY_ID, SUM(QUANTITY_ALLOCATED) AS total_allocated 
    FROM VICTIM_SUPPLY GROUP BY SUPPLY_ID
) vs ON s.SUPPLY_ID = vs.SUPPLY_ID;

show tables;