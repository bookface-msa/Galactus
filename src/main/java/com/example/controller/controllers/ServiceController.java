package com.example.controller.controllers;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.controller.services.DeployService;
import com.example.controller.services.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(path="/v1/service")
public class ServiceController {

    public record ServiceMetadata(String name){
    }

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    DeployService deployService;

    @PostMapping(path="/deploy")
    public HashMap<String, String> deployService(@RequestParam("file") MultipartFile file, @RequestParam("data") String metadata) throws IOException{
        // TODO metadata
        ObjectMapper om = new ObjectMapper();
        ServiceMetadata sm = om.readValue(metadata, ServiceMetadata.class);
        System.out.println(sm);
        // TODO check magicbytes to validate its a jar file
        String serviceId = fileStorageService.saveFile(file);
        HashMap<String, String> res = new HashMap<>();
        // TODO call deployService to allocate a server
        // TODO response
        res.put("serviceId", serviceId);
        res.put("serviceName", file.getOriginalFilename());
        return res;
    }

    @PostMapping(path="/deliver")
    public void deliverSerive(){
        // TODO
    }

    @PostMapping(path="/update")
    public void updateService(){
        // TODO
    }

    @PostMapping(path="/shutdown")
    public void shutdownService(){
        // TODO
    }
    
}
