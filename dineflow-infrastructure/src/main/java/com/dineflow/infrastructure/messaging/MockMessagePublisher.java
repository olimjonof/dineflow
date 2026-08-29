package com.dineflow.infrastructure.messaging;

import com.dineflow.application.port.MessagePublisher;
import org.springframework.stereotype.Component;

@Component
public class MockMessagePublisher implements MessagePublisher {

    @Override
    public void publish(String type, String payload) {
        System.out.println(
                "Publishing event: " + type +
                        " | payload: " + payload
        );
    }
}