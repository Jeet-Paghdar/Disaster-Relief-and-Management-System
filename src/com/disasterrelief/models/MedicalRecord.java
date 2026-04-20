package com.disasterrelief.models;

import java.time.LocalDate;

public class MedicalRecord {

    private int recordNumber;
    private int victimId;
    private int age;
    private String bloodType;
    private String prescriptions;
    private String treatmentDetails;
    private LocalDate treatmentDate;
    private int workerId;

    public MedicalRecord() {}

    public MedicalRecord(int recordNumber, int victimId, String bloodType,
                         String prescriptions, String treatmentDetails,
                         LocalDate treatmentDate, int workerId) {
        this.recordNumber = recordNumber;
        this.victimId = victimId;
        this.bloodType = bloodType;
        this.prescriptions = prescriptions;
        this.treatmentDetails = treatmentDetails;
        setTreatmentDate(treatmentDate);
        this.workerId = workerId;
    }

    public MedicalRecord(int recordNumber, int victimId, int age, String bloodType,
                         String prescriptions, String treatmentDetails,
                         LocalDate treatmentDate, int workerId) {
        this(recordNumber, victimId, bloodType, prescriptions, treatmentDetails, treatmentDate, workerId);
        this.age = age;
    }

    public int getRecordNumber() { return recordNumber; }
    public void setRecordNumber(int recordNumber) { this.recordNumber = recordNumber; }

    public int getVictimId() { return victimId; }
    public void setVictimId(int victimId) { this.victimId = victimId; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getPrescriptions() { return prescriptions; }
    public void setPrescriptions(String prescriptions) { this.prescriptions = prescriptions; }

    public String getTreatmentDetails() { return treatmentDetails; }
    public void setTreatmentDetails(String treatmentDetails) { this.treatmentDetails = treatmentDetails; }

    public LocalDate getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(LocalDate treatmentDate) {
        if (treatmentDate == null)
            throw new IllegalArgumentException("Treatment date cannot be null");
        if (treatmentDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Treatment date cannot be in the future");
        this.treatmentDate = treatmentDate;
    }

    public int getWorkerId() { return workerId; }
    public void setWorkerId(int workerId) { this.workerId = workerId; }

    @Override
    public String toString() {
        return "MedicalRecord [recordNumber=" + recordNumber + ", victimId=" + victimId +
               ", bloodType=" + bloodType + ", treatmentDate=" + treatmentDate + "]";
    }
}
