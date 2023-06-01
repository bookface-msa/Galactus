package com.example.controller;

import java.util.Collection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import com.example.controller.services.LoadBalancerControlProperties;


import com.example.controller.config.GalactusConfig;
import com.example.controller.models.ResourceMetadata;
import com.example.controller.models.ServerMetadata;
import com.example.controller.repos.ResourceRepository;
import com.example.controller.repos.ServerRepository;
import com.example.controller.services.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class,
		LoadBalancerControlProperties.class
})
public class ControllerApplication {
	static SpringApplication app;
	static ConfigurableApplicationContext ctx;
	public static void main(String[] args) {
		app = new SpringApplication(ControllerApplication.class);
		ctx = app.run(args);
		init();
	}

	public static void init() {
		var config = ctx.getBean(GalactusConfig.class);
		var serverRepo = ctx.getBean(ServerRepository.class);
		var servers = config.getPool();
		
		// not the most efficient, but we use java so ¯\_(ツ)_/¯
		if(servers != null){
			var allIps = serverRepo.findAll().stream().map((md) -> md.ipAddresse).toList();
			serverRepo.saveAllAndFlush(
					servers.stream().filter(server -> !allIps.contains(server.ip())).map((serverConfig) -> {
					var server = new ServerMetadata();
					server.ipAddresse = serverConfig.ip();
					server.name = serverConfig.name();
					server.username = serverConfig.username();
					server.password = serverConfig.password();
					return server;
				}
			).toList());
		}
		
		var resourceRepo = ctx.getBean(ResourceRepository.class);
		var resources = config.getResources();
		if(resources != null){
			var allResources = resourceRepo.findAll().stream().map((md) -> md.name).toList();
			resourceRepo.saveAllAndFlush(
				resources.stream().filter(r -> !allResources.contains(r.name())).map((r) -> {
					ResourceMetadata resource = new ResourceMetadata();
					resource.name = r.name();
					resource.ipAddresse = r.ip();
					resource.port = r.port();
					return resource;
			
				}
			).toList());
		}
	}
}
