package com.example.controller.internal;

import com.example.controller.models.ResourceMetadata;

public interface Processor  {
    public String apply(String input, ResourceMetadata[] args);
}
