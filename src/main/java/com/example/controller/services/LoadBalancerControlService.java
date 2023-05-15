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

@Service
public class LoadBalancerControlService {
    private String HAProxyHostname;
    private int HAProxyPort;

    @Autowired
    public LoadBalancerControlService(LoadBalancerControlProperties loadBalancerControlProperties) {
        this.HAProxyHostname = loadBalancerControlProperties.getHAProxyHostname();
        this.HAProxyPort = loadBalancerControlProperties.getHAProxyPort();
    }

    public void addServerToLoadBalancer(String serviceName, String serverName, String serverSocket) {
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
