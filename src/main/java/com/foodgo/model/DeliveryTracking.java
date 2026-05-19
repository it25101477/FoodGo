package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DeliveryTracking Model - Tracks order delivery status and location
 */
public class DeliveryTracking {
    @JsonProperty("trackingId")
    private String trackingId;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("riderId")
    private String riderId;
    
    @JsonProperty("riderName")
    private String riderName;
    
    @JsonProperty("riderPhone")
    private String riderPhone;
    
    @JsonProperty("status")
    private String status; // picked, in_transit, arriving, delivered
    
    @JsonProperty("pickupLocation")
    private String pickupLocation;
    
    @JsonProperty("deliveryLocation")
    private String deliveryLocation;
    
    @JsonProperty("currentLatitude")
    private double currentLatitude;
    
    @JsonProperty("currentLongitude")
    private double currentLongitude;
    
    @JsonProperty("estimatedArrival")
    private long estimatedArrival; // timestamp
    
    @JsonProperty("startedAt")
    private long startedAt;
    
    @JsonProperty("deliveredAt")
    private long deliveredAt;

    public DeliveryTracking() {}

    public DeliveryTracking(String orderId, String riderId, String riderName, String riderPhone) {
        this.trackingId = String.valueOf(System.currentTimeMillis());
        this.orderId = orderId;
        this.riderId = riderId;
        this.riderName = riderName;
        this.riderPhone = riderPhone;
        this.status = "picked";
        this.startedAt = System.currentTimeMillis();
        this.estimatedArrival = System.currentTimeMillis() + (30 * 60 * 1000); // 30 mins
    }

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public String getRiderName() { return riderName; }
    public void setRiderName(String riderName) { this.riderName = riderName; }

    public String getRiderPhone() { return riderPhone; }
    public void setRiderPhone(String riderPhone) { this.riderPhone = riderPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDeliveryLocation() { return deliveryLocation; }
    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }

    public double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(double currentLatitude) { this.currentLatitude = currentLatitude; }

    public double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(double currentLongitude) { this.currentLongitude = currentLongitude; }

    public long getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(long estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public long getStartedAt() { return startedAt; }
    public long getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(long deliveredAt) { this.deliveredAt = deliveredAt; }

    public String toFileLine() {
        return trackingId + "||" + orderId + "||" + riderId + "||" + 
               (riderName != null ? riderName : "") + "||" + 
               (riderPhone != null ? riderPhone : "") + "||" + 
               status + "||" + 
               (pickupLocation != null ? pickupLocation : "") + "||" + 
               (deliveryLocation != null ? deliveryLocation : "") + "||" + 
               currentLatitude + "||" + currentLongitude + "||" + 
               estimatedArrival + "||" + startedAt + "||" + deliveredAt;
    }

    public static DeliveryTracking fromFileLine(String line) {
        String[] parts = line.split("\\|\\|", -1);
        DeliveryTracking t = new DeliveryTracking();
        t.setTrackingId(parts.length > 0 ? parts[0] : "");
        t.setOrderId(parts.length > 1 ? parts[1] : "");
        t.setRiderId(parts.length > 2 ? parts[2] : "");
        t.setRiderName(parts.length > 3 ? parts[3] : "");
        t.setRiderPhone(parts.length > 4 ? parts[4] : "");
        t.setStatus(parts.length > 5 ? parts[5] : "picked");
        t.setPickupLocation(parts.length > 6 ? parts[6] : "");
        t.setDeliveryLocation(parts.length > 7 ? parts[7] : "");
        t.setCurrentLatitude(parts.length > 8 ? Double.parseDouble(parts[8]) : 0);
        t.setCurrentLongitude(parts.length > 9 ? Double.parseDouble(parts[9]) : 0);
        t.setEstimatedArrival(parts.length > 10 ? Long.parseLong(parts[10]) : 0);
        t.startedAt = parts.length > 11 ? Long.parseLong(parts[11]) : 0;
        t.deliveredAt = parts.length > 12 ? Long.parseLong(parts[12]) : 0;
        return t;
    }
}
