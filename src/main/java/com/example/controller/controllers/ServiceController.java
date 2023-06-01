package com.example.controller.controllers;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.controller.services.DeploymentService;
import com.example.controller.services.DeploymentService.NoServerAvailableExecption;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sshtools.common.ssh.SshException;

@RestController
@RequestMapping(path = "/v1/service")
public class ServiceController {

    @Autowired
    DeploymentService deploymentService;

    public record DeployServiceRequest(String name, Integer port, Integer maxInstanceCount, String[] deps){}

    @ResponseBody
    @PostMapping(path = "/deploy")
    public HashMap<String, String> deployService(@RequestParam("file") MultipartFile file,
            @RequestParam("propsFile") MultipartFile propsFile,
            @RequestParam("data") String metadata) throws IOException, NoServerAvailableExecption, NumberFormatException, SshException {
        ObjectMapper om = new ObjectMapper();
        DeployServiceRequest request = om.readValue(metadata, DeployServiceRequest.class);
        
        if(request.name == null || request.port == null || request.maxInstanceCount == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID SERVICE METADATA");
        
        var status = deploymentService.deployService(request, file, propsFile);
        
        // TODO response
        HashMap<String, String> res = new HashMap<>();
        res.put("status", status.toString());
        res.put("serviceName", file.getOriginalFilename());
        return res;
    }

    @PostMapping(path = "/deliver")
    public void deliverSerive(@RequestParam("file") MultipartFile file, @RequestParam("data") String metadata) throws JsonMappingException, JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        DeployServiceRequest request = om.readValue(metadata, DeployServiceRequest.class);

        if (request.name == null || request.port == null || request.maxInstanceCount == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID SERVICE METADATA");

        // TODO get old service and freeze and then shutdown
        // TODO deploy a new service with server
    }

    @PostMapping(path = "/update")
    public void updateService(@RequestParam("data") String metadata) {
        // TODO update metadata of the service that could also include updating deps ??
    }

    @PostMapping(path = "/freeze/{serviceName}")
    public void freezeService(@PathVariable String serviceName) {
        deploymentService.freezeService(serviceName);
    }

    @PostMapping(path = "/continue/{serviceId}")
    public void continueService(@PathVariable String serviceName) {
        deploymentService.resumeService(serviceName);
    }

    @PostMapping(path = "/shutdown/{serviceName}")
    public void shutdownService(@PathVariable String serviceName) throws Exception {
        deploymentService.shutdownService(serviceName);
    }

}
