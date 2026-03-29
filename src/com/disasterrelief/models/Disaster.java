package com.disasterrelief.models;

public class Disaster {

    private int disasterId;
    private String type;
    private String severity;
    private String affectedRegions;
    private int agencyId;

    public Disaster() {}

    public Disaster(int disasterId, String type, String severity, String affectedRegions, int agencyId) {
        this.disasterId = disasterId;
        this.type = type;
        this.severity = severity;
        this.affectedRegions = affectedRegions;
        this.agencyId = agencyId;
    }

    public int getDisasterId() { return disasterId; }
    public void setDisasterId(int disasterId) { this.disasterId = disasterId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getAffectedRegions() { return affectedRegions; }
    public void setAffectedRegions(String affectedRegions) { this.affectedRegions = affectedRegions; }

    public int getAgencyId() { return agencyId; }
    public void setAgencyId(int agencyId) { this.agencyId = agencyId; }

    @Override
    public String toString() {
        return "Disaster [disasterId=" + disasterId + ", type=" + type + ", severity=" + severity + "]";
    }
}
