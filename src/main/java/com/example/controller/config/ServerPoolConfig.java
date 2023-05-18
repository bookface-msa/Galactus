package com.example.controller.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "pool")
@PropertySource(value = "classpath:serverpool.yaml", factory = YamlPropertySourceFactory.class)
public class ServerPoolConfig {
    private List<String> ips;

    public List<String> getIps(){
        return this.ips;
    }

    public void setIps(List<String> ips){
        this.ips = ips;
    }

}
