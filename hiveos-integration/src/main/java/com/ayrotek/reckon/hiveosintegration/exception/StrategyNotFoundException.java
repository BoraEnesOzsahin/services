package com.ayrotek.reckon.hiveosintegration.exception;

public class StrategyNotFoundException extends RuntimeException {

    public StrategyNotFoundException(String id) {
        super("Mining strategy not found: " + id);
    }
}
