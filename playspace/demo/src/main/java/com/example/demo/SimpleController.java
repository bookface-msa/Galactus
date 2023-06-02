package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/test")
public class SimpleController {

	Logger logger = LoggerFactory.getLogger(SpringBootAppApplication.class);

    @GetMapping
    public String hello(){
        logger.info("PLease Work");
        return "hello2";
    }
}
