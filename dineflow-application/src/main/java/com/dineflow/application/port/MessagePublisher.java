package com.dineflow.application.port;

public interface MessagePublisher {
    void publish(String type, String payload);
}
