package com.foodgo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payment Model - OOP: Encapsulation; Inheritance base class for payment types
 * Handles both Cash and Card payment methods
 */
public class Payment {
    @JsonProperty("id")
    private String id;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("amount")
    private double amount;

    /** cash | card */
    @JsonProperty("method")
    private String method;

    /** pending | paid | failed | refunded */
    @JsonProperty("status")
    private String status = "pending";

    // Card-specific fields (only populated when method=card)
    @JsonProperty("cardLast4")
    private String cardLast4 = "";
    @JsonProperty("cardBrand")
    private String cardBrand = "";

    @JsonProperty("transactionRef")
    private String transactionRef = "";

    @JsonProperty("createdAt")
    private long createdAt;
    @JsonProperty("updatedAt")
    private long updatedAt;

    public Payment() {}

    public Payment(String orderId, String userId, double amount, String method) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.status = "pending";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; this.updatedAt = System.currentTimeMillis(); }
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4 != null ? cardLast4 : ""; }
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand != null ? cardBrand : ""; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef != null ? transactionRef : ""; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    private static String esc(String s) { return s != null ? s.replace("||", "&#124;&#124;") : ""; }
    private static long parseLong(String s) { try { return Long.parseLong(s.trim()); } catch(Exception e){ return 0; } }
    private static double parseDouble(String s) { try { return Double.parseDouble(s.trim()); } catch(Exception e){ return 0.0; } }

    public String toFileLine() {
        return esc(id)+"||"+esc(orderId)+"||"+esc(userId)+"||"+amount+"||"+esc(method)+"||"
              +esc(status)+"||"+esc(cardLast4)+"||"+esc(cardBrand)+"||"+esc(transactionRef)+"||"
              +createdAt+"||"+updatedAt;
    }

    public static Payment fromFileLine(String line) {
        String[] p = line.split("\\|\\|", -1);
        Payment pay = new Payment();
        pay.id             = p.length>0  ? p[0]  : "";
        pay.orderId        = p.length>1  ? p[1]  : "";
        pay.userId         = p.length>2  ? p[2]  : "";
        pay.amount         = p.length>3  ? parseDouble(p[3]) : 0;
        pay.method         = p.length>4  ? p[4]  : "cash";
        pay.status         = p.length>5  ? p[5]  : "pending";
        pay.cardLast4      = p.length>6  ? p[6]  : "";
        pay.cardBrand      = p.length>7  ? p[7]  : "";
        pay.transactionRef = p.length>8  ? p[8]  : "";
        pay.createdAt      = p.length>9  ? parseLong(p[9])  : 0;
        pay.updatedAt      = p.length>10 ? parseLong(p[10]) : 0;
        return pay;
    }
}
