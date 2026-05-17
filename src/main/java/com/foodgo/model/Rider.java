package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rider Model - Represents a delivery partner in the FoodGo system
 * OOP: Encapsulation — private fields, public accessors
 */
public class Rider {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;
    @JsonProperty("phone")
    private String phone;

    @JsonProperty("online")
    private boolean online = false;

    /** pending | approved | rejected */
    @JsonProperty("approvalStatus")
    private String approvalStatus = "pending";
    @JsonProperty("rejectionReason")
    private String rejectionReason = "";

    // Vehicle Details
    @JsonProperty("bikeModel")
    private String bikeModel = "";
    @JsonProperty("bikeNumber")
    private String bikeNumber = "";
    @JsonProperty("vehicleRegistration")
    private String vehicleRegistration = "";
    @JsonProperty("insuranceDocument")
    private String insuranceDocument = "";
    @JsonProperty("vehiclePhoto")
    private String vehiclePhoto = "";

    // Rider Documents
    @JsonProperty("nicDocument")
    private String nicDocument = "";
    @JsonProperty("drivingLicense")
    private String drivingLicense = "";
    @JsonProperty("riderPhoto")
    private String riderPhoto = "";
    @JsonProperty("riderInsurance")
    private String riderInsurance = "";

    @JsonProperty("vehicleDetailsSubmitted")
    private boolean vehicleDetailsSubmitted = false;
    @JsonProperty("documentsSubmitted")
    private boolean documentsSubmitted = false;

    @JsonProperty("createdAt")
    private long createdAt = 0;
    @JsonProperty("updatedAt")
    private long updatedAt = 0;

    public Rider() {}

    public Rider(String id, String name, String email, String password, String phone) {
        this.id = id; this.name = name; this.email = email;
        this.password = password; this.phone = phone;
        this.approvalStatus = "pending";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String pw) { this.password = pw; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String s) { this.approvalStatus = s != null ? s : "pending"; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String r) { this.rejectionReason = r != null ? r : ""; }
    public String getBikeModel() { return bikeModel; }
    public void setBikeModel(String v) { this.bikeModel = v != null ? v : ""; }
    public String getBikeNumber() { return bikeNumber; }
    public void setBikeNumber(String v) { this.bikeNumber = v != null ? v : ""; }
    public String getVehicleRegistration() { return vehicleRegistration; }
    public void setVehicleRegistration(String v) { this.vehicleRegistration = v != null ? v : ""; }
    public String getInsuranceDocument() { return insuranceDocument; }
    public void setInsuranceDocument(String v) { this.insuranceDocument = v != null ? v : ""; }
    public String getVehiclePhoto() { return vehiclePhoto; }
    public void setVehiclePhoto(String v) { this.vehiclePhoto = v != null ? v : ""; }
    public String getNicDocument() { return nicDocument; }
    public void setNicDocument(String v) { this.nicDocument = v != null ? v : ""; }
    public String getDrivingLicense() { return drivingLicense; }
    public void setDrivingLicense(String v) { this.drivingLicense = v != null ? v : ""; }
    public String getRiderPhoto() { return riderPhoto; }
    public void setRiderPhoto(String v) { this.riderPhoto = v != null ? v : ""; }
    public String getRiderInsurance() { return riderInsurance; }
    public void setRiderInsurance(String v) { this.riderInsurance = v != null ? v : ""; }
    public boolean isVehicleDetailsSubmitted() { return vehicleDetailsSubmitted; }
    public void setVehicleDetailsSubmitted(boolean v) { this.vehicleDetailsSubmitted = v; }
    public boolean isDocumentsSubmitted() { return documentsSubmitted; }
    public void setDocumentsSubmitted(boolean v) { this.documentsSubmitted = v; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long v) { this.createdAt = v; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long v) { this.updatedAt = v; }

    public boolean canGoOnline() {
        return vehicleDetailsSubmitted && documentsSubmitted && "approved".equals(approvalStatus);
    }

    private static String esc(String s) { return s != null ? s.replace("||", "&#124;&#124;") : ""; }
    private static long parseLong(String s) { try { return Long.parseLong(s.trim()); } catch (Exception e) { return 0; } }

    public String toFileLine() {
        return esc(id)+"||"+esc(name)+"||"+esc(email)+"||"+esc(password)+"||"+esc(phone)+"||"
              +online+"||"+esc(approvalStatus)+"||"+esc(rejectionReason)+"||"
              +esc(bikeModel)+"||"+esc(bikeNumber)+"||"
              +esc(vehicleRegistration)+"||"+esc(insuranceDocument)+"||"+esc(vehiclePhoto)+"||"
              +esc(nicDocument)+"||"+esc(drivingLicense)+"||"+esc(riderPhoto)+"||"+esc(riderInsurance)+"||"
              +vehicleDetailsSubmitted+"||"+documentsSubmitted+"||"
              +createdAt+"||"+updatedAt;
    }

    public static Rider fromFileLine(String line) {
        // Support old comma-separated 5-field format
        if (!line.contains("||")) {
            String[] p = line.split(",", 5);
            return new Rider(
                p.length>0?p[0]:"", p.length>1?p[1]:"", p.length>2?p[2]:"",
                p.length>3?p[3]:"", p.length>4?p[4]:""
            );
        }
        String[] p = line.split("\\|\\|", -1);
        Rider r = new Rider();
        r.id                      = p.length>0  ? p[0]  : "";
        r.name                    = p.length>1  ? p[1]  : "";
        r.email                   = p.length>2  ? p[2]  : "";
        r.password                = p.length>3  ? p[3]  : "";
        r.phone                   = p.length>4  ? p[4]  : "";
        r.online                  = p.length>5  && Boolean.parseBoolean(p[5]);
        r.approvalStatus          = p.length>6  ? p[6]  : "pending";
        r.rejectionReason         = p.length>7  ? p[7]  : "";
        r.bikeModel               = p.length>8  ? p[8]  : "";
        r.bikeNumber              = p.length>9  ? p[9]  : "";
        r.vehicleRegistration     = p.length>10 ? p[10] : "";
        r.insuranceDocument       = p.length>11 ? p[11] : "";
        r.vehiclePhoto            = p.length>12 ? p[12] : "";
        r.nicDocument             = p.length>13 ? p[13] : "";
        r.drivingLicense          = p.length>14 ? p[14] : "";
        r.riderPhoto              = p.length>15 ? p[15] : "";
        r.riderInsurance          = p.length>16 ? p[16] : "";
        r.vehicleDetailsSubmitted = p.length>17 && Boolean.parseBoolean(p[17]);
        r.documentsSubmitted      = p.length>18 && Boolean.parseBoolean(p[18]);
        r.createdAt               = p.length>19 ? parseLong(p[19]) : 0;
        r.updatedAt               = p.length>20 ? parseLong(p[20]) : 0;
        return r;
    }

    @Override
    public String toString() {
        return "Rider{id='"+id+"', name='"+name+"', phone='"+phone+"', approvalStatus='"+approvalStatus+"', canGoOnline="+canGoOnline()+'}';
    }
}
