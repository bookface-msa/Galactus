package com.example.controller.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.controller.controllers.ServiceController.ServiceMetadata;

@Service
public class DeploymentService {

    @Autowired
    LoadBalancerControlService loadBalancerControlService;

    public void deployService(String serviceId, MultipartFile file, ServiceMetadata metadata) {
        // TODO Extract serviceMame from serviceMetadata
        String serviceName = "web_backends";

        // TODO Get a free server from the servers' pool
        String serverName = "webx";
        String serverSocket = "127.0.0.1:8089";

        // TODO Deploy the service on the server

        loadBalancerControlService.addServerToLoadBalancer(serviceName, serverName, serverSocket);
    }
}
