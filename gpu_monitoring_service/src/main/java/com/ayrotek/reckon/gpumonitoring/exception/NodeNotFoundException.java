package com.ayrotek.reckon.gpumonitoring.exception;

public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(String nodeId) {
        super("Node not found: " + nodeId);
    }
}
