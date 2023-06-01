package com.example.controller.internal;

import com.example.controller.models.ResourceMetadata;

public class Pipeline {
    private Processor[] prs;
    ResourceMetadata[] args;

    public Pipeline(Processor[] processes, ResourceMetadata[] resourcs){
        this.prs = processes;
        this.args= resourcs;
    }

    public String process(String input){
        String res = input;
        for(Processor ps : this.prs)    
            res = ps.apply(res, args);
        return res;
    }  
}
