package com.example.controller.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.controller.models.ServiceMetadata;

public interface ServiceMetadataRepository extends JpaRepository<ServiceMetadata, String> {
}