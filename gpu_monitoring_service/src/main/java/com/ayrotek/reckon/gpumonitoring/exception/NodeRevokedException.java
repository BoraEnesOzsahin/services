package com.ayrotek.reckon.gpumonitoring.exception;

public class NodeRevokedException extends RuntimeException {

    public NodeRevokedException(String nodeId) {
        super("Node has been revoked: " + nodeId);
    }
}
