package com.example.controller.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    public Path targetDir;

    @Autowired
    public FileStorageService(FileStorageProperties properties) throws IOException{
        this.targetDir = Paths.get(properties.getUploadDir())
                .toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
    }

    public String saveFile(MultipartFile file) throws IOException{
        String serviceId = UUID.randomUUID().toString();
        Path targetPath = this.targetDir.resolve(serviceId + ".jar");
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return serviceId;
    }

    public byte[] getFile(String fileName) throws IOException{
        Path targetPath = this.targetDir.resolve(fileName);
        return Files.readAllBytes(targetPath);
    }

    public void deleteFile(String fileName){
        // TODO
    }
}
