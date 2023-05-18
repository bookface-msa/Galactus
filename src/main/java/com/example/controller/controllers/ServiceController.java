package com.example.controller.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.controller.services.DeploymentService;
import com.example.controller.services.FileStorageService;
import com.example.controller.services.DeploymentService.NoServerAvailableExecption;
import com.example.controller.config.ServerPoolConfig;
import com.example.controller.models.ServiceMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sshtools.common.ssh.SshException;

@RestController
@RequestMapping(path = "/v1/service")
public class ServiceController {

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    ServerPoolConfig config;

    @PostMapping(path = "/deploy")
    public HashMap<String, String> deployService(@RequestParam("file") MultipartFile file,
            @RequestParam("data") String metadata) throws IOException, NoServerAvailableExecption, NumberFormatException, SshException {
        // TODO metadata and save it to the db
        ObjectMapper om = new ObjectMapper();
        ServiceMetadata sm = om.readValue(metadata, ServiceMetadata.class);
        System.out.println(sm);
        // TODO check magicbytes to validate its a jar file
        String serviceId = fileStorageService.saveFile(file);
        HashMap<String, String> res = new HashMap<>();

        deploymentService.deployService(serviceId, file, sm);

        // TODO response
        res.put("serviceId", serviceId);
        res.put("serviceName", file.getOriginalFilename());
        return res;
    }

    @PostMapping(path = "/deliver")
    public void deliverSerive() {
        // TODO
    }

    @PostMapping(path = "/update")
    public void updateService() {
        // TODO
    }

    @PostMapping(path = "/freeze")
    public void freezeService() {
        // TODO
    }

    @PostMapping(path = "/continue")
    public void continueService() {
        // TODO
    }

}
