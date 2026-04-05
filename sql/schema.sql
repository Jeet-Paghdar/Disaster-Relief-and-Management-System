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
    AFFECTED_REGIONS varchar(200),
    AGENCY_ID int,
    foreign key (AGENCY_ID) references GOVT_AGENCY(AGENCY_ID)
);

-- 3. PERSON
create table PERSON (
    PERSON_ID int auto_increment primary key,
    FIRST_NAME varchar(50) not null,
    LAST_NAME varchar(50) not null,
    DOB date,
    AGE int,
    GENDER varchar(10),
    EMAIL varchar(100),
    PHONE_NUMBER varchar(15)
);

-- 4. VICTIM
create table VICTIM (
    VICTIM_ID int primary key,
    ADDRESS_BEFORE varchar(200),
    ADDRESS_AFTER varchar(200),
    INJURY_STATUS varchar(100),
    ENTRY_DATE date,
    DISASTER_ID int,
    foreign key (VICTIM_ID) references PERSON(PERSON_ID),
    foreign key (DISASTER_ID) references DISASTER(DISASTER_ID)
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
    foreign key (PERSON_ID) references PERSON(PERSON_ID)
);

-- 7. FAMILY_RELATION
create table FAMILY_RELATION (
    RELATION_ID int auto_increment primary key,
    VICTIM1_ID int,
    VICTIM2_ID int,
    RELATION_TYPE varchar(50),
    foreign key (VICTIM1_ID) references VICTIM(VICTIM_ID),
    foreign key (VICTIM2_ID) references VICTIM(VICTIM_ID)
);

-- 8. MEDICAL_RECORD
create table MEDICAL_RECORD (
    RECORD_NUMBER int auto_increment primary key,
    VICTIM_ID int,
    BLOOD_TYPE varchar(5),
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
    CAPACITY int
);

-- 11. RELIEF_SERVICE
create table RELIEF_SERVICE (
    SERVICE_ID int auto_increment primary key,
    INQUIRER_ID int,
    VICTIM_ID int,
    LOCATION_ID int,
    DATE_OF_INQUIRY date,
    INFO_PROVIDED text,
    foreign key (INQUIRER_ID) references INQUIRER(INQUIRER_ID),
    foreign key (VICTIM_ID) references VICTIM(VICTIM_ID),
    foreign key (LOCATION_ID) references LOCATION(LOCATION_ID)
);

-- 12. VENDOR
create table VENDOR (
    VENDOR_ID int auto_increment primary key,
    COMPANY_NAME varchar(100) not null,
    RATING decimal(3,1),
    EMAIL varchar(100),
    PHONE_NUMBER varchar(15)
);

-- 13. SUPPLY
create table SUPPLY (
    SUPPLY_ID int auto_increment primary key,
    ITEM_NAME varchar(100) not null,
    QUANTITY int default 0,
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

-- 15. VENDOR_SUPPLY
create table VENDOR_SUPPLY (
    VENDOR_ID int,
    SUPPLY_ID int,
    primary key (VENDOR_ID, SUPPLY_ID),
    foreign key (VENDOR_ID) references VENDOR(VENDOR_ID),
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

show tables;