package com.example.controller.internal;

import com.example.controller.models.ResourceMetadata;

public class RabbitMQPreprocess implements Processor {

    @Override
    public String apply(String input, ResourceMetadata[] args) {
        return input;
    }
    
}
