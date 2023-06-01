package com.example.controller.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.example.controller.models.ServerMetadata;
import com.example.controller.repos.ServerRepository;

@Service
@Scope("singleton")
public class ServerPool {
   
    public final byte STATUS_FREE = 0;
    public final byte STATUS_LOCKED = 1;
    public final byte STATUS_ALLOC = 2;

    @Autowired
    ServerRepository serverRepo;

    public synchronized Optional<ServerMetadata> getServer(){
        List<ServerMetadata> servers = serverRepo.findByStatus(STATUS_FREE);
        if(servers.size() == 0)
            return Optional.of(null);
        ServerMetadata server = servers.get(0);
        server.status = STATUS_LOCKED;
        return Optional.of(server);
    }

    public synchronized void freeServer(ServerMetadata server){
        server.status = STATUS_FREE;
        serverRepo.save(server);
    }

    public synchronized void allocServer(ServerMetadata server){
        server.status = STATUS_ALLOC;
        serverRepo.save(server);
    }

}
