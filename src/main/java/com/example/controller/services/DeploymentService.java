package com.example.controller.services;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.controller.models.ServiceMetadata;
import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClient;
import com.sshtools.client.shell.ExpectShell;
import com.sshtools.client.shell.ShellTimeoutException;
import com.sshtools.client.tasks.ShellTask;
import com.sshtools.common.ssh.SshException;

@Service
public class DeploymentService {

    Logger logger = LoggerFactory.getLogger(DeploymentService.class);

    @Autowired
    LoadBalancerControlService loadBalancerControlService;
    @Autowired
    ServerPool serverPool;


    public class NoServerAvailableExecption extends Exception {

    }

    public void deployService(String serviceId, MultipartFile file, ServiceMetadata metadata) throws NoServerAvailableExecption, NumberFormatException, IOException, SshException {

        String serviceName = metadata.name;
        String allocatedServer = serverPool.getServer().orElseThrow(
            () -> new NoServerAvailableExecption()
        );
        logger.info("Allocated Server: "+allocatedServer + ", for service" + serviceName);
        String[] host = allocatedServer.split(":");

        // !NOTES
        // - threadpool for deployment
        // - shorter timeout for ssh connection

        // TODO Deploy the service on the server
        try (SshClient jump = new SshClient(host[0], Integer.parseInt(host[1]), "root", "mypassword".toCharArray())) {

            jump.runTask(new ShellTask(jump) {
                @Override
                protected void onOpenSession(SessionChannelNG session)
                        throws IOException, SshException,
                        ShellTimeoutException {

                    ExpectShell shell = new ExpectShell(this);

                    shell.execute("mkdir service");
                    shell.execute("cd service");
                    shell.execute("wget -O service.jar http://localhost:8080/source/"+serviceId+".jar");
                    shell.execute("echo Done:$(date) >> releases.txt");
                    // TODO run jar file
                }

            });
        }


        // loadBalancerControlService.addServerToLoadBalancer(serviceName, "serverName", allocatedServer);
    }
}
