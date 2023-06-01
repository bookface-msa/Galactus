package com.example.controller.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "infra")
@PropertySource(value = "classpath:galactus-config.yaml", factory = YamlPropertySourceFactory.class)
public class GalactusConfig {

    private List<ServersConfig> pool;
    private List<ResourceConfig> resources;

    public List<ResourceConfig> getResources() {
        return resources;
    }

    public void setResources(List<ResourceConfig> resources) {
        this.resources = resources;
    }

    public List<ServersConfig> getPool() {
        return pool;
    }

    public void setPool(List<ServersConfig> pool) {
        this.pool = pool;
    }

    public static record ResourceConfig(String name, String ip, int port) {}
    public static record ServersConfig(String name, String ip, String username, String password){}
}
