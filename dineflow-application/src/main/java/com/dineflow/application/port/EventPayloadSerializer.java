package com.dineflow.application.port;

public interface EventPayloadSerializer {
    String serialize(Object object);
}
