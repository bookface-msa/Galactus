package com.example.controller.services;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deploy")
public class DeploymentProperties {
    private String HAProxyHostname;
    private int HAProxyPort;

    public String getHAProxyHostname() {
        return HAProxyHostname;
    }

    public int getHAProxyPort() {
        return HAProxyPort;
    }

    public void setHAProxyHostname(String HAProxyHostname) {
        this.HAProxyHostname = HAProxyHostname;
    }

    public void setHAProxyPort(int HAProxyPort) {
        this.HAProxyPort = HAProxyPort;
    }
}
