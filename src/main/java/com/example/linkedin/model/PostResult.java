package com.example.linkedin.model;

/**
 * Result of posting a response to LinkedIn.
 */
public class PostResult {
    private boolean success;
    private String responseId;
    private String errorMessage;
    private int statusCode;

    public PostResult(boolean success) {
        this.success = success;
    }

    public PostResult(boolean success, String responseId) {
        this.success = success;
        this.responseId = responseId;
    }

    public PostResult(boolean success, String errorMessage, int statusCode) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
