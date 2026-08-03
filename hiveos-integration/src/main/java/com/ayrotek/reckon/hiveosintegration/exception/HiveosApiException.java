package com.ayrotek.reckon.hiveosintegration.exception;

public class HiveosApiException extends RuntimeException {

    private final int status;

    public HiveosApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
