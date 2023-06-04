package com.example.controller.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.controller.services.DeploymentService;
import com.example.controller.services.DeploymentService.DeploymentStatus;

@RestController
@RequestMapping(path = "/v1/alert")
public class AlertController {

    Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static Map<String, Boolean> isScaling = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, Future<?>> scaleRoutine = Collections.synchronizedMap(new HashMap<>());

    private static final int SLEEP_DURATION = 20000;

    @Autowired
    DeploymentService deploymentService;

    @PostMapping(path = "/")
    public String alert(@RequestBody Map<String, Object> payload) {
        logger.info("Alert, payload: " + payload);

        JSONObject alertmanagerMessage = new JSONObject(payload);
        JSONArray alerts = alertmanagerMessage.getJSONArray("alerts");
        JSONObject alert = (JSONObject) alerts.get(0);
        JSONObject alertLabels = alert.getJSONObject("labels");
        String alertname = alertLabels.getString("alertname");
        String alertStatus = alertmanagerMessage.getString("status");
        String application = alertLabels.getString("application");

        logger.info("alertname:" + alertname);
        logger.info("application:" + application);
        logger.info("status: " + alertStatus);
        
        // choose action 
        switch (alertname) {
            case "ServiceHighCpuLoad":
                isScaling.put(application, true);
                break;
            case "ServiceLowCpuLoad":
                isScaling.put(application, false);
                break;
            default:
                return "NOP !";
        }

        Future<?> _future = scaleRoutine.get(application);
        if(_future != null && ! _future.isDone() ){
            return "JUMP !";
        }
        // assuming that the alert will be called once ??
        Future<?> futuer = executor.submit(() -> {
            while(true){
                Boolean scaling = isScaling.get(application);

                // do scale
                DeploymentStatus status = DeploymentStatus.DONE;
                if(scaling == true){
                    status = deploymentService.scaleUpService(application);
                }else {
                    status = deploymentService.scaleDownService(application);
                }
                
                // can't scale more -> return thread to pool
                if(
                    (status == DeploymentStatus.SCALED_DOWN && isScaling.get(application) == false)||
                    (status == DeploymentStatus.SCALED_UP && isScaling.get(application) == true)
                ){
                    logger.info("stop scaling");
                    return;
                }

                // some interrupt happend !
                try {
                    Thread.sleep(SLEEP_DURATION);
                } catch (InterruptedException e) {
                    logger.error("interrupted", e);
                    return;
                }
            }
        });
        scaleRoutine.put(application, futuer);

        return "AM I SCALING!";
    }
}