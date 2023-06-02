package com.example.controller.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
        Files.createDirectories(targetDir.resolve("props"));
    }

    public void saveFile(String serviceId, MultipartFile file) throws IOException{
        Path targetPath = this.targetDir.resolve(serviceId + ".jar");
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void savePropsFile(String serviceId, MultipartFile file) throws IOException {
        Path targetPath = this.targetDir.resolve("props").resolve(serviceId+"app.properties");
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public byte[] getFile(String fileName) throws IOException{
        Path targetPath = this.targetDir.resolve(fileName);
        return Files.readAllBytes(targetPath);
    }

    public byte[] getProps(String fileName) throws IOException {
        Path targePath = this.targetDir.resolve("props").resolve(fileName+"app.properties");
        return Files.readAllBytes(targePath);
    }

    public byte[] getMisc(String fileName) throws IOException {
        Path targePath = this.targetDir.resolve("props").resolve(fileName);
        return Files.readAllBytes(targePath);
    }

    public void deleteFile(String fileName) throws IOException{
        Path targetPath = this.targetDir.resolve(fileName);
        Files.delete(targetPath);
    }
}
