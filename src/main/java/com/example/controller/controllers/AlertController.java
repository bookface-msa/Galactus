package com.example.controller.controllers;

import java.util.HashMap;
import java.util.Map;

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

@RestController
@RequestMapping(path = "/v1/alert")
public class AlertController {

    Logger logger = LoggerFactory.getLogger(DeploymentService.class);

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

        switch (alertname) {
            case "ServiceHighCpuLoad":
                deploymentService.scaleUpService(application);
                break;
            case "ServiceLowCpuLoad":
                deploymentService.scaleDownService(application);
                break;
        }

        HashMap<String, Object> res = new HashMap<>();
        res.put("payload", payload);
        return res;
    }
}