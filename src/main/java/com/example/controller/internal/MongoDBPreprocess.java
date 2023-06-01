package com.example.controller.internal;

import java.util.StringJoiner;

import com.example.controller.models.ResourceMetadata;

public class MongoDBPreprocess implements Processor {

    @Override
    public String apply(String input, ResourceMetadata[] args) {
        ResourceMetadata current = null;
        for(ResourceMetadata resource: args)
            if(resource.name.contains("mongo")){
                current = resource;
                break;
            }
        if(current == null)
            return input;

        String[] lines = input.split("\n");
        for (int i=0; i<lines.length; i++) {
            String line = lines[i];
            if (line.contains("mongodb.host")) {
                lines[i] = "spring.data.mongodb.host="+current.ipAddresse;
            }

            if (line.contains("mongodb.uri")) {
                String[] parts = line.split("=");
                String updated = parts[1].replace("localhost", current.ipAddresse);
                lines[i] = parts[0]+"="+updated;
            }
        }

        StringJoiner sj = new StringJoiner("\n");
        for(String line: lines) sj.add(line);
        return sj.toString();
    }

}
