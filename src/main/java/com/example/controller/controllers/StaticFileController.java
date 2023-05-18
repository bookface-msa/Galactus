package com.example.controller.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.controller.services.FileStorageService;


@RestController
@RequestMapping(path = "/v1/source")
public class StaticFileController {
    
    @Autowired
    FileStorageService fileStorageService;

    @GetMapping(path="/{fileName}")
    public @ResponseBody byte[] getServiceJar(@PathVariable("fileName") String fileName) throws IOException{
        return fileStorageService.getFile(fileName);
    }
}
