package com.foodgo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * API Response Wrapper - Standardizes all API responses
 */
public class ApiResponse {
    private String status;
    private String message;
    private Object data;
    private long timestamp;

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Static factory methods
    public static ApiResponse success(String message) {
        return new ApiResponse("success", message);
    }

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse("success", message, data);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse("error", message);
    }

    public static ApiResponse error(String message, Object data) {
        return new ApiResponse("error", message, data);
    }

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setData(Object data) { this.data = data; }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
