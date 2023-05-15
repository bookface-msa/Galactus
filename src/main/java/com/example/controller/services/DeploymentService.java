package com.example.controller.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.controller.controllers.ServiceController.ServiceMetadata;

@Service
public class DeploymentService {
    private String HAProxyHostname;
    private int HAProxyPort;
    
    @Autowired
    public DeploymentService(DeploymentProperties deploymentProperties) {
        this.HAProxyHostname = deploymentProperties.getHAProxyHostname();
        this.HAProxyPort = deploymentProperties.getHAProxyPort();
    }

    public void deployService(String serviceId, MultipartFile file, ServiceMetadata metadata) {
        // TODO Extract serviceMame from serviceMetadata
        String serviceName = "web_backends";

        // TODO Get a free server from the servers' pool
        String serverName = "webx";
        String serverSocket = "127.0.0.1:8089";

        // TODO Deploy the service on the server

        addServerToLoadBalancer(serviceName, serverName, serverSocket);
    }

    private void addServerToLoadBalancer(String serviceName, String serverName, String serverSocket) {
        String addServerCommand = constructAddServerCommand(serviceName, serverName, serverSocket);

        sendCommandToLoadBalancer(addServerCommand);
    }

    private void sendCommandToLoadBalancer(String command) {
        System.out.println(HAProxyHostname);
        System.out.println(HAProxyPort);
        System.out.println(command);
        try {
            Socket socket = new Socket(HAProxyHostname, HAProxyPort);
            InputStream socketInputStream = socket.getInputStream();
            OutputStream socketOutputStream = socket.getOutputStream();

            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socketInputStream));
            PrintWriter socketWriter = new PrintWriter(new OutputStreamWriter(socketOutputStream));

            socketWriter.println(command);
            socketWriter.flush();
            String line = socketReader.readLine();
            while (line != null) {
                System.out.println(line);
                line = socketReader.readLine();
            }

            socketInputStream.close();
            socketOutputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String constructAddServerCommand(String serviceName, String serverName, String serverSocket) {
        String serviceServer = String.format("%s/%s", serviceName, serverName);
        return String.format("%s %s %s", "add server", serviceServer, serverSocket);
    }
}
