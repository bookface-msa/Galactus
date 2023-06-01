package com.example.controller.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "ResourceMetadata")
@Table(name = "resource_metadata")
public class ResourceMetadata {
    @Id
    @Column(name = "name", updatable = false)
    public String name;
    
    @Column(name = "ip_addresse")
    public String ipAddresse;
    
    @Column(name = "port")
    public Integer port;
}
