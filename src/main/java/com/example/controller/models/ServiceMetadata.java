package com.example.controller.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "ServiceMetadata")
@Table(name = "service_metadata")
public
class ServiceMetadata {
    @Id
    @Column(name = "id", updatable = false)
    public String id;
    @Column(name = "name")
    public String name;
    @Column(name = "maxInstanceCount", nullable = false)
    public Integer maxInstanceCount;
    @Column(name = "jarFilePath", nullable = false)
    public String jarFilePath;
    // @Column(name = "maxInstanceCount", nullable = false)
    public String [] depends;
}