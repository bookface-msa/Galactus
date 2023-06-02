package com.example.controller.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.IssuerUriCondition;
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
    private static Executor executor = Executors.newFixedThreadPool(30);
    private static Map<String, Boolean> isResolved = Collections.synchronizedMap(new HashMap<>());
    private static final int SLEEP_DURATION = 20000;

    @Autowired
    DeploymentService deploymentService;

    @PostMapping(path = "/")
    public HashMap<String, Object> alert(@RequestBody Map<String, Object> payload) {
        JSONObject alertmanagerMessage = new JSONObject(payload);
        JSONArray alerts = alertmanagerMessage.getJSONArray("alerts");
        JSONObject alert = (JSONObject) alerts.get(0);
        JSONObject alertLabels = alert.getJSONObject("labels");
        String alertname = alertLabels.getString("alertname");
        String application = alertLabels.getString("application");

        logger.info("Alert, payload: " + payload);
        logger.info("alertname:" + alertname);
        logger.info("application:" + application);

        String result = "NO ACTION";
        Function<String, DeploymentStatus> action = null;
        switch (alertname) {
            case "ServiceHighCpuLoad":
               action =  deploymentService::scaleUpService;
                break;
            case "ServiceLowCpuLoad":
                action = deploymentService::scaleDownService;
                break;
        }

        if(action != null){
            result = "AM I SCALLING !";
            final Function<String, DeploymentStatus> effictiveAction = action;
            // assuming that the alert will be called once ??
            isResolved.put(application, false);
            executor.execute(() -> {
                while(true){
                    Boolean reloved = isResolved.get(application);
                    if(reloved == null || reloved == true){
                        break;
                    }
                    effictiveAction.apply(application);
                    try {
                        Thread.sleep(SLEEP_DURATION);
                    } catch (InterruptedException e) {
                       logger.error("interrupted", e);
                       break;
                    }
                }
            });
        }

        HashMap<String, Object> res = new HashMap<>();
        res.put("payload", payload);
        res.put("result", result);
        return res;
    }

    @PostMapping(path = "/resolve")
    public HashMap<String, Object> resovle(@RequestBody Map<String, Object> payload) {
        JSONObject alertmanagerMessage = new JSONObject(payload);
        JSONArray alerts = alertmanagerMessage.getJSONArray("alerts");
        JSONObject alert = (JSONObject) alerts.get(0);
        JSONObject alertLabels = alert.getJSONObject("labels");
        String alertname = alertLabels.getString("alertname");
        String application = alertLabels.getString("application");

        logger.info("Alert, payload: " + payload);
        logger.info("alertname:" + alertname);
        logger.info("application:" + application);

        isResolved.put(application, true);
        return null;
    }
}