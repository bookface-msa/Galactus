package com.example.controller.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.catalina.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.controller.controllers.ServiceController.DeployServiceRequest;
import com.example.controller.models.ResourceMetadata;
import com.example.controller.models.ServerMetadata;
import com.example.controller.models.ServiceMetadata;
import com.example.controller.repos.ResourceRepository;
import com.example.controller.repos.ServiceRepository;
import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClient;
import com.sshtools.client.shell.ExpectShell;
import com.sshtools.client.shell.ShellProcess;
import com.sshtools.client.shell.ShellTimeoutException;
import com.sshtools.client.tasks.ShellTask;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.util.IOUtils;

@Service
public class DeploymentService {

    Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    Executor executor = Executors.newFixedThreadPool(30);

    @Autowired
    LoadBalancerControlService loadBalancerControlService;
    @Autowired
    ServerPool serverPool;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    ServiceRepository serviceRepo;
    @Autowired
    ResourceRepository resourceRepo;


    public enum DeploymentStatus {
        INVALID_DEPS,
        INVALID_FILE,
        SERVICE_EXIST,
        SERVICE_DOSNT_EXIST,
        PROCESSING,
        FAILED,
        SCALED_DOWN,
        SCALED_UP,
        DONE
    }

    public enum SSHStepsStatus{
        SUCCESSFUL,
        FAILED
    }


    public class NoServerAvailableExecption extends Exception {}
    public class SSHStepsExecption extends Exception {}

    public void freezeService(String serviceName){
        var services = serviceRepo.findByName(serviceName);
        if (services == null || services.size() == 0)
            return;

        var service = services.get(0);
        this.executor.execute(() -> {
            logger.info("Freezing service:" + service.name);
            _freezeJob(service);
        });
    }
    
    public void resumeService(String serviceName) {
        var services = serviceRepo.findByName(serviceName);
        if (services == null || services.size() == 0)
            return;

        var service = services.get(0);
        this.executor.execute(() -> {
            logger.info("Freezing service:" + service.name);
            _continueJob(service);
        });
    }

    public void shutdownService(String serviceName) {
        var services = serviceRepo.findByName(serviceName);
        if (services == null || services.size() == 0)
            return;

        var service = services.get(0);
        this.executor.execute(() -> {
            logger.info("ShutingDown service:" + service.name);
            _shutdownJob(service);
        });
    }

    public DeploymentStatus scaleDownService(String serviceName) {
        var services = serviceRepo.findByName(serviceName);
        if (services == null || services.size() == 0)
            return DeploymentStatus.SERVICE_DOSNT_EXIST;
        var service = services.get(0);
        if(service.servers.size() == 1) return DeploymentStatus.SCALED_DOWN;

        this.executor.execute(() -> {
            logger.info("Scaleing down service:" + service.name);
            _scaleDownJob(service);
        });
        return DeploymentStatus.PROCESSING;
    }

    public DeploymentStatus scaleUpService(String serviceName){
        var services = serviceRepo.findByName(serviceName);
        if(services == null || services.size() == 0)
            return DeploymentStatus.SERVICE_DOSNT_EXIST;
        
        var service = services.get(0);
        if(service.maxInstanceCount != null && service.maxInstanceCount == service.servers.size())
            return DeploymentStatus.SCALED_UP;
        this.executor.execute(() -> {
            logger.info("Scaleing up service:"+service.name);
            _deploymentJob(service);
        });
        return DeploymentStatus.PROCESSING;
    }

    public DeploymentStatus deliverUpdatetoService(DeployServiceRequest metadata, MultipartFile file, MultipartFile propsFile) throws IOException {
        var services = serviceRepo.findByName(metadata.name());
        if (services == null || services.size() == 0)
            return DeploymentStatus.SERVICE_DOSNT_EXIST;

        String[] deps = metadata.deps();
        var depsFound = new HashSet<ResourceMetadata>();
        if (deps != null && deps.length != 0)
            for (String depName : deps) {
                var resource = resourceRepo.findByName(depName);
                if (resource.size() == 0)
                    return DeploymentStatus.INVALID_DEPS;
                else
                    depsFound.add(resource.get(0));
            }

        var service = services.get(0);
        service.name = metadata.name();
        service.port = metadata.port();
        service.maxInstanceCount = metadata.maxInstanceCount();
        service.resources = depsFound;
        serviceRepo.saveAndFlush(service);

        fileStorageService.saveFile(service.id, file);
        fileStorageService.savePropsFile(service.id, propsFile);

        this.executor.execute(() -> {
            logger.info("Delivering update to service:" + service.name);
            _deliverJob(service);
        });
        return DeploymentStatus.PROCESSING;
    }

    public DeploymentStatus deployService(DeployServiceRequest metadata, MultipartFile file, MultipartFile propsFile) throws IOException {
        var depsFound = new HashSet<ResourceMetadata>();
        var result = serviceRepo.findByName(metadata.name());
        if(result.size() != 0)
            return DeploymentStatus.SERVICE_EXIST;
        String[] deps = metadata.deps();
        if(deps != null && deps.length != 0)
            for(String depName : deps){
                var resource = resourceRepo.findByName(depName);
                if(resource.size() == 0)
                    return DeploymentStatus.INVALID_DEPS;
                else
                    depsFound.add(resource.get(0));
            }
        if(!file.getContentType().equals("application/octet-stream"))
            return DeploymentStatus.INVALID_FILE;
                    
        ServiceMetadata service = new ServiceMetadata();
        service.id = UUID.randomUUID().toString();
        service.name = metadata.name();
        service.port = metadata.port();
        service.maxInstanceCount = metadata.maxInstanceCount();
        service.resources = depsFound;
        serviceRepo.saveAndFlush(service);

        fileStorageService.saveFile(service.id, file);
        fileStorageService.savePropsFile(service.id, propsFile);
        // TODO preprocess the properties file

        this.executor.execute(() -> {
            logger.info("Deploying up service"+service.name);
           _deploymentJob(service);
        });

        return DeploymentStatus.PROCESSING;
    }

    private void _deploymentJob(ServiceMetadata service){
        try {
            ServerMetadata allocatedServer = serverPool.getServer().orElseThrow(
                    () -> new NoServerAvailableExecption());
            logger.info("Allocated Server: " + allocatedServer.ipAddresse + ", for service" + service.name);
            
            SSHStepsStatus res =  _runSSHSession(
                    service,
                    allocatedServer.ipAddresse,
                    allocatedServer.username,
                    allocatedServer.password,
                    deploymentSSHSteps(service.id));

            if(res == SSHStepsStatus.FAILED){
                allocatedServer.deploymentStatus = DeploymentStatus.FAILED.toString();
                serverPool.freeServer(allocatedServer);
                return;
            }

            // String serviceAddress = allocatedServer.ipAddresse.split(":")[0] + ":" + service.port;
            // loadBalancerControlService.addServer(
            //         service.name,
            //         allocatedServer.name,
            //         serviceAddress);
            loadBalancerControlService.unFreezeServer(service.name, allocatedServer.name);

            serverPool.allocServer(allocatedServer);
            if(service.servers == null)
                service.servers = new HashSet<>();
            service.servers.add(allocatedServer);
            serviceRepo.save(service);
        } catch (NoServerAvailableExecption e) {
            logger.error("failed to deploy service", e);
        }
    }

    private void _deliverJob(ServiceMetadata service) {
        if (service.servers == null)
            return;
        Set<ServerMetadata> toDelete = new HashSet<>();
        int ServerNum = service.servers.size();

        for(ServerMetadata server: service.servers)
            toDelete.add(server);

        while(ServerNum-->0)
            _deploymentJob(service);
        
        // put all the server in ready mode
        for (ServerMetadata server : toDelete) {
            _runSSHSession(service, server.ipAddresse, server.username, server.password, cleanUpSSHSteps(null));
            loadBalancerControlService.freezeServer(service.name, server.name);
            // loadBalancerControlService.deleteServer(service.name, server.name);
            serverPool.freeServer(server);
            service.servers.remove(server);
        }
        serviceRepo.save(service);
    }

    private void _shutdownJob(ServiceMetadata service){
        if(service.servers == null) return;
        
        // put all the server in maint mode
        for(ServerMetadata server: service.servers){
            loadBalancerControlService.freezeServer(service.name, server.name);
        }

        // run cleanup steps to all servers
        for(ServerMetadata server: service.servers){
            _runSSHSession(service, server.ipAddresse, server.username, server.password, cleanUpSSHSteps(null));
            loadBalancerControlService.freezeServer(service.name, server.name);
            // loadBalancerControlService.deleteServer(service.name, server.name);
            serverPool.freeServer(server);
            // service.servers.remove(server);
            // serviceRepo.save(service);
        }

        logger.warn("deleteing service "+ service.id);
        serviceRepo.flush();
        service.resources = new HashSet<>();
        service.servers = new HashSet<>();
        serviceRepo.saveAndFlush(service);
        serviceRepo.delete(service);
    }

    private void _freezeJob(ServiceMetadata service){
        if(service.servers == null) return;
        
        // put all the server in maint mode
        for (ServerMetadata server : service.servers) {
            loadBalancerControlService.freezeServer(service.name, server.name);
        }
    }

    private void _continueJob(ServiceMetadata service){
        if (service.servers == null)
            return;

        // put all the server in ready mode
        for (ServerMetadata server : service.servers) {
            loadBalancerControlService.unFreezeServer(service.name, server.name);
        }
    }

    private void _scaleDownJob(ServiceMetadata service) {
        if (service.servers == null)
            return;

        // put all the server in ready mode
        for (ServerMetadata server : service.servers) {
            _runSSHSession(service, server.ipAddresse, server.username, server.password, cleanUpSSHSteps(null));
            loadBalancerControlService.freezeServer(service.name, server.name);
            // loadBalancerControlService.deleteServer(service.name, server.name);
            serverPool.freeServer(server);
            service.servers.remove(server);
            serviceRepo.save(service);
            break;
        }
    }
    
    private SSHStepsStatus _runSSHSession(ServiceMetadata service, String host, String username, String password, String[] steps){
        String[] hostAddr = host.split(":");
        logger.info("Connecting to host: "+host);
        String ip = hostAddr[0];
        int port = Integer.parseInt(hostAddr[1]);

        try (SshClient jump = new SshClient(ip, port, username, password.toCharArray())) {
            jump.runTask(new ShellTask(jump) {
                @Override
                protected void onOpenSession(SessionChannelNG session)
                        throws IOException, SshException,
                        ShellTimeoutException {

                    ExpectShell shell = new ExpectShell(this);

                    // var s = shell.executeCommand("rm -rf ./service && mkdir service");
                    // var ss = IOUtils.readStringFromStream(s.getInputStream(), "utf-8");
                    // System.out.println("HERE1" + s.getExitCode() + " " + ss);
                    for(String step : steps){
                        logger.info("running command: "+step);
                        ShellProcess shellProcess = shell.executeCommand(step);
                        String commandOutput = IOUtils.readStringFromStream(shellProcess.getInputStream(), "utf-8");
                        logger.info("command output: " + commandOutput);
                        int exitCode = shellProcess.getExitCode();
                        // System.out.println("HEER\n\n\n"+exitCode);
                        if(exitCode != 0){
                            logger.error(
                                "Error while executing step:"+step+" on server:"+host+" output:"+commandOutput, 
                                getLastError()
                            );                
                            throw new IOException("Couldn't execute step:"+step, getLastError());
                        }
                    }
                }
            });
        }catch(IOException | SshException e){
            return SSHStepsStatus.FAILED;
        }
        return SSHStepsStatus.SUCCESSFUL;
    }

    private String[] deploymentSSHSteps (String serviceId) {
        return new String[]{
            "rm -rf ./service && mkdir service",
            "cd service",
            "wget -O service.jar http://controller:8080/v1/source/"+serviceId+".jar",
            "wget -O app.properties http://controller:8080/v1/source/props/"+serviceId,
            "echo Done:$(date) >> releases.txt",
            "java -jar service.jar --spring.config.location=file://$(pwd)/app.properties & disown",
            "ps -aux",
        };
    }

    private String[] cleanUpSSHSteps(String serviceId) {
        return new String[] {
            "rm -rf ./service",
            "rm -f releases.txt",
            "pgrep java | xargs kill -2",
        };
    }

}
