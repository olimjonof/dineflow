package com.dineflow.infrastructure.event;

import com.dineflow.application.port.EventPayloadSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JacksonEventPayloadSerializer implements EventPayloadSerializer {

    private final ObjectMapper objectMapper;

    public JacksonEventPayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize event payload",
                    e
            );
        }
    }
}