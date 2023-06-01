package com.example.controller.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.controller.models.ServiceMetadata;

public interface ServiceRepository extends JpaRepository<ServiceMetadata, String> {
    List<ServiceMetadata> findByName(String name);
}