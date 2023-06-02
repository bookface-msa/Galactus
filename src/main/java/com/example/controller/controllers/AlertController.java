package com.example.controller.controllers;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.controller.services.DeploymentService;

@RestController
@RequestMapping(path = "/v1/alert")
public class AlertController {

    Logger logger = LoggerFactory.getLogger(DeploymentService.class);

    @Autowired
    DeploymentService deploymentService;
    
    @ResponseBody
    @PostMapping(path = "/alert")
    public HashMap<String, Object> alert(@RequestBody Map<String, Object> payload) {
        JSONObject alert = new JSONObject(payload);
        System.out.println(alert.get("alerts"));
        logger.info("Alert, payload: " + payload);
        HashMap<String, Object> res = new HashMap<>();
        res.put("payload", payload);
        return res;
    }
}