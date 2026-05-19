package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Review Model - Represents a food/restaurant review and rating
 * OOP: Encapsulation — private fields, public accessors
 */
public class Review {
    @JsonProperty("id")
    private String id;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("foodId")
    private String foodId;
    @JsonProperty("foodName")
    private String foodName;
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("rating")
    private int rating; // 1-5
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("createdAt")
    private long createdAt;

    /** hidden = true means admin has hidden this review from public view */
    @JsonProperty("hidden")
    private boolean hidden = false;

    public Review() {}

    public Review(String userId, String userName, String foodId, String foodName,
                  String orderId, int rating, String comment) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.userId = userId;
        this.userName = userName;
        this.foodId = foodId;
        this.foodName = foodName;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = System.currentTimeMillis();
        this.hidden = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getFoodId() { return foodId; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public String toFileLine() {
        return id + "||" + userId + "||" + userName + "||" + foodId + "||" + foodName + "||" +
               orderId + "||" + rating + "||" + (comment != null ? comment.replace("|", "&#124;") : "") +
               "||" + createdAt + "||" + hidden;
    }

    public static Review fromFileLine(String line) {
        String[] parts = line.split("\\|\\|", -1);
        Review r = new Review();
        r.id        = parts.length > 0 ? parts[0] : "";
        r.userId    = parts.length > 1 ? parts[1] : "";
        r.userName  = parts.length > 2 ? parts[2] : "";
        r.foodId    = parts.length > 3 ? parts[3] : "";
        r.foodName  = parts.length > 4 ? parts[4] : "";
        r.orderId   = parts.length > 5 ? parts[5] : "";
        r.rating    = parts.length > 6 ? parseInt(parts[6]) : 0;
        r.comment   = parts.length > 7 ? parts[7].replace("&#124;", "|") : "";
        r.createdAt = parts.length > 8 ? parseLong(parts[8]) : 0;
        r.hidden    = parts.length > 9 && Boolean.parseBoolean(parts[9]);
        return r;
    }

    private static int parseInt(String s) { try { return Integer.parseInt(s.trim()); } catch(Exception e){ return 0; } }
    private static long parseLong(String s) { try { return Long.parseLong(s.trim()); } catch(Exception e){ return 0; } }
}
