package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Model - Represents a customer order in the FoodGo system
 */
public class Order {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("items")
    private List<OrderItem> items;
    
    @JsonProperty("total")
    private double total;
    
    @JsonProperty("status")
    private String status; // pending, accepted, preparing, ready, picked, delivered, cancelled
    
    @JsonProperty("paymentMethod")
    private String paymentMethod; // cod, card
    
    @JsonProperty("deliveryAddress")
    private String deliveryAddress;
    
    @JsonProperty("specialInstructions")
    private String specialInstructions;
    
    @JsonProperty("estimatedTime")
    private int estimatedTime; // minutes
    
    @JsonProperty("assignedRiderId")
    private String assignedRiderId;
    
    @JsonProperty("createdAt")
    private long createdAt;
    
    @JsonProperty("updatedAt")
    private long updatedAt;

    public Order() {}

    public Order(String userId, String userName, List<OrderItem> items, double total, 
                 String paymentMethod, String deliveryAddress) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.userId = userId;
        this.userName = userName;
        this.items = items;
        this.total = total;
        this.status = "pending";
        this.paymentMethod = paymentMethod;
        this.deliveryAddress = deliveryAddress;
        this.estimatedTime = 30;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; this.updatedAt = System.currentTimeMillis(); }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getAssignedRiderId() { return assignedRiderId; }
    public void setAssignedRiderId(String assignedRiderId) { this.assignedRiderId = assignedRiderId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String toFileLine() {
        // Serialize items as JSON-like string to preserve complex data
        StringBuilder itemsStr = new StringBuilder("[");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                itemsStr.append(item.foodId).append("~").append(item.foodName).append("~")
                        .append(item.quantity).append("~").append(item.price).append("~")
                        .append(item.notes != null ? item.notes : "");
                if (i < items.size() - 1) itemsStr.append(";");
            }
        }
        itemsStr.append("]");
        
        return id + "||" + userId + "||" + (userName != null ? userName : "") + "||" + 
               itemsStr.toString() + "||" + total + "||" + status + "||" + paymentMethod + "||" + 
               (deliveryAddress != null ? deliveryAddress : "") + "||" + 
               (specialInstructions != null ? specialInstructions : "") + "||" + 
               estimatedTime + "||" + (assignedRiderId != null ? assignedRiderId : "") + "||" + 
               createdAt + "||" + updatedAt;
    }

    public static Order fromFileLine(String line) {
        String[] parts = line.split("\\|\\|", -1);
        Order o = new Order();
        o.setId(parts.length > 0 ? parts[0] : "");
        o.setUserId(parts.length > 1 ? parts[1] : "");
        o.setUserName(parts.length > 2 ? parts[2] : "");
        
        // Parse items
        if (parts.length > 3 && !parts[3].isEmpty() && !parts[3].equals("[]")) {
            String itemsStr = parts[3].substring(1, parts[3].length() - 1);
            List<OrderItem> items = new ArrayList<>();
            if (!itemsStr.isEmpty()) {
                String[] itemArray = itemsStr.split(";");
                for (String item : itemArray) {
                    String[] itemParts = item.split("~", -1);
                    if (itemParts.length >= 4) {
                        items.add(new OrderItem(
                            itemParts[0],
                            itemParts[1],
                            Integer.parseInt(itemParts[2]),
                            Double.parseDouble(itemParts[3]),
                            itemParts.length > 4 ? itemParts[4] : ""
                        ));
                    }
                }
            }
            o.setItems(items);
        }
        
        o.setTotal(parts.length > 4 ? Double.parseDouble(parts[4]) : 0);
        o.setStatus(parts.length > 5 ? parts[5] : "pending");
        o.setPaymentMethod(parts.length > 6 ? parts[6] : "cod");
        o.setDeliveryAddress(parts.length > 7 ? parts[7] : "");
        o.setSpecialInstructions(parts.length > 8 ? parts[8] : "");
        o.setEstimatedTime(parts.length > 9 ? Integer.parseInt(parts[9]) : 30);
        o.setAssignedRiderId(parts.length > 10 ? parts[10] : "");
        o.createdAt = parts.length > 11 ? Long.parseLong(parts[11]) : 0;
        o.updatedAt = parts.length > 12 ? Long.parseLong(parts[12]) : 0;
        return o;
    }

    @Override
    public String toString() {
        return "Order{" + "id='" + id + "', userId='" + userId + "', status='" + status +
               "', total=" + total + ", createdAt=" + createdAt + '}';
    }

    public static class OrderItem {
        @JsonProperty("foodId")
        public String foodId;

        @JsonProperty("foodName")
        public String foodName;

        @JsonProperty("quantity")
        public int quantity;

        @JsonProperty("price")
        public double price;

        @JsonProperty("notes")
        public String notes;

        public OrderItem() {}

        public OrderItem(String foodId, String foodName, int quantity, double price, String notes) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.quantity = quantity;
            this.price = price;
            this.notes = notes;
        }
    }
}
