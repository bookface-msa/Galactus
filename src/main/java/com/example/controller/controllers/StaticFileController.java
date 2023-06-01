package com.example.controller.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.controller.services.FileStorageService;


@RestController
@RequestMapping(path = "/v1/source")
public class StaticFileController {
    
    @Autowired
    FileStorageService fileStorageService;

    @GetMapping(path="/{fileName}")
    public @ResponseBody byte[] getServiceJar(@PathVariable("fileName") String fileName){
        try {
            return fileStorageService.getFile(fileName);
        } catch (IOException e) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file does not exist");
        }
    }

    @GetMapping(path="/props/{fileName}")
    public @ResponseBody byte[] getProps(@PathVariable("fileName") String fileName) {
        try {
            return fileStorageService.getProps(fileName);
        } catch (IOException e) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "file does not exist");
        }
    }

    @GetMapping(path="/props/{fileName}")
    public @ResponseBody byte[] getProps(@PathVariable("fileName") String fileName) throws IOException {
        return fileStorageService.getProps(fileName);
    }
}
