package org.syncloud.android.model;

public class VerifyStatus {
    private boolean valid = false;
    private String  message = "";

    public VerifyStatus(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return valid;
    }
}
