package com.example.controller.services;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.controller.config.ServerPoolConfig;

@Service
public class ServerPool {
    private Queue<String> readyPool;
    private Set<String> alloected;

    @Autowired
    public ServerPool(ServerPoolConfig config){
        this.readyPool = new LinkedList<>();
        this.alloected = new HashSet<>();

        // populate the server ready queue
        // TODO create server entity to wrap ips
        this.readyPool.addAll(config.getIps());
    }

    public Optional<String> getServer(){
        if(this.readyPool.isEmpty()) 
            return Optional.of(null);
        String serverIp = this.readyPool.poll();
        this.alloected.add(serverIp);
        return Optional.of(serverIp);
    }

    public void freeServer(String serverIp){
        // TODO
    }

}
