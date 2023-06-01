package com.example.controller.internal;

import com.example.controller.models.ResourceMetadata;

public class HaproxyPerprocess implements Processor {

    @Override
    public String apply(String input, ResourceMetadata[] args) {
        // NOTE not necessary
        return input;
    }
    
}
