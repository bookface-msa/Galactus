package com.example.controller.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.controller.models.ResourceMetadata;

public interface ResourceRepository extends JpaRepository<ResourceMetadata, String>{
    List<ResourceMetadata> findByName(String name);
}
