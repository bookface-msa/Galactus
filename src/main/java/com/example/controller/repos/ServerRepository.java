package com.example.controller.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.controller.models.ServerMetadata;

public interface ServerRepository extends JpaRepository<ServerMetadata, Integer> {
    List<ServerMetadata> findByStatus(byte status);
}
